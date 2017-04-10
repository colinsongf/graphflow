package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.exceptions.SerializationDeserializationException;
import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to serialize and deserialize large arrays in parallel, and print proper status and
 * time output.
 */
public abstract class ParallelArraySerDeUtils {

    private static final Logger logger = LogManager.getLogger(ParallelArraySerDeUtils.class);

    private static final int MAX_SERIALIZATION_THREADS = 10;
    private static final Thread.UncaughtExceptionHandler SERDE_THREAD_EXCEPTION_HANDLER = (t, e)
        -> {
        logger.error("Error caught in thread", e);
        throw new SerializationDeserializationException("Error caught in thread");
    };
    private long beginTimeInNano;
    private List<Thread> threads = new ArrayList<>();
    private final String directoryPath;
    private boolean isSerialization;

    /**
     * @param directoryPath The path to the directory where the serialized data.
     */
    public ParallelArraySerDeUtils(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    /**
     * @return The name of the array being serialized or deserialized.
     */
    protected abstract String getArrayName();

    /**
     * @return The total size of the array being serialized or deserialized.
     */
    protected abstract int getArraySize();

    /**
     * @return The name of the class containing the large arrays being serialized or deserialized.
     */
    protected abstract String getClassName();

    /**
     * Serializes the given cell index of the array.
     *
     * @param objectOutputStream The output stream to write serialized data to.
     * @param index The index of the array to serialize.
     */
    protected abstract void serializeArrayCell(ObjectOutputStream objectOutputStream, int index)
        throws IOException;

    /**
     * Deserializes the given cell index of the array.
     *
     * @param objectInputStream The input stream to read serialized data from.
     * @param index The index of the array to deserialize.
     */
    protected abstract void deserializeArrayCell(ObjectInputStream objectInputStream, int index)
        throws IOException, ClassNotFoundException;

    /**
     * Starts serializing the large arrays in parallel.
     * <p>
     * Note: The method returns after starting the threads. A call to
     * {@link ParallelArraySerDeUtils#finishSerDe()} should be made to ensure that all threads
     * have finished their execution.
     */
    public void startSerialization() throws IOException {
        beginTimeInNano = System.nanoTime();
        logger.info(String.format("Serializing %s of %s.", getArrayName(), getClassName()));
        int numArrayIndicesPerFile;
        int numBlocks;
        if (getArraySize() < MAX_SERIALIZATION_THREADS) {
            numArrayIndicesPerFile = getArraySize();
            numBlocks = 1;
        } else {
            numArrayIndicesPerFile = getArraySize() / MAX_SERIALIZATION_THREADS;
            numBlocks = MAX_SERIALIZATION_THREADS;
        }
        ObjectOutputStream objectOutputStream = IOUtils.constructObjectOutputStream(
            SerDeUtils.getMetadataFilePath(directoryPath, getClassName(), getArrayName()));
        objectOutputStream.writeInt(numBlocks);
        objectOutputStream.close();
        for (int i = 0; i < numBlocks; i++) {
            int startIndexOfBlock = i * numArrayIndicesPerFile;
            int endIndexOfBlock = ((i + 1) == numBlocks) ? getArraySize() - 1 :
                (((i + 1) * numArrayIndicesPerFile) - 1);
            final int blockIndex = i;
            Thread serializerThread = new Thread(() -> serializeBlock(blockIndex, startIndexOfBlock,
                endIndexOfBlock));
            serializerThread.setUncaughtExceptionHandler(SERDE_THREAD_EXCEPTION_HANDLER);
            threads.add(serializerThread);
            serializerThread.start();
        }
    }

    /**
     * Starts deserializing the large arrays in parallel.
     * <p>
     * Note: The method returns after starting the threads. A call to
     * {@link ParallelArraySerDeUtils#finishSerDe()} should be made to ensure that all threads
     * have finished their execution.
     */
    public void startDeserialization() throws IOException, ClassNotFoundException {
        beginTimeInNano = System.nanoTime();
        logger.info(String.format("Deserializing %s of %s.", getArrayName(), getClassName()));
        ObjectInputStream objectInputStream = IOUtils.constructObjectInputStream(
            SerDeUtils.getMetadataFilePath(directoryPath, getClassName(), getArrayName()));
        int numBlocks = objectInputStream.readInt();
        objectInputStream.close();
        threads.clear();
        for (int i = 0; i < numBlocks; i++) {
            final int blockIndex = i;
            Thread deserializerThread = new Thread(() -> deserializeBlock(blockIndex));
            deserializerThread.setUncaughtExceptionHandler(SERDE_THREAD_EXCEPTION_HANDLER);
            threads.add(deserializerThread);
            deserializerThread.start();
        }
    }

    /**
     * Waits for all threads to finish their execution and calculates the execution time.
     */
    public void finishSerDe() throws IOException, InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
        logger.info(String.format("%s for array '%s' of class '%s' completed in %.3f ms.",
            isSerialization ? "Serialization" : "Deserialization", getArrayName(), getClassName(),
            IOUtils.getElapsedTimeInMillis(beginTimeInNano)));
    }

    /**
     * Thread method that serializes the given range of array indices.
     *
     * @param blockId The id of the array block being serialized.
     * @param startIndexOfBlock The start index of the array.
     * @param endIndexOfBlock The end index of the array.
     */
    private void serializeBlock(int blockId, int startIndexOfBlock, int endIndexOfBlock) {
        logger.info(String.format("Serializing block %s of %s", blockId, getArrayName()));
        long blockBeginTimeInNano = System.nanoTime();
        try {
            ObjectOutputStream objectOutputStream = IOUtils.constructObjectOutputStream(
                SerDeUtils.getArrayBlockFilePath(directoryPath, getClassName(), getArrayName(),
                    blockId));
            objectOutputStream.writeInt(startIndexOfBlock);
            objectOutputStream.writeInt(endIndexOfBlock);
            for (int i = startIndexOfBlock; i <= endIndexOfBlock; i++) {
                serializeArrayCell(objectOutputStream, i);
            }
            objectOutputStream.close();
            logger.info(String.format("%s block %s (from %s to %s) of %s serialized in %.3f ms.",
                getArrayName(), blockId, startIndexOfBlock, endIndexOfBlock, getClassName(),
                IOUtils.getElapsedTimeInMillis(blockBeginTimeInNano)));
        } catch (IOException e) {
            logger.error(String.format("Error serializing block %s of %s", blockId,
                getArrayName()), e);
            throw new SerializationDeserializationException("Error in serialization");
        }
    }

    /**
     * Thread method that deserializes the given range of array indices.
     *
     * @param blockId The id of the array block being deserialized.
     */
    private void deserializeBlock(int blockId) {
        logger.info(String.format("Deserializing block %s of %s", blockId, getArrayName()));
        long blockBeginTimeInNano = System.nanoTime();
        try {
            ObjectInputStream objectInputStream = IOUtils.constructObjectInputStream(
                SerDeUtils.getArrayBlockFilePath(directoryPath, getClassName(), getArrayName(),
                    blockId));
            int startIndexOfBlock = objectInputStream.readInt();
            int endIndexOfBlock = objectInputStream.readInt();
            for (int i = startIndexOfBlock; i <= endIndexOfBlock; i++) {
                deserializeArrayCell(objectInputStream, i);
            }
            objectInputStream.close();
            logger.info(String.format("%s block %s (from %s to %s) of %s deserialized in %.3f ms.",
                getArrayName(), blockId, startIndexOfBlock, endIndexOfBlock, getClassName(),
                IOUtils.getElapsedTimeInMillis(blockBeginTimeInNano)));
        } catch (IOException | ClassNotFoundException e) {
            logger.error(String.format("Error deserializing block %s of %s", blockId,
                getArrayName()), e);
            throw new SerializationDeserializationException("Error in deserialization");
        }
    }
}

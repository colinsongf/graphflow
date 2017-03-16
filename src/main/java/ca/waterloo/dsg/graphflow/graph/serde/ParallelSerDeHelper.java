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
 * Helper class to serialize and deserialize data in parallel for any given
 * {@link ParallelSerDeObjectContainer}, and print proper status and time output.
 */
public class ParallelSerDeHelper extends BaseSerDeHelper {

    private static final Logger logger = LogManager.getLogger(ParallelSerDeHelper.class);
    private static final int MAX_SERIALIZATION_THREADS = 10;
    private static Thread.UncaughtExceptionHandler SERDE_THREAD_EXCEPTION_HANDLER = (t, e) -> {
        logger.error("Error caught in thread", e);
        throw new SerializationDeserializationException("Error caught in thread");
    };
    private ParallelSerDeObjectContainer parallelSerDeObjectContainer;
    private long beginTimeInNano;
    private List<Thread> threads = new ArrayList<>();

    public ParallelSerDeHelper(String ioDirectoryPath, ParallelSerDeObjectContainer
        parallelSerDeObjectContainer) {
        this.ioDirectoryPath = ioDirectoryPath;
        this.parallelSerDeObjectContainer = parallelSerDeObjectContainer;
    }

    /**
     * Serializes data in parallel for the given {@code parallelSerDeObjectContainer}.
     * <p>
     * Note: The method returns after starting the threads. A call to
     * {@link ParallelSerDeHelper#finishSerDe()} should be made to ensure that all threads
     * have finished their execution.
     */
    public void startSerialization() throws IOException {
        beginTimeInNano = System.nanoTime();
        logger.info(String.format("Serializing %s of %s.", parallelSerDeObjectContainer
                .getObjectName(),
            parallelSerDeObjectContainer.getClassName()));
        int numArrayIndicesPerFile;
        int numFiles;
        if (parallelSerDeObjectContainer.getObjectSize() < MAX_SERIALIZATION_THREADS) {
            numArrayIndicesPerFile = parallelSerDeObjectContainer.getObjectSize();
            numFiles = 1;
        } else {
            numArrayIndicesPerFile = parallelSerDeObjectContainer.getObjectSize() /
                MAX_SERIALIZATION_THREADS;
            numFiles = MAX_SERIALIZATION_THREADS;
        }
        ObjectOutputStream objectOutputStream = IOUtils.constructObjectOutputStream(
            getMetadataFilePath(parallelSerDeObjectContainer.getClassName(),
                parallelSerDeObjectContainer.getObjectName()));
        objectOutputStream.writeInt(numFiles);
        objectOutputStream.close();
        threads.clear();
        for (int i = 0; i < numFiles; i++) {
            int startIndexOfBlock = i * numArrayIndicesPerFile;
            int endIndexOfBlock = ((i + 1) == numFiles) ? parallelSerDeObjectContainer
                .getObjectSize() - 1 :
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
     * Deserializes data in parallel for the given {@code parallelSerDeObjectContainer}.
     * <p>
     * Note: The method returns after starting the threads. A call to
     * {@link ParallelSerDeHelper#finishSerDe()} should be made to ensure that all threads
     * have finished their execution.
     */
    public void startDeserialization() throws IOException, ClassNotFoundException {
        beginTimeInNano = System.nanoTime();
        logger.info(String.format("Deserializing %s of %s.", parallelSerDeObjectContainer
                .getObjectName(),
            parallelSerDeObjectContainer.getClassName()));
        ObjectInputStream objectInputStream = IOUtils.constructObjectInputStream(
            getMetadataFilePath(parallelSerDeObjectContainer.getClassName(),
                parallelSerDeObjectContainer.getObjectName()));
        int numFiles = objectInputStream.readInt();
        objectInputStream.close();
        threads.clear();
        for (int i = 0; i < numFiles; i++) {
            final int blockIndex = i;
            Thread deserializerThread = new Thread(() -> deserializeBlock(blockIndex));
            deserializerThread.setUncaughtExceptionHandler(SERDE_THREAD_EXCEPTION_HANDLER);
            threads.add(deserializerThread);
            deserializerThread.start();
        }
    }

    /**
     * Waits for all threads to finish their execution, and calculates the execution time.
     */
    public void finishSerDe() throws IOException, InterruptedException {
        for (Thread thread : threads) {
            thread.join();
        }
        logger.info(String.format("SerDe for %s of %s completed in %.3f ms.",
            parallelSerDeObjectContainer.getObjectName(), parallelSerDeObjectContainer.
                getClassName(), IOUtils.getElapsedTimeInMillis(beginTimeInNano)));
    }

    /**
     * Thread method that serializes a block of data by calling
     * {@link ParallelSerDeObjectContainer#serializeBlock(ObjectOutputStream, int)} method of the
     * given {@code parallelSerDeObjectContainer}.
     *
     * @param blockIndex The index of the block being serialized.
     * @param startIndexOfBlock The start index of the block.
     * @param endIndexOfBlock The end index of the block.
     */
    private void serializeBlock(int blockIndex, int startIndexOfBlock, int endIndexOfBlock) {
        logger.info(String.format("Serializing block %s of %s", blockIndex,
            parallelSerDeObjectContainer.
                getObjectName()));
        long blockBeginTimeInNano = System.nanoTime();
        try {
            ObjectOutputStream objectOutputStream = IOUtils.constructObjectOutputStream(
                getBlockFilePath(parallelSerDeObjectContainer.getClassName(),
                    parallelSerDeObjectContainer.getObjectName(),
                    blockIndex));
            objectOutputStream.writeInt(startIndexOfBlock);
            objectOutputStream.writeInt(endIndexOfBlock);
            for (int i = startIndexOfBlock; i <= endIndexOfBlock; i++) {
                parallelSerDeObjectContainer.serializeBlock(objectOutputStream, i);
            }
            objectOutputStream.close();
            logger.info(String.format("%s block %s (from %s to %s) of %s serialized in %.3f ms.",
                parallelSerDeObjectContainer.getObjectName(), blockIndex, startIndexOfBlock,
                endIndexOfBlock, parallelSerDeObjectContainer.getClassName(), IOUtils.
                    getElapsedTimeInMillis(blockBeginTimeInNano)));
        } catch (IOException e) {
            logger.error("Error in serialization", e);
            throw new SerializationDeserializationException("Error in serialization");
        }
    }

    /**
     * Thread method that deserializes a block of data by calling
     * {@link ParallelSerDeObjectContainer#deserializeBlock(ObjectInputStream, int)} method of the
     * given {@code parallelSerDeObjectContainer}.
     *
     * @param blockIndex The index of the block being serialized.
     */
    private void deserializeBlock(int blockIndex) {
        logger.info(String.format("Deserializing block %s of %s", blockIndex,
            parallelSerDeObjectContainer.
                getObjectName()));
        long blockBeginTimeInNano = System.nanoTime();
        try {
            ObjectInputStream objectInputStream = IOUtils.constructObjectInputStream(
                getBlockFilePath(parallelSerDeObjectContainer.getClassName(),
                    parallelSerDeObjectContainer.getObjectName(),
                    blockIndex));
            int startIndexOfBlock = objectInputStream.readInt();
            int endIndexOfBlock = objectInputStream.readInt();
            for (int i = startIndexOfBlock; i <= endIndexOfBlock; i++) {
                parallelSerDeObjectContainer.deserializeBlock(objectInputStream, i);
            }
            objectInputStream.close();
            logger.info(String.format("%s block %s (from %s to %s) of %s deserialized in %.3f ms.",
                parallelSerDeObjectContainer.getObjectName(), blockIndex, startIndexOfBlock,
                endIndexOfBlock,
                parallelSerDeObjectContainer.getClassName(), IOUtils.getElapsedTimeInMillis(
                    blockBeginTimeInNano)));
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Error in deserialization", e);
            throw new SerializationDeserializationException("Error in deserialization");
        }
    }
}

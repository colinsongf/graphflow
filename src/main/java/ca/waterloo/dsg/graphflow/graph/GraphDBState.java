package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.exceptions.SerializationDeserializationException;
import ca.waterloo.dsg.graphflow.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Used to reset, serialize or deserialize {@link Graph}, {@link EdgeStore},
 * {@link VertexPropertyStore}, and {@link TypeAndPropertyKeyStore}.
 */
public class GraphDBState {

    public static final int MAX_SERIALIZATION_THREADS = 10;
    public static String FILE_PREFIX_SUFFIX = "graphflow_%s.data";
    public static final String BLOCK_FILE_SUBSTRING = "_block_";
    private static final String FILE_VERTEX_PROPERTY_STORE = "vertex_property_store";
    private static final String FILE_TYPE_PROPERTY_KEY_STORE = "type_property_key_store";
    private static final Logger logger = LogManager.getLogger(GraphDBState.class);
    public static Thread.UncaughtExceptionHandler SERIALIZE_DESERIALIZE_THREAD_EXCEPTION_HANDLER =
        (t, e) -> {
            logger.error("Error caught in thread", e);
            throw new SerializationDeserializationException("Error caught in thread");
        };

    /**
     * Resets {@link Graph}, {@link EdgeStore}, {@link VertexPropertyStore}, and
     * {@link TypeAndPropertyKeyStore}.
     */
    public static void reset() {
        Graph.reset();
        EdgeStore.reset();
        VertexPropertyStore.reset();
        TypeAndPropertyKeyStore.reset();
    }

    private static void serializeVertexPropertyOrTypeAndPropertyKeyStore(
        boolean serializeVertexPropertyStore, String outputDirectoryPath)
        throws IOException {
        String className = serializeVertexPropertyStore ? "VertexPropertyStore" :
            "TypeAndPropertyKeyStore";
        logger.info("Serializing " + className);
        long beginTimeInNano = System.nanoTime();
        ObjectOutputStream objectOutputStream = Util.constructObjectOutputStream(
            outputDirectoryPath + File.separator + String.format(
                FILE_PREFIX_SUFFIX, serializeVertexPropertyStore ? FILE_VERTEX_PROPERTY_STORE :
                    FILE_TYPE_PROPERTY_KEY_STORE));
        if (serializeVertexPropertyStore) {
            VertexPropertyStore.getInstance().serialize(objectOutputStream);
        } else {
            TypeAndPropertyKeyStore.getInstance().serialize(objectOutputStream);
        }
        objectOutputStream.close();
        logger.info(String.format("%s serialized in %.3f ms.", className, Util.
            getElapsedTimeInMicro(beginTimeInNano)));
    }

    private static void deserializeVertexPropertyOrTypeAndPropertyKeyStore(
        boolean deserializeVertexPropertyStore, String inputDirectoryPath) throws IOException,
        ClassNotFoundException {
        String className = deserializeVertexPropertyStore ? "VertexPropertyStore" :
            "TypeAndPropertyKeyStore";
        logger.info("Deserializing " + className);
        long beginTime = System.nanoTime();
        ObjectInputStream objectInputStream = Util.constructObjectInputStream(
            inputDirectoryPath + File.separator + String.format(
                FILE_PREFIX_SUFFIX, deserializeVertexPropertyStore ?
                    FILE_VERTEX_PROPERTY_STORE : FILE_TYPE_PROPERTY_KEY_STORE));
        if (deserializeVertexPropertyStore) {
            VertexPropertyStore.getInstance().deserialize(objectInputStream);
        } else {
            TypeAndPropertyKeyStore.getInstance().deserialize(objectInputStream);
        }
        objectInputStream.close();
        logger.info(String.format("%s deserialized in %.3f ms.", className, Util.
            getElapsedTimeInMicro(beginTime)));
    }

    /**
     * Serializes {@link Graph}, {@link EdgeStore}, {@link VertexPropertyStore}, and
     * {@link TypeAndPropertyKeyStore} and writes it to files inside the given directory.
     *
     * @param outputDirectoryPath The directory path to write the serialized data to.
     */
    public static void serialize(String outputDirectoryPath) {
        try {
            Graph.getInstance().serialize(outputDirectoryPath);
            EdgeStore.getInstance().serialize(outputDirectoryPath);
            serializeVertexPropertyOrTypeAndPropertyKeyStore(true /* VertexPropertyStore */,
                outputDirectoryPath);
            serializeVertexPropertyOrTypeAndPropertyKeyStore(false /* TypeAndPropertyKeyStore */,
                outputDirectoryPath);
        } catch (IOException | InterruptedException e) {
            logger.error("Error in serialization:", e);
            throw new SerializationDeserializationException("Error in serialization.");
        }
    }

    /**
     * Deserializes {@link Graph}, {@link EdgeStore}, {@link VertexPropertyStore}, and
     * {@link TypeAndPropertyKeyStore} from the files containing serialized data in the given
     * directory.
     *
     * @param inputDirectoryPath The input directory path to read serialized data from.
     */
    public static void deserialize(String inputDirectoryPath) {
        try {
            Graph.getInstance().deserialize(inputDirectoryPath);
            EdgeStore.getInstance().deserialize(inputDirectoryPath);
            deserializeVertexPropertyOrTypeAndPropertyKeyStore(true /* VertexPropertyStore */,
                inputDirectoryPath);
            deserializeVertexPropertyOrTypeAndPropertyKeyStore(false /* TypeAndPropertyKeyStore */,
                inputDirectoryPath);
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            GraphDBState.reset();
            logger.error("Error in deserialization:", e);
            throw new SerializationDeserializationException("Error in deserialization.");
        }
    }
}

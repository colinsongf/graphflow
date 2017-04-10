package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Helper class to serialize and deserialize the main file data of a {@link GraphflowSerializable}
 * class, and print proper status and time output.
 */
public class MainFileSerDeHelper {

    private static final Logger logger = LogManager.getLogger(MainFileSerDeHelper.class);

    /**
     * Serializes main file data by calling the {@link GraphflowSerializable#serializeMainFile
     * (ObjectOutputStream)} method of the given {@code graphflowSerializable} object.
     *
     * @param graphflowSerializable The {@link GraphflowSerializable} object to be serialized.
     * @param outputDirectoryPath The directory to write serialized data to.
     */
    public static void serialize(GraphflowSerializable graphflowSerializable,
        String outputDirectoryPath) throws IOException {
        long beginTimeInNano = System.nanoTime();
        logger.info(String.format("Serializing main file of %s.", graphflowSerializable.
            getMainFileNamePrefix()));
        ObjectOutputStream objectOutputStream = IOUtils.constructObjectOutputStream(
            SerDeUtils.getMainFilePath(outputDirectoryPath, graphflowSerializable.
                getMainFileNamePrefix()));
        graphflowSerializable.serializeMainFile(objectOutputStream);
        objectOutputStream.close();
        logger.info(String.format("Main file of %s serialized in %.3f ms.", graphflowSerializable.
            getMainFileNamePrefix(), IOUtils.getElapsedTimeInMillis(beginTimeInNano)));
    }

    /**
     * Deserializes main file data by calling the {@link GraphflowSerializable#deserializeMainFile
     * (ObjectInputStream)} method of the given {@code graphflowSerializable} object.
     *
     * @param graphflowSerializable The {@link GraphflowSerializable} object to be deserialized.
     * @param inputDirectoryPath The directory to read serialized data from.
     */
    public static void deserialize(GraphflowSerializable graphflowSerializable,
        String inputDirectoryPath) throws IOException, ClassNotFoundException {
        long beginTimeInNano = System.nanoTime();
        logger.info(String.format("Deserializing %s.", graphflowSerializable.
            getMainFileNamePrefix()));
        ObjectInputStream objectInputStream = IOUtils.constructObjectInputStream(
            SerDeUtils.getMainFilePath(inputDirectoryPath, graphflowSerializable.
                getMainFileNamePrefix()));
        graphflowSerializable.deserializeMainFile(objectInputStream);
        objectInputStream.close();
        logger.info(String.format("Main file of %s deserialized in %.3f ms.", graphflowSerializable.
            getMainFileNamePrefix(), IOUtils.getElapsedTimeInMillis(beginTimeInNano)));
    }
}

package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Helper class to serialize and deserialize data for any given {@link MainFileSerDe}, and print
 * proper status and time output.
 */
public class MainFileSerDeHelper extends BaseSerDeHelper {

    private static final Logger logger = LogManager.getLogger(MainFileSerDeHelper.class);

    public MainFileSerDeHelper(String ioDirectoryPath) {
        this.ioDirectoryPath = ioDirectoryPath;
    }

    /**
     * Serializes data by calling the {@link MainFileSerDe#serialize(ObjectOutputStream)} method
     * of the given {@code mainFileSerDe}.
     *
     * @param mainFileSerDe The {@link MainFileSerDe} used to serialize data to.
     */
    public void serialize(MainFileSerDe mainFileSerDe) throws IOException {
        long beginTimeInNano = System.nanoTime();
        logger.info(String.format("Serializing main file of %s.", mainFileSerDe.getFileNamePrefix
            ()));
        ObjectOutputStream objectOutputStream = IOUtils.constructObjectOutputStream(
            getMainFilePath(mainFileSerDe.getFileNamePrefix()));
        mainFileSerDe.serialize(objectOutputStream);
        objectOutputStream.close();
        logger.info(String.format("Main file of %s serialized in %.3f ms.", mainFileSerDe.
            getFileNamePrefix(), IOUtils.getElapsedTimeInMillis(beginTimeInNano)));
    }

    /**
     * Deserializes data by calling the {@link MainFileSerDe#deserialize(ObjectInputStream)} method
     * of the given {@code mainFileSerDe}.
     *
     * @param mainFileSerDe The {@link MainFileSerDe} used to deserialize data from.
     */
    public void deserialize(MainFileSerDe mainFileSerDe) throws IOException,
        ClassNotFoundException {
        long beginTimeInNano = System.nanoTime();
        logger.info(String.format("Deserializing %s.", mainFileSerDe.getFileNamePrefix()));
        ObjectInputStream objectInputStream = IOUtils.constructObjectInputStream(
            getMainFilePath(mainFileSerDe.getFileNamePrefix()));
        mainFileSerDe.deserialize(objectInputStream);
        objectInputStream.close();
        logger.info(String.format("Main file of %s deserialized in %.3f ms.", mainFileSerDe.
            getFileNamePrefix(), IOUtils.getElapsedTimeInMillis(beginTimeInNano)));
    }
}

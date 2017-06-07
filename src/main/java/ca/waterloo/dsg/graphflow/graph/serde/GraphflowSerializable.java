package ca.waterloo.dsg.graphflow.graph.serde;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Interface for serialization and deserialization.
 * Each Graphflow object that implements {@link GraphflowSerializable} has: (1) a main file into
 * which it serializes parts or all of its data; and (2) optionally other files to store large data
 * structures, such as large arrays, in multiple parts. The main files are serialized using
 * {@link MainFileSerDeHelper}, for which we also have the {@link #serializeMainFile
 * (ObjectOutputStream)}, {@link #deserializeMainFile(ObjectInputStream)}, and
 * {@link #getMainFileNamePrefix()} methods in this interface.
 */
public interface GraphflowSerializable {

    /**
     * Serializes data to the given {@code outputDirectoryPath}.
     *
     * @param outputDirectoryPath The directory to write serialized data to.
     */
    void serializeAll(String outputDirectoryPath) throws IOException, InterruptedException;

    /**
     * Deserializes data from the given {@code inputDirectoryPath}.
     *
     * @param inputDirectoryPath The directory to read serialized data from.
     */
    void deserializeAll(String inputDirectoryPath) throws IOException, ClassNotFoundException,
        InterruptedException;

    /**
     * Serializes the main file data and writes it to given {@code objectOutputStream}.
     * See {@link GraphflowSerializable} class comments for a more detailed explanation.
     *
     * @param objectOutputStream The {@link ObjectOutputStream} to write the serialized data to.
     */
    void serializeMainFile(ObjectOutputStream objectOutputStream) throws IOException;

    /**
     * Deserializes the main file data from the given {@code objectInputStream}.
     * See {@link GraphflowSerializable} class comments for a more detailed explanation.
     *
     * @param objectInputStream The {@link ObjectInputStream} to read serialized data from.
     */
    void deserializeMainFile(ObjectInputStream objectInputStream) throws IOException,
        ClassNotFoundException;

    /**
     * Returns the file name prefix for the main file.
     *
     * @return the file name prefix {@link String}.
     */
    String getMainFileNamePrefix();
}

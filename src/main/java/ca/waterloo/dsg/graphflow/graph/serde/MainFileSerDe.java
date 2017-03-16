package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Implemented by {@link Graph}, {@link EdgeStore}, {@link VertexPropertyStore}, and
 * {@link TypeAndPropertyKeyStore} to serialize and deserialize their respective main file data.
 */
public interface MainFileSerDe {

    /**
     * Returns the file name prefix for serialization and deserialization file names.
     *
     * @return the file name prefix {@link String}.
     */
    String getFileNamePrefix();

    /**
     * Serializes the main file data and writes it to given {@code objectOutputStream}.
     *
     * @param objectOutputStream The {@link ObjectOutputStream} to write the serialized data to.
     */
    void serialize(ObjectOutputStream objectOutputStream) throws IOException;

    /**
     * Deserializes the main file data from the given {@code objectInputStream}.
     *
     * @param objectInputStream The {@link ObjectInputStream} to read serialized data from.
     */
    void deserialize(ObjectInputStream objectInputStream) throws IOException,
        ClassNotFoundException;
}

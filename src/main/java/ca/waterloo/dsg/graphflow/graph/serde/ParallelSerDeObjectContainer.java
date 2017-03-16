package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Stores an object member of {@code Graph} or {@link EdgeStore} for its serialization and
 * deserialization in parallel.
 */
public interface ParallelSerDeObjectContainer {

    /**
     * @return The name of the class whose object is being serialized or deserialized.
     */
    String getClassName();

    /**
     * @return The size of the stored object.
     */
    int getObjectSize();

    /**
     * @return The name of the stored object.
     */
    String getObjectName();

    /**
     * Serializes the data present at the given {@code index} of the stored object.
     *
     * @param objectOutputStream The {@link ObjectOutputStream} to save the serialized data to.
     * @param index The index of the stored object to be serialized.
     */
    void serializeBlock(ObjectOutputStream objectOutputStream, int index) throws IOException;

    /**
     * Reads back the data for the given {@code index} of the stored object.
     *
     * @param objectInputStream The {@link ObjectInputStream} containing the serialized data.
     * @param index The index of the stored object where the data should be deserialized to.
     */
    void deserializeBlock(ObjectInputStream objectInputStream, int index) throws IOException,
        ClassNotFoundException;
}

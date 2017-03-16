package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Stores the {@code data} object of the {@link EdgeStore} for its serialization and
 * deserialization in parallel.
 */
public class EdgeStoreDataContainer implements ParallelSerDeObjectContainer {

    private byte[][][] data;

    public EdgeStoreDataContainer(byte[][][] data) {
        this.data = data;
    }

    @Override
    public String getClassName() {
        return "edge_store";
    }

    @Override
    public int getObjectSize() {
        return data.length;
    }

    @Override
    public String getObjectName() {
        return "data";
    }

    @Override
    public void serializeBlock(ObjectOutputStream objectOutputStream, int index)
        throws IOException {
        objectOutputStream.writeObject(data[index]);
    }

    @Override
    public void deserializeBlock(ObjectInputStream objectInputStream, int index)
        throws IOException, ClassNotFoundException {
        data[index] = (byte[][]) objectInputStream.readObject();
    }
}

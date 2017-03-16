package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Stores the {@code dataOffsets} object of the {@link EdgeStore} for its serialization and
 * deserialization in parallel.
 */
public class EdgeStoreDataOffsetsContainer implements ParallelSerDeObjectContainer {

    private int[][][] dataOffsets;

    public EdgeStoreDataOffsetsContainer(int[][][] dataOffsets) {
        this.dataOffsets = dataOffsets;
    }

    @Override
    public String getClassName() {
        return "edge_store";
    }

    @Override
    public int getObjectSize() {
        return dataOffsets.length;
    }

    @Override
    public String getObjectName() {
        return "data_offsets";
    }

    @Override
    public void serializeBlock(ObjectOutputStream objectOutputStream, int index)
        throws IOException {
        objectOutputStream.writeObject(dataOffsets[index]);
    }

    @Override
    public void deserializeBlock(ObjectInputStream objectInputStream, int index)
        throws IOException, ClassNotFoundException {
        dataOffsets[index] = (int[][]) objectInputStream.readObject();
    }
}

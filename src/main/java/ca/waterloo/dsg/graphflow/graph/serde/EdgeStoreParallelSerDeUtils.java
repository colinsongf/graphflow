package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Helper class to serialize and deserialize the {@code data} and {@code dataOffset} arrays of
 * {@link EdgeStore} in parallel.
 */
public class EdgeStoreParallelSerDeUtils extends ParallelArraySerDeUtils {

    private static final String ARRAY_NAME = "data_and_dataoffsets";
    private byte[][][] data;
    private int[][][] dataOffsets;
    private int size;

    /**
     * @param directoryPath Path to the directory where serialized data is stored.
     * @param data The {@code data} array object of {@link EdgeStore}.
     * @param dataOffsets The {@code dataOffsets} array object of {@link EdgeStore}.
     */
    public EdgeStoreParallelSerDeUtils(String directoryPath, byte[][][] data,
        int[][][] dataOffsets, int size) {
        super(directoryPath);
        this.data = data;
        this.dataOffsets = dataOffsets;
        this.size = size;
    }

    @Override
    protected String getArrayName() {
        return ARRAY_NAME;
    }

    @Override
    protected int getArraySize() {
        return size;
    }

    @Override
    protected String getClassName() {
        return EdgeStore.getInstance().getMainFileNamePrefix();
    }

    @Override
    protected void serializeArrayCell(ObjectOutputStream objectOutputStream, int index)
        throws IOException {
        objectOutputStream.writeObject(data[index]);
        objectOutputStream.writeObject(dataOffsets[index]);
    }

    @Override
    protected void deserializeArrayCell(ObjectInputStream objectInputStream, int index)
        throws IOException, ClassNotFoundException {
        data[index] = (byte[][]) objectInputStream.readObject();
        dataOffsets[index] = (int[][]) objectInputStream.readObject();
    }
}

package ca.waterloo.dsg.graphflow.graph;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GraphDBState {

    /**
     * Resets the state of {@link Graph}, {@link EdgeStore}, {@link VertexPropertyStore}, and
     * {@link TypeAndPropertyKeyStore}.
     */
    public static void reset() {
        Graph.getInstance().reset();
        EdgeStore.getInstance().reset();
        VertexPropertyStore.getInstance().reset();
        TypeAndPropertyKeyStore.getInstance().reset();
    }

    /**
     * Serializes the state of {@link Graph}, {@link EdgeStore}, {@link VertexPropertyStore}, and
     * {@link TypeAndPropertyKeyStore} and writes it to the given ObjectOutputStream.
     *
     * @param objectOutputStream The output stream to write the serialized data to.
     */
    public static void serialize(ObjectOutputStream objectOutputStream) throws IOException {
        Graph.getInstance().serialize(objectOutputStream);
        EdgeStore.getInstance().serialize(objectOutputStream);
        VertexPropertyStore.getInstance().serialize(objectOutputStream);
        TypeAndPropertyKeyStore.getInstance().serialize(objectOutputStream);
    }

    /**
     * Reads back the state of {@link Graph}, {@link EdgeStore}, {@link VertexPropertyStore}, and
     * {@link TypeAndPropertyKeyStore} from the given ObjectOutputStream containing serialized data.
     *
     * @param objectInputStream The input stream to read serialized data from.
     */
    public static void deserialize(ObjectInputStream objectInputStream) throws IOException,
        ClassNotFoundException {
        Graph.getInstance().deserialize(objectInputStream);
        EdgeStore.getInstance().deserialize(objectInputStream);
        VertexPropertyStore.getInstance().deserialize(objectInputStream);
        TypeAndPropertyKeyStore.getInstance().deserialize(objectInputStream);
    }
}

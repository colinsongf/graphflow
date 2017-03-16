package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.exceptions.SerializationDeserializationException;
import ca.waterloo.dsg.graphflow.graph.serde.MainFileSerDeHelper;
import ca.waterloo.dsg.graphflow.graph.serde.ParallelSerDeHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Used to reset, serialize or deserialize {@link Graph}, {@link EdgeStore},
 * {@link VertexPropertyStore}, and {@link TypeAndPropertyKeyStore}.
 */
public class GraphDBState {

    private static final Logger logger = LogManager.getLogger(GraphDBState.class);

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

    /**
     * Serializes {@link Graph}, {@link EdgeStore}, {@link VertexPropertyStore}, and
     * {@link TypeAndPropertyKeyStore} and writes it to files inside the given directory.
     *
     * @param outputDirectoryPath The directory path to write the serialized data to.
     */
    public static void serialize(String outputDirectoryPath) {
        try {
            MainFileSerDeHelper serDeHelper = new MainFileSerDeHelper(outputDirectoryPath);

            ParallelSerDeHelper graphAdjListsSerdeHelper = new ParallelSerDeHelper(
                outputDirectoryPath, Graph.getInstance().getGraphAdjListSerDeHelper());
            graphAdjListsSerdeHelper.startSerialization();
            serDeHelper.serialize(Graph.getInstance());

            ParallelSerDeHelper edgeStoreDataSerdeHelper = new ParallelSerDeHelper(
                outputDirectoryPath, EdgeStore.getInstance().getEdgeStoreDataSerDeHelper());
            edgeStoreDataSerdeHelper.startSerialization();
            ParallelSerDeHelper edgeStoreDataOffsetsSerdeHelper = new ParallelSerDeHelper(
                outputDirectoryPath, EdgeStore.getInstance().getEdgeStoreDataOffsetsSerDeHelper());
            edgeStoreDataOffsetsSerdeHelper.startSerialization();
            serDeHelper.serialize(EdgeStore.getInstance());

            serDeHelper.serialize(VertexPropertyStore.getInstance());
            serDeHelper.serialize(TypeAndPropertyKeyStore.getInstance());

            graphAdjListsSerdeHelper.finishSerDe();
            edgeStoreDataSerdeHelper.finishSerDe();
            edgeStoreDataOffsetsSerdeHelper.finishSerDe();
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
        GraphDBState.reset();
        try {
            MainFileSerDeHelper serDeHelper = new MainFileSerDeHelper(inputDirectoryPath);

            serDeHelper.deserialize(Graph.getInstance());
            ParallelSerDeHelper graphAdjListsSerdeHelper = new ParallelSerDeHelper(
                inputDirectoryPath, Graph.getInstance().getGraphAdjListSerDeHelper());
            graphAdjListsSerdeHelper.startDeserialization();

            serDeHelper.deserialize(EdgeStore.getInstance());
            ParallelSerDeHelper edgeStoreDataSerdeHelper = new ParallelSerDeHelper(
                inputDirectoryPath, EdgeStore.getInstance().getEdgeStoreDataSerDeHelper());
            edgeStoreDataSerdeHelper.startDeserialization();
            ParallelSerDeHelper edgeStoreDataOffsetsSerdeHelper = new ParallelSerDeHelper(
                inputDirectoryPath, EdgeStore.getInstance().getEdgeStoreDataOffsetsSerDeHelper());
            edgeStoreDataOffsetsSerdeHelper.startDeserialization();

            serDeHelper.deserialize(VertexPropertyStore.getInstance());
            serDeHelper.deserialize(TypeAndPropertyKeyStore.getInstance());

            graphAdjListsSerdeHelper.finishSerDe();
            edgeStoreDataSerdeHelper.finishSerDe();
            edgeStoreDataOffsetsSerdeHelper.finishSerDe();
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            GraphDBState.reset();
            logger.error("Error in deserialization:", e);
            throw new SerializationDeserializationException("Error in deserialization.");
        }
    }
}

package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.SortedAdjacencyList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Helper class to serialize and deserialize the adjacency list arrays of {@link Graph} in parallel.
 */
public class GraphParallelSerDeUtils extends ParallelArraySerDeUtils {

    private static final String ARRAY_NAME = "adjacency_lists";
    private SortedAdjacencyList[] forwardAdjLists;
    private SortedAdjacencyList[] backwardAdjLists;
    private int size;

    /**
     * @param directoryPath The path of the directory where serialized data is stored.
     * @param forwardAdjLists The {@code forwardAdjLists} object of {@link Graph}.
     * @param backwardAdjLists The {@code backwardAdjLists} object of {@link Graph}.
     * @param size The size of the adjacency lists.
     */
    public GraphParallelSerDeUtils(String directoryPath, SortedAdjacencyList[] forwardAdjLists,
        SortedAdjacencyList[] backwardAdjLists, int size) {
        super(directoryPath);
        this.forwardAdjLists = forwardAdjLists;
        this.backwardAdjLists = backwardAdjLists;
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
        return Graph.getInstance().getMainFileNamePrefix();
    }

    @Override
    protected void serializeArrayCell(ObjectOutputStream objectOutputStream, int index)
        throws IOException {
        forwardAdjLists[index].serialize(objectOutputStream);
        backwardAdjLists[index].serialize(objectOutputStream);
    }

    @Override
    protected void deserializeArrayCell(ObjectInputStream objectInputStream, int index)
        throws IOException, ClassNotFoundException {
        forwardAdjLists[index].deserialize(objectInputStream);
        backwardAdjLists[index].deserialize(objectInputStream);
    }
}

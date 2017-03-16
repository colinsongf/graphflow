package ca.waterloo.dsg.graphflow.graph.serde;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.SortedAdjacencyList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Stores the {@code forwardAdjLists} and {@code backwardAdjLists} objects of {@link Graph} for its
 * serialization and deserialization in parallel.
 */
public class GraphAdjListSerDeContainer implements ParallelSerDeObjectContainer {

    private int size;
    private SortedAdjacencyList[] forwardAdjLists;
    private SortedAdjacencyList[] backwardAdjLists;

    public GraphAdjListSerDeContainer(int size, SortedAdjacencyList[] forwardAdjLists,
        SortedAdjacencyList[] backwardAdjLists) {
        this.size = size;
        this.forwardAdjLists = forwardAdjLists;
        this.backwardAdjLists = backwardAdjLists;
    }

    @Override
    public String getClassName() {
        return "graph";
    }

    @Override
    public int getObjectSize() {
        return size;
    }

    @Override
    public String getObjectName() {
        return "adjacency_lists";
    }

    @Override
    public void serializeBlock(ObjectOutputStream objectOutputStream, int index)
        throws IOException {
        forwardAdjLists[index].serialize(objectOutputStream);
        backwardAdjLists[index].serialize(objectOutputStream);
    }

    @Override
    public void deserializeBlock(ObjectInputStream objectInputStream, int index)
        throws IOException, ClassNotFoundException {
        forwardAdjLists[index].deserialize(objectInputStream);
        backwardAdjLists[index].deserialize(objectInputStream);
    }
}

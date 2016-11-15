package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the Graph representation and provides utility methods.
 */
public class Graph {

    // Used to represent different versions of the graph.
    public enum GraphVersion {
        // Graph formed after finalizing all additions and deletions.
        CURRENT,
        // Graph of only scheduled additions.
        DIFF_PLUS,
        // Graph of only scheduled deletions.
        DIFF_MINUS,
        // Graph formed after merging the scheduled additions and deletions with the {@code
        // CURRENT} graph.
        MERGED
    }

    public enum EdgeDirection {
        FORWARD,
        REVERSE
    }

    private static final Logger logger = LogManager.getLogger(Graph.class);
    private static final int DEFAULT_GRAPH_SIZE = 10;

    // Store the highest vertex ID of the current graph.
    private int highestVertexId = -1;
    // Store the highest vertex ID found in edges scheduled for addition to the graph.
    private int temporaryHighestVertexId = -1;

    // Adjacency lists for the current graph.
    private SortedIntArrayList[] outgoingAdjLists;
    private SortedIntArrayList[] incomingAdjLists;
    // Edges for additions and deletions.
    private List<int[]> diffPlusEdges;
    private List<int[]> diffMinusEdges;
    // Adjacency lists for the vertices affected by additions and deletions.
    private Map<Integer, SortedIntArrayList> mergedOutgoingAdjLists;
    private Map<Integer, SortedIntArrayList> mergedIncomingAdjLists;

    public Graph() {
        this(DEFAULT_GRAPH_SIZE);
    }

    public Graph(int vertexLength) {
        outgoingAdjLists = new SortedIntArrayList[vertexLength];
        incomingAdjLists = new SortedIntArrayList[vertexLength];
        diffPlusEdges = new ArrayList<>();
        diffMinusEdges = new ArrayList<>();
        mergedOutgoingAdjLists = new HashMap<>();
        mergedIncomingAdjLists = new HashMap<>();
    }

    /**
     * Returns the number of vertices in the current graph.
     *
     * @return int
     */
    public int getVertexCount() {
        return highestVertexId + 1;
    }

    /**
     * Add an edge temporarily to the graph.
     */
    public void addEdge(int fromVertex, int toVertex) {
        if (fromVertex <= highestVertexId && outgoingAdjLists[fromVertex] != null &&
            outgoingAdjLists[fromVertex].search(toVertex) != -1) {
            return;   // edge already present. Skip.
        }
        // Save the edge to the diff graph for additions.
        diffPlusEdges.add(new int[]{fromVertex, toVertex});
        temporaryHighestVertexId = Integer.max(temporaryHighestVertexId,
            Integer.max(fromVertex, toVertex));
        // Create updated outgoing adjacency list for the vertex.
        updateLists(true, fromVertex, toVertex, mergedOutgoingAdjLists, outgoingAdjLists);
        // Create updated incoming adjacency list for the vertex.
        updateLists(true, toVertex, fromVertex, mergedIncomingAdjLists, incomingAdjLists);
    }

    /**
     * Delete an edge temporarily from the graph.
     */
    public void deleteEdge(int fromVertex, int toVertex) {
        if (fromVertex <= highestVertexId &&
            (outgoingAdjLists[fromVertex] == null || outgoingAdjLists[fromVertex].search(
                toVertex) == -1)) {
            return;   // edge not present. Skip.
        }
        // Save the edge to the diff graph for deletions.
        diffMinusEdges.add(new int[]{fromVertex, toVertex});
        // Add edge to the outgoing adjacency list for the merged graph.
        updateLists(false, fromVertex, toVertex, mergedOutgoingAdjLists, outgoingAdjLists);
        // Add edge to the incoming adjacency list for the merged graph.
        updateLists(false, toVertex, fromVertex, mergedIncomingAdjLists, incomingAdjLists);
    }

    /**
     * Perform addition or deletion of the edge {@code fromVertex}->{@code toVertex} from the
     * lists {@code mergedList} and {@code currentAdjLists}.
     *
     * @param isAddition {@code true} for addition, {@code false} for deletion.
     */
    private void updateLists(boolean isAddition, int fromVertex, int toVertex,
        Map<Integer, SortedIntArrayList> mergedList, SortedIntArrayList[] currentAdjLists) {
        if (mergedList.containsKey(fromVertex)) {
            if (isAddition) {
                mergedList.get(fromVertex).add(toVertex);
            } else {
                mergedList.get(fromVertex).removeElement(toVertex);
            }
        } else {
            SortedIntArrayList updatedList = new SortedIntArrayList();
            if (fromVertex <= highestVertexId && currentAdjLists[fromVertex] != null) {
                updatedList.addAll(currentAdjLists[fromVertex].toArray());
            }
            if (isAddition) {
                updatedList.add(toVertex);
            } else {
                updatedList.removeElement(toVertex);
            }
            mergedList.put(fromVertex, updatedList);
        }
    }

    /**
     * Make the temporary addition and deletion operations on the graph permanent.
     */
    public void finalizeChanges() {
        ensureCapacity(temporaryHighestVertexId + 1);
        highestVertexId = temporaryHighestVertexId;
        for (int vertex : mergedOutgoingAdjLists.keySet()) {
            outgoingAdjLists[vertex] = mergedOutgoingAdjLists.get(vertex);
        }
        for (int vertex : mergedIncomingAdjLists.keySet()) {
            incomingAdjLists[vertex] = mergedIncomingAdjLists.get(vertex);
        }
        diffPlusEdges = new ArrayList<>();
        diffMinusEdges = new ArrayList<>();
        mergedOutgoingAdjLists = new HashMap<>();
        mergedIncomingAdjLists = new HashMap<>();
    }

    /**
     * Get the list of current edges.
     */
    public int[][] getCurrentEdges(EdgeDirection edgeDirection) {
        List<int[]> list = new ArrayList<>();
        SortedIntArrayList[] committedList =
            edgeDirection == EdgeDirection.FORWARD ? outgoingAdjLists : incomingAdjLists;
        for (int fromVertex = 0; fromVertex <= highestVertexId; fromVertex++) {
            if (committedList[fromVertex] == null) {
                continue;
            }
            SortedIntArrayList adjList;
            adjList = committedList[fromVertex];
            for (int toVertex : adjList.toArray()) {
                list.add(new int[]{fromVertex, toVertex});
            }
        }
        int size = list.size();
        int[][] edges = new int[size][];
        for (int i = 0; i < size; i++) {
            edges[i] = list.get(i);
        }
        return edges;
    }

    /**
     * Returns an array of outgoing or incoming adjacency lists for the given vertex.
     */
    public SortedIntArrayList getAdjacencyList(int vertexIndex, EdgeDirection edgeDirection,
        GraphVersion graphVersion) {
        if (vertexIndex > highestVertexId || graphVersion == GraphVersion.DIFF_MINUS ||
            graphVersion == GraphVersion.DIFF_PLUS) {
            return new SortedIntArrayList();
        }
        SortedIntArrayList result;
        if (edgeDirection == EdgeDirection.FORWARD) {
            if (graphVersion == GraphVersion.MERGED && mergedOutgoingAdjLists.containsKey(
                vertexIndex)) {
                result = mergedOutgoingAdjLists.get(vertexIndex);
            } else {
                if (outgoingAdjLists[vertexIndex] == null) {
                    outgoingAdjLists[vertexIndex] = new SortedIntArrayList();
                }
                result = outgoingAdjLists[vertexIndex];
            }
        } else {
            if (graphVersion == GraphVersion.MERGED && mergedIncomingAdjLists.containsKey(
                vertexIndex)) {
                result = mergedIncomingAdjLists.get(vertexIndex);
            } else {
                if (incomingAdjLists[vertexIndex] == null) {
                    incomingAdjLists[vertexIndex] = new SortedIntArrayList();
                }
                result = incomingAdjLists[vertexIndex];
            }
        }
        return result;
    }

    /**
     * Checks if the current capacity exceeds size and increases the capacity if it doesn't.
     */
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = outgoingAdjLists.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            outgoingAdjLists = Arrays.copyOf(outgoingAdjLists, newCapacity);
            incomingAdjLists = Arrays.copyOf(incomingAdjLists, newCapacity);
        }
    }

    /**
     * Convert the graph to a string.
     */
    @Override
    public String toString() {
        return "Outgoing Adj Lists:" + System.lineSeparator() + convertAdjListsToString(
            outgoingAdjLists) + "Incoming Adj Lists:" + System.lineSeparator() +
            convertAdjListsToString(incomingAdjLists);
    }

    /**
     * Converts the set of adjacency lists in the given direction to a {@code string}.
     */
    private String convertAdjListsToString(SortedIntArrayList[] adjLists) {
        StringBuilder adjString = new StringBuilder();
        for (int index = 0; index <= highestVertexId; index++) {
            SortedIntArrayList adjList = adjLists[index];
            adjString.append(index).append(": ");
            adjString.append(adjList == null ? "" : adjList.toString());
            adjString.append(System.lineSeparator());
        }
        return adjString.toString();
    }
}

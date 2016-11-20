package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

/**
 * Encapsulates the Graph representation and provides utility methods.
 */
public class Graph {

    // Used to represent different versions of the graph.
    public enum GraphVersion {
        // Graph formed after making all additions and deletions permanent.
        PERMANENT,
        // Graph of only the temporary additions.
        DIFF_PLUS,
        // Graph of only the temporary deletions.
        DIFF_MINUS,
        // Graph formed after merging the temporary additions and deletions with the permanent
        // graph to reflect the new state of the graph that will be formed after making the
        // changes permanent.
        MERGED
    }

    /**
     * This is used for both identifying the edge direction in adjacency lists in the graph
     * representation and the direction when evaluating MATCH queries using Generic Join and
     * the direction of traversals in SHORTEST PATH queries.
     */
    public enum Direction {
        FORWARD(true),
        BACKWARD(false);

        private final boolean isForward;

        Direction(boolean isForward) {
            this.isForward = isForward;
        }

        public boolean getBooleanValue() {
            return isForward;
        }
    }

    private static final Logger logger = LogManager.getLogger(Graph.class);
    private static final int DEFAULT_GRAPH_SIZE = 10;

    // Stores the highest vertex ID of the permanent graph.
    private int highestPermanentVertexId = -1;
    // Stores the highest vertex ID present among all vertices in the permanent graph and the
    // temporary vertices to be added. This is used when permanently applying the temporary changes
    // to the graph to decide if the adjacency list arrays need resizing to accommodate higher
    // vertex IDs being added.
    private int highestMergedVertexId = -1;

    // Adjacency lists for the permanent graph.
    private SortedIntArrayList[] forwardAdjLists;
    private SortedIntArrayList[] backwardAdjLists;
    // Edges for additions and deletions.
    private List<int[]> diffPlusEdges;
    private List<int[]> diffMinusEdges;
    // Updated adjacency lists for the vertices affected by additions and deletions.
    private Map<Integer, SortedIntArrayList> mergedForwardAdjLists;
    private Map<Integer, SortedIntArrayList> mergedBackwardAdjLists;

    public Graph() {
        this(DEFAULT_GRAPH_SIZE);
    }

    public Graph(int vertexLength) {
        forwardAdjLists = new SortedIntArrayList[vertexLength];
        backwardAdjLists = new SortedIntArrayList[vertexLength];
        diffPlusEdges = new ArrayList<>();
        diffMinusEdges = new ArrayList<>();
        mergedForwardAdjLists = new HashMap<>();
        mergedBackwardAdjLists = new HashMap<>();
    }

    /**
     * Returns the number of vertices in the permanent graph.
     *
     * @return The number of permanent vertices.
     */
    public int getVertexCount() {
        return highestPermanentVertexId + 1;
    }

    /**
     * Adds an edge temporarily to the graph. A call to {@link #finalizeChanges()} is required to
     * make the changes permanent.
     *
     * @param fromVertex The starting vertex ID for the edge.
     * @param toVertex The ending vertex ID for the edge.
     */
    public void addEdgeTemporarily(int fromVertex, int toVertex) {
        if (fromVertex <= highestPermanentVertexId && toVertex <= highestPermanentVertexId &&
            null != forwardAdjLists[fromVertex] && -1 != forwardAdjLists[fromVertex]
            .search(toVertex)) {
            return; // Edge is already present in the permanent graph. Skip.
        }
        addOrDeleteEdgeTemporarily(true /* addition */, fromVertex, toVertex, diffPlusEdges);
        highestMergedVertexId = Integer.max(highestMergedVertexId, Integer.max(fromVertex,
            toVertex));
    }

    /**
     * Deletes an edge temporarily from the graph. A call to {@link #finalizeChanges()} is required
     * to make the changes permanent.
     *
     * @param fromVertex The starting vertex ID for the edge.
     * @param toVertex The ending vertex ID for the edge.
     */
    public void deleteEdgeTemporarily(int fromVertex, int toVertex) {
        // Check whether the edge exists in either the MERGED or the PERMANENT graph.
        if (!((fromVertex <= highestMergedVertexId) && ((toVertex <= highestMergedVertexId) &&
            mergedForwardAdjLists.containsKey(fromVertex) && (-1 != mergedForwardAdjLists.get
            (fromVertex).search(toVertex)))) && !((fromVertex <= highestPermanentVertexId) &&
            (toVertex <= highestPermanentVertexId) && (null != forwardAdjLists[fromVertex]) &&
            (-1 != forwardAdjLists[fromVertex].search(toVertex)))) {
            // The edge does not exist.
            throw new NoSuchElementException("The edge " + fromVertex + "->" + toVertex +
                " does not exist.");
        }
        addOrDeleteEdgeTemporarily(false /* deletion */, fromVertex, toVertex, diffMinusEdges);
    }

    /**
     * Adds or deletes an edge temporarily from the graph.
     *
     * @param isAddition {@code true} for addition, {@code false} for deletion.
     * @param fromVertex The starting vertex ID for the edge.
     * @param toVertex The ending vertex ID for the edge.
     * @param diffEdges The diff list of edges to modify.
     */
    private void addOrDeleteEdgeTemporarily(boolean isAddition, int fromVertex, int toVertex,
        List<int[]> diffEdges) {
        // Saves the edge to the diffEdges list.
        diffEdges.add(new int[]{fromVertex, toVertex});
        // Create the updated forward adjacency list for the vertex.
        updateMergedAdjLists(isAddition, fromVertex, toVertex, mergedForwardAdjLists,
            forwardAdjLists);
        // Create the updated backward adjacency list for the vertex.
        updateMergedAdjLists(isAddition, toVertex, fromVertex, mergedBackwardAdjLists,
            backwardAdjLists);
    }

    /**
     * Temporarily performs an addition or deletion of the edge {@code fromVertex}->{@code toVertex}
     * by updating the list {@code mergedAdjLists}, using {@code permanentAdjLists} if required.
     *
     * @param isAddition {@code true} for addition, {@code false} for deletion.
     * @param fromVertex The starting vertex ID for the edge.
     * @param toVertex The ending vertex ID for the edge.
     * @param mergedAdjLists The merged adjacency lists to modify.
     * @param permanentAdjLists The permanent adjacency list, used if {@code fromVertex} does not
     * already exist in {@code mergedAdjLists}.
     */
    private void updateMergedAdjLists(boolean isAddition, int fromVertex, int toVertex,
        Map<Integer, SortedIntArrayList> mergedAdjLists, SortedIntArrayList[] permanentAdjLists) {
        if (mergedAdjLists.containsKey(fromVertex)) {
            if (isAddition) {
                mergedAdjLists.get(fromVertex).add(toVertex);
            } else {
                mergedAdjLists.get(fromVertex).removeElement(toVertex);
            }
        } else {
            SortedIntArrayList updatedList = new SortedIntArrayList();
            if (fromVertex <= highestPermanentVertexId && permanentAdjLists[fromVertex] != null) {
                // Copy the adjacency list from the permanent graph to the merged graph.
                updatedList.addAll(permanentAdjLists[fromVertex].toArray());
            }
            if (isAddition) {
                updatedList.add(toVertex);
            } else {
                updatedList.removeElement(toVertex);
            }
            mergedAdjLists.put(fromVertex, updatedList);
        }
    }

    /**
     * Permanently applies the temporary additions and deletions that have been applied, using the
     * {@link #addEdgeTemporarily(int, int)} and {@link #deleteEdgeTemporarily(int, int)} methods,
     * since the previous call to {@code finalizeChanges()}.
     */
    public void finalizeChanges() {
        // Increase the size of the adjacency lists if newly added edges have a higher
        // vertex ID
        // as captured by {@code highestMergedVertexId}.
        // TODO: handle very large vertex ids.
        ensureCapacity(highestMergedVertexId + 1);
        highestPermanentVertexId = highestMergedVertexId;
        // Replace the adjacency lists of permanent vertices with the merged ones.
        for (int vertex : mergedForwardAdjLists.keySet()) {
            forwardAdjLists[vertex] = mergedForwardAdjLists.get(vertex);
        }
        for (int vertex : mergedBackwardAdjLists.keySet()) {
            backwardAdjLists[vertex] = mergedBackwardAdjLists.get(vertex);
        }
        // Reset the diff and merged graph states.
        diffPlusEdges.clear();
        diffMinusEdges.clear();
        mergedForwardAdjLists.clear();
        mergedBackwardAdjLists.clear();
    }

    /**
     * Returns an iterator over the edges of the graph for the given {@code graphVersion} and
     * {@code direction}. The order of the returned edges is first in increasing order of source
     * vertex IDs and then, for the edges with the same source vertex ID, in increasing order of
     * destination IDs.
     *
     * @param graphVersion The {@code GraphVersion} for which list of edges is required.
     * @param direction The {@code Direction} of the edges.
     * @return An iterator to the list of edges for the given {@code graphVersion} and
     * {@code direction}.
     */
    public Iterator<int[]> getEdgesIterator(GraphVersion graphVersion, Direction direction) {
        if ((GraphVersion.DIFF_PLUS == graphVersion || GraphVersion.DIFF_MINUS == graphVersion) &&
            Direction.BACKWARD == direction) {
            throw new UnsupportedOperationException("Getting edges for the DIFF_PLUS "
                + "or DIFF_MINUS graph in BACKWARD direction is not supported.");
        }
        if (GraphVersion.DIFF_PLUS == graphVersion) {
            return Collections.unmodifiableCollection(diffPlusEdges).iterator();
        } else if (GraphVersion.DIFF_MINUS == graphVersion) {
            return Collections.unmodifiableCollection(diffMinusEdges).iterator();
        } else {
            SortedIntArrayList[] permanentAdjacencyLists;
            Map<Integer, SortedIntArrayList> mergedAdjLists;
            if (Direction.FORWARD == direction) {
                permanentAdjacencyLists = forwardAdjLists;
                mergedAdjLists = mergedForwardAdjLists;
            } else {
                permanentAdjacencyLists = backwardAdjLists;
                mergedAdjLists = mergedBackwardAdjLists;
            }
            int lastVertexId = (GraphVersion.MERGED == graphVersion) ? highestMergedVertexId :
                highestPermanentVertexId;

            if (0 > lastVertexId) {
                // Handle the case when the graph is empty.
                logger.warn("A getEdgesIterator(" + graphVersion + "," + direction +
                    ") call received when the graph was empty.");
                return Collections.<int[]>emptyList().iterator();
            }
            return new EdgesIterator(graphVersion, permanentAdjacencyLists, mergedAdjLists,
                lastVertexId);
        }
    }

    /**
     * @param vertexId The vertex ID whose adjacency list is required.
     * @param direction The {@code Direction} of the adjacency list.
     * @param graphVersion The {@code GraphVersion} to consider.
     * @return The adjacency list for the vertex with the given {@code vertexId}, for the given
     * {@code graphVersion} and {@code direction}.
     */
    public SortedIntArrayList getAdjacencyList(int vertexId, Direction direction,
        GraphVersion graphVersion) {
        if (vertexId > highestPermanentVertexId) {
            throw new NoSuchElementException(vertexId + " does not exist.");
        } else if (GraphVersion.DIFF_MINUS == graphVersion || GraphVersion.DIFF_PLUS ==
            graphVersion) {
            throw new UnsupportedOperationException("Getting adjacency lists from the DIFF_PLUS "
                + "or DIFF_MINUS graph is not supported.");
        }
        SortedIntArrayList[] permanentAdjList;
        Map<Integer, SortedIntArrayList> mergedAdjLists;
        if (Direction.FORWARD == direction) {
            permanentAdjList = forwardAdjLists;
            mergedAdjLists = mergedForwardAdjLists;
        } else {
            permanentAdjList = backwardAdjLists;
            mergedAdjLists = mergedBackwardAdjLists;
        }
        if (GraphVersion.MERGED == graphVersion && mergedAdjLists.containsKey(vertexId)) {
            // Use the adjacency list of the merged graph.
            return mergedAdjLists.get(vertexId);
        } else {
            if (null == permanentAdjList[vertexId]) {
                permanentAdjList[vertexId] = new SortedIntArrayList();
            }
            // Use the adjacency list of the permanent graph.
            return permanentAdjList[vertexId];
        }
    }

    /**
     * Checks if the permanent capacity exceeds {@code minCapacity} and increases the capacity if it
     * doesn't.
     *
     * @param minCapacity The minimum required size of the arrays.
     */
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = forwardAdjLists.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            forwardAdjLists = Arrays.copyOf(forwardAdjLists, newCapacity);
            backwardAdjLists = Arrays.copyOf(backwardAdjLists, newCapacity);
        }
    }

    /**
     * Convert the graph to a {@code String}.
     *
     * @return The {@code String} representation of the graph.
     */
    @Override
    public String toString() {
        String graph = "Forward Adjacency Lists:" + System.lineSeparator() +
            convertPermanentAdjListsToString(forwardAdjLists);
        graph += "Backward Adjacency Lists:" + System.lineSeparator() +
            convertPermanentAdjListsToString(backwardAdjLists);
        graph += "Temporarily added edges: " + convertDiffEdgesToString(diffPlusEdges) + System
            .lineSeparator();
        graph += "Temporarily deleted edges: " + convertDiffEdgesToString(diffMinusEdges) +
            System.lineSeparator();
        graph += "Merged Forward Adjacency Lists: " + convertMergedAdjListsToString
            (mergedForwardAdjLists);
        graph += "Merged Backward Adjacency Lists: " + convertMergedAdjListsToString
            (mergedBackwardAdjLists);
        graph += "highestPermanentVertexId = " + highestPermanentVertexId + System.lineSeparator();
        graph += "highestMergedVertexId = " + highestMergedVertexId + System.lineSeparator();
        return graph;
    }

    /**
     * Converts the permanent adjacency lists {@code permanentAdjLists} to a {@code String}.
     *
     * @param permanentAdjLists The permanent adjacency list to convert to a {@code String}.
     * @return The{@code String} representation of {@code permanentAdjLists}.
     */
    private String convertPermanentAdjListsToString(SortedIntArrayList[] permanentAdjLists) {
        StringBuilder adjString = new StringBuilder();
        for (int index = 0; index <= highestPermanentVertexId; index++) {
            SortedIntArrayList adjList = permanentAdjLists[index];
            adjString.append(index).append(": ");
            adjString.append((null == adjList) ? "[]" : adjList.toString());
            adjString.append(System.lineSeparator());
        }
        return adjString.toString();
    }

    /**
     * Converts the merged adjacency lists {@code mergedAdjLists} to a {@code String}.
     *
     * @param mergedAdjLists The merged adjacency list to convert to a {@code String}.
     * @return The {@code String} representation of {@code permanentAdjLists}.
     */
    private String convertMergedAdjListsToString(Map<Integer, SortedIntArrayList> mergedAdjLists) {
        StringBuilder adjString = new StringBuilder();
        for (int index : mergedAdjLists.keySet()) {
            SortedIntArrayList adjList = mergedAdjLists.get(index);
            adjString.append(index).append(": ");
            adjString.append((null == adjList) ? "[]" : adjList.toString());
            adjString.append(System.lineSeparator());
        }
        return adjString.toString();
    }

    /**
     * Converts the list of diff edges {@code diffEdges} to a {@code String}.
     *
     * @param diffEdges The list of diff edges to convert to a {@code String}.
     * @return The {@code String} representation of {@code diffEdges}.
     */
    private String convertDiffEdgesToString(List<int[]> diffEdges) {
        StringJoiner stringJoiner = new StringJoiner(",");
        for (int[] edge : diffEdges) {
            stringJoiner.add(Arrays.toString(edge));
        }
        return "[" + stringJoiner.toString() + "]";
    }
}

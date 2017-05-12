package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.serde.GraphParallelSerDeUtils;
import ca.waterloo.dsg.graphflow.graph.serde.GraphflowSerializable;
import ca.waterloo.dsg.graphflow.graph.serde.MainFileSerDeHelper;
import ca.waterloo.dsg.graphflow.graph.serde.ParallelArraySerDeUtils;
import ca.waterloo.dsg.graphflow.util.ArrayUtils;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.LongArrayList;
import ca.waterloo.dsg.graphflow.util.ShortArrayList;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
public class Graph implements GraphflowSerializable {

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
     * representation and the direction when evaluating MATCH queries using Generic Join and the
     * direction of traversals in SHORTEST PATH queries.
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
    private static final int DEFAULT_GRAPH_SIZE = 2;
    private static Graph INSTANCE = new Graph();
    // Stores the highest vertex ID of the permanent graph.
    private int highestPermanentVertexId = -1;
    // Adjacency lists for the permanent graph, containing both the neighbour vertex IDs and edge
    // type IDs to those neighbours.
    private SortedAdjacencyList[] forwardAdjLists = new SortedAdjacencyList[DEFAULT_GRAPH_SIZE];
    private SortedAdjacencyList[] backwardAdjLists = new SortedAdjacencyList[DEFAULT_GRAPH_SIZE];
    // Stores the highest vertex ID present among all vertices in the permanent graph and the
    // temporary vertices to be added. This is used when permanently applying the temporary changes
    // to the graph to decide if the adjacency list arrays need resizing to accommodate higher
    // vertex IDs being added.
    private int highestMergedVertexId = -1;
    private ShortArrayList vertexTypes = new ShortArrayList();
    // Edges for additions and deletions.
    private List<int[]> diffPlusEdges = new ArrayList<>();
    private List<int[]> diffMinusEdges = new ArrayList<>();
    // Each edge at index i of {@link diffPlusEdges} or {@link diffMinusEdges} has a type at
    // index i of {@link diffPlusEdgeTypes} or {@link diffMinusEdgeTypes}.
    private ShortArrayList diffPlusEdgeTypes = new ShortArrayList();
    private ShortArrayList diffMinusEdgeTypes = new ShortArrayList();
    private LongArrayList diffPlusEdgeIds = new LongArrayList();
    private LongArrayList diffMinusEdgeIds = new LongArrayList();
    // Updated adjacency lists for the vertices affected by additions and deletions.
    private Map<Integer, SortedAdjacencyList> mergedForwardAdjLists = new HashMap<>();
    private Map<Integer, SortedAdjacencyList> mergedBackwardAdjLists = new HashMap<>();

    private Graph() {
        initializeSortedAdjacencyLists(0 /* starting index */, DEFAULT_GRAPH_SIZE);
    }

    /**
     * @return The highest permanent vertex ID .
     */
    public int getVertexCount() {
        return highestPermanentVertexId + 1;
    }

    /***
     * @return The highest merged vertex ID in the graph.
     */
    int getHighestVertexId() {
        return highestMergedVertexId;
    }

    /**
     * Adds a vertex to the graph.
     * <p>
     * Warning: Currently, as part of this call, we will overwrite the current vertex type and
     * properties of {@code vertexId} with {@code vertexType} and {@code vertexProperties},
     * respectively.
     * <p>
     * If the properties passed are {@code null}, no changes to the vertex properties occur.
     * If a vertex u has type T, callers should always call this method with type T for u to keep
     * u's type.
     *
     * @param vertexId The vertex ID.
     * @param vertexType The type of {@code vertexId}.
     * @param vertexProperties The properties of {@code vertexId}.
     */
    public void addVertex(int vertexId, short vertexType,
        Map<Short, Pair<DataType, String>> vertexProperties) {
        vertexTypes.set(vertexId, vertexType);
        VertexPropertyStore.getInstance().set(vertexId, vertexProperties);
        highestPermanentVertexId = Integer.max(highestPermanentVertexId, vertexId);
        highestMergedVertexId = Integer.max(highestMergedVertexId, vertexId);
        ensureCapacity(highestMergedVertexId + 1);
    }

    /**
     * Adds an edge temporarily to the graph. A call to {@link #finalizeChanges()} is required to
     * make the changes permanent.
     * <p>
     * Note: If an edge to {@code toVertex} with the given {@code edgeType} already exists, this
     * method returns without doing anything.
     * <p>
     * Warning: Currently, as part of this call, we will overwrite the current vertex types and
     * properties of {@code fromVertex} and {@code toVertex} with {@code fromVertexType}, {@code
     * toVertexType}, {@code toVertexProperties}, and {@code fromVertexProperties}, respectively.
     * If the properties passed are {@code null}, no changes to the vertex properties occur.
     * Otherwise, the vertex properties are overwritten.
     * Warning: This method makes the types and properties permanent.
     * If a vertex u has type T, callers should always call this method with type T for u to keep
     * u's type.
     *
     * @param fromVertex The source vertex ID for the edge.
     * @param toVertex The destination vertex ID for the edge.
     * @param fromVertexType The type of {@code fromVertex}.
     * @param toVertexType The type of {@code toVertex}.
     * @param fromVertexProperties The properties of {@code fromVertex}.
     * @param toVertexProperties The properties of {@code toVertex}.
     * @param edgeType The type of the edge being added.
     * @param edgeProperties The properties of the edge being added.
     */
    public void addEdgeTemporarily(int fromVertex, int toVertex, short fromVertexType,
        short toVertexType, Map<Short, Pair<DataType, String>> fromVertexProperties,
        Map<Short, Pair<DataType, String>> toVertexProperties, short edgeType,
        Map<Short, Pair<DataType, String>> edgeProperties) {
        if ((fromVertex <= highestPermanentVertexId) && (null != forwardAdjLists[fromVertex]) &&
            (forwardAdjLists[fromVertex].contains(toVertex, edgeType))) {
            return; // Edge is already present. Skip.
        }
        vertexTypes.set(fromVertex, fromVertexType);
        vertexTypes.set(toVertex, toVertexType);
        VertexPropertyStore.getInstance().set(fromVertex, fromVertexProperties);
        VertexPropertyStore.getInstance().set(toVertex, toVertexProperties);
        addOrDeleteEdgeTemporarily(true /* addition */, fromVertex, toVertex, edgeType,
            edgeProperties);
        highestMergedVertexId = Integer.max(highestMergedVertexId, Integer.max(fromVertex,
            toVertex));
    }

    /**
     * Deletes an edge temporarily from the graph. A call to {@link #finalizeChanges()} is required
     * to make the changes permanent.
     *
     * @param fromVertex The starting vertex ID for the edge.
     * @param toVertex The ending vertex ID for the edge.
     * @param edgeType The type of the edge being deleted.
     * @throws NoSuchElementException Exception thrown when the specified edge does not exist.
     */
    public void deleteEdgeTemporarily(int fromVertex, int toVertex, short edgeType) {
        // Check whether the edge exists in either the MERGED or the PERMANENT graph.
        if (!((fromVertex <= highestMergedVertexId) && ((toVertex <= highestMergedVertexId) &&
            mergedForwardAdjLists.containsKey(fromVertex) && (-1 != mergedForwardAdjLists.get
            (fromVertex).search(toVertex, edgeType)))) && !((fromVertex <= highestPermanentVertexId)
            && (toVertex <= highestPermanentVertexId) && (null != forwardAdjLists[fromVertex]) &&
            (forwardAdjLists[fromVertex].contains(toVertex, edgeType)))) {
            // The edge does not exist.
            throw new NoSuchElementException("The edge " + fromVertex + "->" + toVertex +
                " does not exist.");
        }
        addOrDeleteEdgeTemporarily(false /* deletion */, fromVertex, toVertex, edgeType,
            null /* no property equality filters on deletion */);
    }

    /**
     * Adds or deletes an edge temporarily from the graph.
     *
     * @param isAddition {@code true} for addition, {@code false} for deletion.
     * @param fromVertex The starting vertex ID for the edge.
     * @param toVertex The ending vertex ID for the edge.
     * @param edgeType The type of the edge being added or deleted.
     * @param edgeProperties The properties of the edge being added. Should equal {@code null} if
     * deletion.
     */
    private void addOrDeleteEdgeTemporarily(boolean isAddition, int fromVertex, int toVertex,
        short edgeType, Map<Short, Pair<DataType, String>> edgeProperties) {
        List<int[]> diffEdges = isAddition ? diffPlusEdges : diffMinusEdges;
        ShortArrayList diffEdgeTypes = isAddition ? diffPlusEdgeTypes : diffMinusEdgeTypes;
        LongArrayList diffEdgeIds = isAddition ? diffPlusEdgeIds : diffMinusEdgeIds;
        // Saves the edge to the diffEdges list and the types to the diffEdgeTypes list.
        diffEdges.add(new int[]{fromVertex, toVertex});
        diffEdgeTypes.add(edgeType);
        long edgeId = -1;
        if (isAddition) {
            edgeId = EdgeStore.getInstance().addEdge(edgeProperties);
            diffEdgeIds.add(edgeId);
        } else {
            SortedAdjacencyList fromVertexMergedAdjList = mergedForwardAdjLists.get(fromVertex);
            if (null != fromVertexMergedAdjList) {
                edgeId = fromVertexMergedAdjList.getEdgeId(toVertex, edgeType);
            } else { // edgeId is in permanentEdges
                edgeId = forwardAdjLists[fromVertex].getEdgeId(toVertex, edgeType);
            }
            diffEdgeIds.add(edgeId);
        }
        // Create the updated forward adjacency list for the vertex.
        updateMergedAdjLists(isAddition, fromVertex, toVertex, edgeType, edgeId,
            mergedForwardAdjLists, forwardAdjLists);
        // Create the updated backward adjacency list for the vertex.
        updateMergedAdjLists(isAddition, toVertex, fromVertex, edgeType, edgeId,
            mergedBackwardAdjLists, backwardAdjLists);
    }

    /**
     * Temporarily performs an addition or deletion of the edge {@code fromVertex}->{@code toVertex}
     * by updating the list {@code mergedAdjLists}, using {@code permanentAdjLists} if required.
     *
     * @param isAddition {@code true} for addition, {@code false} for deletion.
     * @param fromVertex The starting vertex ID for the edge.
     * @param toVertex The ending vertex ID for the edge.
     * @param edgeType The type of the edge being added or deleted.
     * @param edgeId the ID generated by the edgeStore for the edge.
     * @param mergedAdjLists The merged adjacency lists to modify.
     * @param permanentAdjLists The permanent adjacency list, used if {@code fromVertex} does not
     * already exist in {@code mergedAdjLists}.
     */
    private void updateMergedAdjLists(boolean isAddition, int fromVertex, int toVertex,
        short edgeType, long edgeId, Map<Integer, SortedAdjacencyList> mergedAdjLists,
        SortedAdjacencyList[] permanentAdjLists) {
        if (mergedAdjLists.containsKey(fromVertex)) {
            if (isAddition) {
                mergedAdjLists.get(fromVertex).add(toVertex, edgeType, edgeId);
            } else {
                mergedAdjLists.get(fromVertex).removeNeighbour(toVertex, edgeType);
            }
        } else {
            SortedAdjacencyList updatedList = new SortedAdjacencyList();
            if (fromVertex <= highestPermanentVertexId && null != permanentAdjLists[fromVertex]) {
                // Copy the adjacency list from the permanent graph to the merged graph.
                updatedList.addAll(permanentAdjLists[fromVertex]);
            }
            if (isAddition) {
                updatedList.add(toVertex, edgeType, edgeId);
            } else {
                updatedList.removeNeighbour(toVertex, edgeType);
            }
            mergedAdjLists.put(fromVertex, updatedList);
        }
    }

    /**
     * Permanently applies the temporary additions and deletions that have been applied using the
     * {@link #addEdgeTemporarily} and {@link #deleteEdgeTemporarily} methods since the previous
     * call to this method.
     */
    public void finalizeChanges() {
        // Increase the size of the adjacency lists if newly added edges have a higher
        // vertex ID as captured by {@code highestMergedVertexId}.
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
        // delete edgeIds from the edge store.
        for (int i = 0; i < diffMinusEdgeIds.getSize(); ++i) {
            EdgeStore.getInstance().deleteEdge(diffMinusEdgeIds.get(i));
        }
        // Reset the diff and merged graph states.
        diffPlusEdges.clear();
        diffMinusEdges.clear();
        diffPlusEdgeTypes.clear();
        diffMinusEdgeTypes.clear();
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
     * @param fromVertexTypeFilter The type of the from vertex of the edge returned by the
     * iterator.
     * @param toVertexTypeFilter The type of the to vertex of the edge returned by the iterator.
     * @param edgeTypeFilter The type of the edges returned by the iterator.
     * @return An iterator to the list of edges for the given {@code graphVersion} and
     * {@code direction}.
     * @throws UnsupportedOperationException Exception thrown when {@code graphVersion} is
     * {@link GraphVersion#DIFF_MINUS} or {@link GraphVersion#DIFF_PLUS} and direction is
     * {@link Direction#BACKWARD}.
     */
    public Iterator<int[]> getEdgesIterator(GraphVersion graphVersion, Direction direction,
        short fromVertexTypeFilter, short toVertexTypeFilter, short edgeTypeFilter) {
        if ((GraphVersion.DIFF_PLUS == graphVersion || GraphVersion.DIFF_MINUS == graphVersion) &&
            Direction.BACKWARD == direction) {
            throw new UnsupportedOperationException("Getting edges for the DIFF_PLUS "
                + "or DIFF_MINUS graph in the BACKWARD direction is not supported.");
        }
        if (GraphVersion.DIFF_PLUS == graphVersion) {
            return new DiffEdgesIterator(diffPlusEdges, diffPlusEdgeTypes, vertexTypes,
                fromVertexTypeFilter, toVertexTypeFilter, edgeTypeFilter);
        } else if (GraphVersion.DIFF_MINUS == graphVersion) {
            return new DiffEdgesIterator(diffMinusEdges, diffMinusEdgeTypes, vertexTypes,
                fromVertexTypeFilter, toVertexTypeFilter, edgeTypeFilter);
        } else {
            SortedAdjacencyList[] permanentAdjacencyLists;
            Map<Integer, SortedAdjacencyList> mergedAdjLists;
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
            return new PermanentAndMergedEdgesIterator(graphVersion, permanentAdjacencyLists,
                mergedAdjLists, vertexTypes, fromVertexTypeFilter, toVertexTypeFilter,
                edgeTypeFilter, lastVertexId);
        }
    }

    /**
     * Checks if an edge is present between {@code fromVertexId} and {@code toVertexId} in the
     * given {@code graphVersion} of the graph, for the given {@code direction}, with a given
     * edge type and a set of properties.
     *
     * @param fromVertexId The from vertex ID.
     * @param toVertexId The to vertex ID.
     * @param direction The {@link Direction} of the edge.
     * @param graphVersion The {@link GraphVersion} where the edge's presence needs to be checked.
     * @param typeFilter The type of the edge being searched for.
     * @return {@code true} if the edge is present, {@code false} otherwise.
     */
    public boolean isEdgePresent(int fromVertexId, int toVertexId, Direction direction,
        GraphVersion graphVersion, short typeFilter) {
        if (GraphVersion.DIFF_MINUS == graphVersion || GraphVersion.DIFF_PLUS == graphVersion) {
            throw new UnsupportedOperationException("Checking presence of an edge in the DIFF_PLUS "
                + "or DIFF_MINUS graph is not supported.");
        }
        if (fromVertexId < 0 || fromVertexId > highestMergedVertexId || toVertexId < 0 ||
            toVertexId > highestMergedVertexId) {
            return false;
        }
        if (GraphVersion.PERMANENT == graphVersion && (fromVertexId > highestPermanentVertexId ||
            toVertexId > highestPermanentVertexId)) {
            return false;
        }
        SortedAdjacencyList[] permanentAdjacencyLists;
        Map<Integer, SortedAdjacencyList> mergedAdjLists;
        if (Direction.FORWARD == direction) {
            permanentAdjacencyLists = forwardAdjLists;
            mergedAdjLists = mergedForwardAdjLists;
        } else {
            permanentAdjacencyLists = backwardAdjLists;
            mergedAdjLists = mergedBackwardAdjLists;
        }
        if (graphVersion == GraphVersion.MERGED && mergedAdjLists.containsKey(fromVertexId)) {
            return mergedAdjLists.get(fromVertexId).contains(toVertexId, typeFilter);
        }
        return permanentAdjacencyLists[fromVertexId].contains(toVertexId, typeFilter);
    }

    /**
     * @param srcId ID of the source vertex.
     * @param destinationId ID of the destination vertex.
     * @param type type of the edge between srcId and destinationId.
     * @return the ID of the edge between fromVertexId and toVertexId with the given type in the
     * {@link GraphVersion#MERGED} or {@link GraphVersion#PERMANENT} graph. Returns -1, if the
     * edge does not exist.
     */
    public long getEdgeIdFromGraph(int srcId, int destinationId, short type) {
        long edgeId = -1;
        if (!mergedForwardAdjLists.isEmpty()) {
            if (null != mergedForwardAdjLists.get(srcId)) {
                edgeId = mergedForwardAdjLists.get(srcId).getEdgeId(destinationId, type);
            }
        }
        if (-1 == edgeId) {
            edgeId = forwardAdjLists[srcId].getEdgeId(destinationId, type);
        }
        return edgeId;
    }

    /**
     * Returns the {@link SortedAdjacencyList} for the given {@code vertexId}, {@code direction}
     * and {@code graphVersion}.
     *
     * @param vertexId The vertex ID whose adjacency list is required.
     * @param direction The {@code Direction} of the adjacency list.
     * @param graphVersion The {@code GraphVersion} to consider.
     * @return The adjacency list for the vertex with the given {@code vertexId}, for the given
     * {@code graphVersion} and {@code direction}.
     */
    public SortedAdjacencyList getSortedAdjacencyList(int vertexId, Direction direction,
        GraphVersion graphVersion) {
        if (vertexId < 0 || vertexId > highestMergedVertexId) {
            throw new NoSuchElementException(vertexId + " does not exist.");
        } else if (GraphVersion.PERMANENT == graphVersion && vertexId > highestPermanentVertexId) {
            return new SortedAdjacencyList();
        } else if (GraphVersion.DIFF_MINUS == graphVersion || GraphVersion.DIFF_PLUS ==
            graphVersion) {
            throw new UnsupportedOperationException("Getting adjacency lists from the DIFF_PLUS "
                + "or DIFF_MINUS graph is not supported.");
        }
        SortedAdjacencyList[] permanentAdjList;
        Map<Integer, SortedAdjacencyList> mergedAdjLists;
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
            // Use the adjacency list of the permanent graph.
            return permanentAdjList[vertexId];
        }
    }

    /**
     * Returns the {@link GraphVersion#DIFF_PLUS} or {@link GraphVersion#DIFF_MINUS} edges of the
     * graph.
     *
     * @param graphVersion The vertices in the diffPlus or diffMinus in the graph.
     */
    public List<int[]> getDiffEdges(GraphVersion graphVersion) {
        if (graphVersion != GraphVersion.DIFF_PLUS && graphVersion != GraphVersion.DIFF_MINUS) {
            throw new IllegalArgumentException("The graph version should be " + GraphVersion.
                DIFF_PLUS.name() + " or " + GraphVersion.DIFF_MINUS.name() + ".");
        }
        return graphVersion == GraphVersion.DIFF_PLUS ? diffPlusEdges : diffMinusEdges;
    }

    /**
     * Returns the {@code vertexTypes} in the graph where the type of vertex i is in the ith
     * position of the returned {@link ShortArrayList}.
     */
    public ShortArrayList getVertexTypes() {
        return vertexTypes;
    }

    /**
     * Checks if the permanent capacity exceeds {@code minCapacity} and increases the capacity if it
     * doesn't.
     *
     * @param minCapacity The minimum required size of the arrays.
     */
    private void ensureCapacity(int minCapacity) {
        int oldCapacity = forwardAdjLists.length;
        forwardAdjLists = (SortedAdjacencyList[]) ArrayUtils.resizeIfNecessary(forwardAdjLists,
            minCapacity);
        backwardAdjLists = (SortedAdjacencyList[]) ArrayUtils.resizeIfNecessary(backwardAdjLists,
            minCapacity);
        initializeSortedAdjacencyLists(oldCapacity, forwardAdjLists.length);
    }

    /**
     * Initializes {@link Graph#forwardAdjLists} and {@link Graph#backwardAdjLists} with empty
     * {@link SortedAdjacencyList} in the range given by {@code startIndex} and {@code endIndex}.
     *
     * @param startIndex The start index for initializing {@link SortedAdjacencyList}, inclusive.
     * @param endIndex The end index for initializing {@link SortedAdjacencyList}, exclusive.
     */
    private void initializeSortedAdjacencyLists(int startIndex, int endIndex) {
        for (int i = startIndex; i < endIndex; ++i) {
            forwardAdjLists[i] = new SortedAdjacencyList();
            backwardAdjLists[i] = new SortedAdjacencyList();
        }
    }

    @UsedOnlyByTests
    void setHighestMergedVertexId(int highestMergedVertexId) {
        this.highestMergedVertexId = highestMergedVertexId;
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
        graph += "Temporarily added edges: " + convertDiffEdgesToString(diffPlusEdges) + System.
            lineSeparator();
        graph += "Temporarily deleted edges: " + convertDiffEdgesToString(diffMinusEdges) +
            System.lineSeparator();
        graph += "Merged Forward Adjacency Lists: " + convertMergedAdjListsToString(
            mergedForwardAdjLists);
        graph += "Merged Backward Adjacency Lists: " + convertMergedAdjListsToString(
            mergedBackwardAdjLists);
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
    private String convertPermanentAdjListsToString(SortedAdjacencyList[] permanentAdjLists) {
        StringBuilder adjString = new StringBuilder();
        for (int index = 0; index <= highestPermanentVertexId; index++) {
            SortedAdjacencyList adjList = permanentAdjLists[index];
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
    private String convertMergedAdjListsToString(Map<Integer, SortedAdjacencyList> mergedAdjLists) {
        StringBuilder adjString = new StringBuilder();
        for (int index : mergedAdjLists.keySet()) {
            SortedAdjacencyList adjList = mergedAdjLists.get(index);
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

    @Override
    public void serializeAll(String outputDirectoryPath) throws IOException, InterruptedException {
        finalizeChanges();
        ParallelArraySerDeUtils parallelArraySerDeHelper = new GraphParallelSerDeUtils(
            outputDirectoryPath, forwardAdjLists, backwardAdjLists, highestPermanentVertexId + 1);
        parallelArraySerDeHelper.startSerialization();
        MainFileSerDeHelper.serialize(this, outputDirectoryPath);
        parallelArraySerDeHelper.finishSerDe();
    }

    @Override
    public void deserializeAll(String inputDirectoryPath) throws IOException,
        ClassNotFoundException,
        InterruptedException {
        MainFileSerDeHelper.deserialize(this, inputDirectoryPath);
        ParallelArraySerDeUtils parallelArraySerDeHelper = new GraphParallelSerDeUtils(
            inputDirectoryPath, forwardAdjLists, backwardAdjLists, highestPermanentVertexId + 1);
        parallelArraySerDeHelper.startDeserialization();
        parallelArraySerDeHelper.finishSerDe();
    }

    @Override
    public void serializeMainFile(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeInt(highestPermanentVertexId);
        objectOutputStream.writeInt(forwardAdjLists.length);
        objectOutputStream.writeInt(backwardAdjLists.length);
        vertexTypes.serialize(objectOutputStream);
    }

    @Override
    public void deserializeMainFile(ObjectInputStream objectInputStream) throws IOException,
        ClassNotFoundException {
        highestPermanentVertexId = objectInputStream.readInt();
        highestMergedVertexId = highestPermanentVertexId;
        int forwardAdjListsLength = objectInputStream.readInt();
        int backwardAdjListsLength = objectInputStream.readInt();
        forwardAdjLists = new SortedAdjacencyList[forwardAdjListsLength];
        backwardAdjLists = new SortedAdjacencyList[backwardAdjListsLength];
        initializeSortedAdjacencyLists(0, highestPermanentVertexId + 1);
        vertexTypes.deserialize(objectInputStream);
    }

    @Override
    public String getMainFileNamePrefix() {
        return Graph.class.getName().toLowerCase();
    }

    /**
     * Resets the {@link Graph} state by creating a new {@code INSTANCE}.
     */
    static void reset() {
        INSTANCE = new Graph();
    }

    /**
     * Returns the singleton instance {@link #INSTANCE} of {@link Graph}.
     */
    public static Graph getInstance() {
        return INSTANCE;
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     * @return {@code true} if {@code a}'s values are the same as {@code b}'s.
     */
    @UsedOnlyByTests
    public static boolean isSamePermanentGraphAs(Graph a, Graph b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (a.highestPermanentVertexId != b.highestPermanentVertexId ||
            a.highestMergedVertexId != b.highestMergedVertexId ||
            !ShortArrayList.isSameAs(a.vertexTypes, b.vertexTypes)) {
            return false;
        }
        for (int i = 0; i < a.highestPermanentVertexId; i++) {
            if (!SortedAdjacencyList.isSameAs(a.forwardAdjLists[i], b.forwardAdjLists[i])) {
                return false;
            }
            if (!SortedAdjacencyList.isSameAs(a.backwardAdjLists[i], b.backwardAdjLists[i])) {
                return false;
            }
        }
        return true;
    }
}

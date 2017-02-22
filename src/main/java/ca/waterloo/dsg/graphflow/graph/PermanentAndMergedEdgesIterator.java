package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.util.DataType;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Encapsulates an {@code Iterator} over the edges of the {@code Graph}.
 */
public class PermanentAndMergedEdgesIterator implements Iterator<int[]> {

    private GraphVersion graphVersion;
    private SortedAdjacencyList[] permanentAdjacencyLists;
    private Map<Integer, SortedAdjacencyList> mergedAdjLists;
    private int lastVertexId;
    // Stores the next source vertex ID. 0 <= {@code nextFromVertexId} <= {@code lastVertexId}.
    private int nextFromVertexId = 0;
    // Stores the next destination vertex ID, represented by an index to the adjacency list of
    // {@code nextFromVertexId}. 0 <= {@code nextFromVertexIdAdjListIndex} < length of adjacency
    // list of {@code nextFromVertexId}.
    private int nextFromVertexAdjListIndex = -1;
    private short edgeTypeFilter;
    private Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters;

    /**
     * Constructor for {@link PermanentAndMergedEdgesIterator} with all possible vertex and edge
     * filters specified.
     *
     * @param graphVersion The version of the graph to be used for retrieving edges.
     * @param permanentAdjacencyLists The adjacency lists for the permanent version of the graph in
     * the {@link Direction#FORWARD} or {@link Direction#BACKWARD} directions.
     * @param mergedAdjLists The adjacency lists for the merged version of the graph in the {@link
     * Direction#FORWARD} or {@link Direction#BACKWARD} directions.
     * @param edgeTypeFilter The type which the iterated edges should have.
     * @param edgePropertyEqualityFilters The properties which the iterated edges should contain.
     * @param lastVertexId The vertex with the highest ID for the given graph version.
     */
    public PermanentAndMergedEdgesIterator(GraphVersion graphVersion,
        SortedAdjacencyList[] permanentAdjacencyLists,
        Map<Integer, SortedAdjacencyList> mergedAdjLists, short edgeTypeFilter,
        Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters, int lastVertexId) {
        this.graphVersion = graphVersion;
        this.permanentAdjacencyLists = permanentAdjacencyLists;
        this.mergedAdjLists = mergedAdjLists;
        this.edgeTypeFilter = edgeTypeFilter;
        this.edgePropertyEqualityFilters = edgePropertyEqualityFilters;
        this.lastVertexId = lastVertexId;
        setIndicesToNextEdge();
    }

    /**
     * Updates {@code nextFromVertexId} and {@code nextFromVertexIdAdjListIndex} to point to the
     * next edge of the graph.
     */
    private void setIndicesToNextEdge() {
        while (nextFromVertexId <= lastVertexId) {
            nextFromVertexAdjListIndex++;
            if (GraphVersion.MERGED == graphVersion && mergedAdjLists.
                containsKey(nextFromVertexId)) {
                // {@code nextFromVertexId} matches the given {@link #fromVertexType} and is
                // present in the merged graph.
                while (nextFromVertexAdjListIndex < mergedAdjLists.get(nextFromVertexId).
                    getSize()) {
                    SortedAdjacencyList fromVertexMergedAdjList = mergedAdjLists.get(
                        nextFromVertexId);
                    if ((TypeAndPropertyKeyStore.ANY == edgeTypeFilter ||
                        fromVertexMergedAdjList.getEdgeType(nextFromVertexAdjListIndex) ==
                            edgeTypeFilter) &&
                        (null == edgePropertyEqualityFilters || EdgeStore.getInstance().
                            checkEqualityFilters(fromVertexMergedAdjList.getEdgeId(
                                nextFromVertexAdjListIndex), edgePropertyEqualityFilters))) {
                        return;
                    }
                    nextFromVertexAdjListIndex++;
                }
            } else if (null != permanentAdjacencyLists[nextFromVertexId]) {
                while (nextFromVertexAdjListIndex < permanentAdjacencyLists[nextFromVertexId].
                    getSize()) {
                    SortedAdjacencyList fromVertexPermanentAdjList = permanentAdjacencyLists[
                        nextFromVertexId];
                    if ((TypeAndPropertyKeyStore.ANY == edgeTypeFilter ||
                        fromVertexPermanentAdjList.getEdgeType(nextFromVertexAdjListIndex) ==
                            edgeTypeFilter) &&
                        (null == edgePropertyEqualityFilters || EdgeStore.getInstance().
                            checkEqualityFilters(fromVertexPermanentAdjList.getEdgeId(
                                nextFromVertexAdjListIndex), edgePropertyEqualityFilters))) {
                        return;
                    }
                    nextFromVertexAdjListIndex++;
                }
            }
            nextFromVertexId++;
            nextFromVertexAdjListIndex = -1;
        }
    }

    @Override
    public boolean hasNext() {
        return nextFromVertexId <= lastVertexId;
    }

    @Override
    public int[] next() {
        if (GraphVersion.DIFF_PLUS == graphVersion || GraphVersion.DIFF_MINUS == graphVersion) {
            throw new UnsupportedOperationException("This iterator cannot be used to get the " +
                "edges for DIFF_PLUS and DIFF_MINUS graph versions.");
        }
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int[] result;
        if (GraphVersion.MERGED == graphVersion && mergedAdjLists.containsKey(nextFromVertexId)) {
            // {@code nextFromVertexId} is present in the merged graph.
            result = new int[]{nextFromVertexId, mergedAdjLists.get(nextFromVertexId).
                getNeighbourId(nextFromVertexAdjListIndex)};
        } else {
            // Send the permanent version of {@code nextFromVertexId} even for a merged graph
            // request, because the merged version of {@code nextFromVertexId} is not present.
            result = new int[]{nextFromVertexId, permanentAdjacencyLists[nextFromVertexId].
                getNeighbourId(nextFromVertexAdjListIndex)};
        }
        setIndicesToNextEdge();
        return result;
    }
}

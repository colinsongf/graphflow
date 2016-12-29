package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;

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
    private int nextFromVertexIdAdjListIndex = -1;
    private short edgeType;

    /**
     * Constructor for {@link PermanentAndMergedEdgesIterator} with all possible vertex and edge
     * filters specified.
     *
     * @param graphVersion The version of the graph to be used for retrieving edges.
     * @param permanentAdjacencyLists The adjacency lists for the permanent version of the graph in
     * the {@link Direction#FORWARD} or {@link Direction#BACKWARD} directions.
     * @param mergedAdjLists The adjacency lists for the merged version of the graph in the {@link
     * Direction#FORWARD} or {@link Direction#BACKWARD} directions.
     * @param edgeType The type ID which the selected edge should equal.
     * @param lastVertexId The vertex with the highest ID for the given graph version.
     */
    public PermanentAndMergedEdgesIterator(GraphVersion graphVersion,
        SortedAdjacencyList[] permanentAdjacencyLists,
        Map<Integer, SortedAdjacencyList> mergedAdjLists, short edgeType, int lastVertexId) {
        this.graphVersion = graphVersion;
        this.permanentAdjacencyLists = permanentAdjacencyLists;
        this.mergedAdjLists = mergedAdjLists;
        this.edgeType = edgeType;
        this.lastVertexId = lastVertexId;
        setIndicesToNextEdge();
    }

    /**
     * Updates {@code nextFromVertexId} and {@code nextFromVertexIdAdjListIndex} to point to the
     * next edge of the graph.
     */
    private void setIndicesToNextEdge() {
        while (nextFromVertexId <= lastVertexId) {
            nextFromVertexIdAdjListIndex++;
            if (GraphVersion.MERGED == graphVersion && mergedAdjLists.
                containsKey(nextFromVertexId)) {
                // {@code nextFromVertexId} matches the given {@link #fromVertexType} and is
                // present in the merged graph.
                while (nextFromVertexIdAdjListIndex < mergedAdjLists.get(nextFromVertexId).
                    getSize()) {
                    if ((TypeAndPropertyKeyStore.ANY == edgeType || mergedAdjLists.get(
                        nextFromVertexId).getEdgeTypeId(nextFromVertexIdAdjListIndex) == edgeType))
                    {
                        // The neighbour at {@code nextFromVertexIdAdjListIndex} matches {@code
                        // toVertexType} and the edge it forms with {@code nextFromVertexId}
                        // matches {@code edgeType}. In addition, the adjacency list of {@code
                        // nextFromVertexId} in the merged graph has vertices not yet iterated over.
                        return;
                    }
                    nextFromVertexIdAdjListIndex++;
                }
            } else if (null != permanentAdjacencyLists[nextFromVertexId]) {
                while (nextFromVertexIdAdjListIndex < permanentAdjacencyLists[nextFromVertexId].
                    getSize()) {
                    if (TypeAndPropertyKeyStore.ANY == edgeType || permanentAdjacencyLists[
                        nextFromVertexId].getEdgeTypeId(nextFromVertexIdAdjListIndex) == edgeType) {
                        // The neighbour at {@code nextFromVertexIdAdjListIndex} matches {@code
                        // toVertexType} and the edge it forms with {@code nextFromVertexId}
                        // matches {@code edgeType}. In addition, the adjacency list of {@code
                        // nextFromVertexId} in the permanent graph has vertices not yet iterated
                        // over.
                        return;
                    }
                    nextFromVertexIdAdjListIndex++;
                }
            }
            nextFromVertexId++;
            nextFromVertexIdAdjListIndex = -1;
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
                "edges for DIFF_PLUS and DIFF_MINUS graph versions");
        }
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int[] result;
        if (GraphVersion.MERGED == graphVersion && mergedAdjLists.containsKey(nextFromVertexId)) {
            // {@code nextFromVertexId} is present in the merged graph.
            result = new int[]{nextFromVertexId, mergedAdjLists.get(nextFromVertexId).
                getNeighbourId(nextFromVertexIdAdjListIndex)};
        } else {
            // Send the permanent version of {@code nextFromVertexId} even for a merged graph
            // request, because the merged version of {@code nextFromVertexId} is not present.
            result = new int[]{nextFromVertexId, permanentAdjacencyLists[nextFromVertexId].
                getNeighbourId(nextFromVertexIdAdjListIndex)};
        }
        setIndicesToNextEdge();
        return result;
    }
}

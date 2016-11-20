package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Encapsulates an {@code Iterator} over the edges of the {@code Graph}.
 */
public class EdgesIterator implements Iterator<int[]> {

    private GraphVersion graphVersion;
    private SortedIntArrayList[] permanentAdjacencyLists;
    private Map<Integer, SortedIntArrayList> mergedAdjLists;
    private int lastVertexId;
    // Stores the next source vertex ID. 0 <= {@code nextVertexId} <= {@code lastVertexId}.
    private int nextVertexId = 0;
    // Stores the next destination vertex ID, represented by an index to the adjacency list of
    // {@code nextVertexId}. 0 <= {@code currentAdjListIndex} < length of adjacency list of
    // {@code nextVertexId}.
    private int currentAdjListIndex = -1;

    public EdgesIterator(GraphVersion graphVersion, SortedIntArrayList[] permanentAdjacencyLists,
        Map<Integer, SortedIntArrayList> mergedAdjLists, int lastVertexId) {
        this.graphVersion = graphVersion;
        this.permanentAdjacencyLists = permanentAdjacencyLists;
        this.mergedAdjLists = mergedAdjLists;
        this.lastVertexId = lastVertexId;
        setIndicesToNextEdge();
    }

    /**
     * Updates {@code nextVertexId} and {@code currentAdjListIndex} to point to the next edge of
     * the graph.
     */
    private void setIndicesToNextEdge() {
        while (nextVertexId <= lastVertexId) {
            currentAdjListIndex++;
            if (GraphVersion.MERGED == graphVersion && mergedAdjLists.containsKey(nextVertexId)) {
                // {@code nextVertexId} is present in the merged graph.
                if (currentAdjListIndex < mergedAdjLists.get(nextVertexId).getSize()) {
                    // The adjacency list of {@code nextVertexId} in the merged graph has
                    // vertices not yet iterated over.
                    return;
                }
            } else if (null != permanentAdjacencyLists[nextVertexId] && currentAdjListIndex <
                permanentAdjacencyLists[nextVertexId].getSize()) {
                // The adjacency list of {@code nextVertexId} in the permanent graph has
                // vertices not yet iterated over.
                return;
            }
            nextVertexId++;
            currentAdjListIndex = -1;
        }
    }

    @Override
    public boolean hasNext() {
        return nextVertexId <= lastVertexId;
    }

    @Override
    public int[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int[] result;
        if (GraphVersion.MERGED == graphVersion && mergedAdjLists.containsKey(nextVertexId)) {
            // {@code nextVertexId} is present in the merged graph.
            result = new int[]{nextVertexId, mergedAdjLists.get(nextVertexId).get
                (currentAdjListIndex)};
        } else {
            // Send the permanent version of {@code nextVertexId} even for a merged graph
            // request, because the merged version of {@code nextVertexId} is not present.
            result = new int[]{nextVertexId, permanentAdjacencyLists[nextVertexId]
                .get(currentAdjListIndex)};
        }
        setIndicesToNextEdge();
        return result;
    }
}

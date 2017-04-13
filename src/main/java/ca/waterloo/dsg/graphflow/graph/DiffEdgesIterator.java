package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ShortArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Encapsulates an {@code Iterator} over the diff edges of the {@code Graph}.
 */
public class DiffEdgesIterator implements Iterator<int[]> {

    private List<int[]> diffEdges;
    private ShortArrayList diffEdgeTypes;
    private ShortArrayList vertexTypes;
    private short fromVertexTypeFilter;
    private short toVertexTypeFilter;
    private short edgeTypeFilter;
    private int next = -1;

    /**
     * Constructor for {@link DiffEdgesIterator} with all possible vertex and edge filters
     * specified.
     *
     * @param diffEdges The set of edges that were added or deleted from the graph.
     * @param diffEdgeTypes The types of the added or deleted edges.
     * @param vertexTypes The types of the graph vertices.
     * @param fromVertexTypeFilter The {@code short} filter on the type of the from vertex of the
     * edge that should be matched.
     * @param toVertexTypeFilter The {@code short} filter on the type of the to vertex of the edge
     * that should be matched.
     * @param edgeTypeFilter The {@code short} filter on the type of edges that should be matched.
     */
    public DiffEdgesIterator(List<int[]> diffEdges, ShortArrayList diffEdgeTypes,
        ShortArrayList vertexTypes, short fromVertexTypeFilter, short toVertexTypeFilter,
        short edgeTypeFilter) {
        this.diffEdges = diffEdges;
        this.diffEdgeTypes = diffEdgeTypes;
        this.vertexTypes = vertexTypes;
        this.fromVertexTypeFilter = fromVertexTypeFilter;
        this.toVertexTypeFilter = toVertexTypeFilter;
        this.edgeTypeFilter = edgeTypeFilter;
        setIndexToNextEdge();
    }

    @Override
    public boolean hasNext() {
        return next < diffEdges.size();
    }

    @Override
    public int[] next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int[] result = diffEdges.get(next);
        setIndexToNextEdge();
        return result;
    }

    private void setIndexToNextEdge() {
        next++;
        while (next < diffEdges.size()) {
            // Find the next edge {@code e=(u, v)} where {@code e}'s type is {@code
            // edgeTypeFilter} and {@code e}'s properties match {@code edgePropertyEqualityFilters}.
            // If there is no such {@code e=(u, v)}, the {@code next} index is set to the size of
            // {@code diffEdges.size()}.
            int[] diffEdge = diffEdges.get(next);
            if ((TypeAndPropertyKeyStore.ANY == fromVertexTypeFilter ||
                vertexTypes.get(diffEdge[0]) == fromVertexTypeFilter) &&
                (TypeAndPropertyKeyStore.ANY == toVertexTypeFilter ||
                    vertexTypes.get(diffEdges.get(next)[1]) == toVertexTypeFilter) &&
                (TypeAndPropertyKeyStore.ANY == edgeTypeFilter ||
                    diffEdgeTypes.get(next) == edgeTypeFilter)) {
                return;
            }
            next++;
        }
    }
}

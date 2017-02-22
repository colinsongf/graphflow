package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.IntArrayList;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Represents the adjacency list of a vertex. Stores the IDs of the vertex's neighbours, the
 * types, and the IDs of edges that the vertex has to these neighbours in sorted arrays. Arrays
 * are sorted first by neighbour IDs and then by edge {@code short} type values.
 */
public class SortedAdjacencyList {

    private static final int INITIAL_CAPACITY = 2;
    @VisibleForTesting
    int[] neighbourIds;
    @VisibleForTesting
    short[] edgeTypes;
    @VisibleForTesting
    long[] edgeIds;
    private int size;

    /**
     * Default constructor for {@link SortedAdjacencyList}. Initializes the arrays holding neighbour
     * data to default initial capacity.
     */
    public SortedAdjacencyList() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Initializes the arrays holding neighbour IDs and edge types to the given capacity.
     *
     * @param initialCapacity The initial capacity of the arrays holding neighbour IDs and edge
     * types.
     */
    public SortedAdjacencyList(int initialCapacity) {
        neighbourIds = new int[initialCapacity];
        edgeTypes = new short[initialCapacity];
        edgeIds = new long[initialCapacity];
    }

    /**
     * Adds a new neighbour with the given ID, type, and edgeId.
     *
     * @param neighbourId The ID of the neighbour.
     * @param edgeType The type of the edge to the neighbour.
     * @param edgeId The ID of the edge to the neighbour.
     */
    public void add(int neighbourId, short edgeType, long edgeId) {
        ensureCapacity(size + 1);
        neighbourIds[size] = neighbourId;
        edgeTypes[size] = edgeType;
        edgeIds[size] = edgeId;
        size++;
        sort();
    }

    /**
     * Adds the given {@link SortedAdjacencyList} to the current {@link SortedAdjacencyList}.
     *
     * @param otherList The {@link SortedAdjacencyList} to merge.
     */
    public void addAll(SortedAdjacencyList otherList) {
        ensureCapacity(size + otherList.getSize());
        for (int i = 0; i < otherList.getSize(); i++) {
            neighbourIds[size + i] = otherList.getNeighbourId(i);
            edgeTypes[size + i] = otherList.getEdgeType(i);
            edgeIds[size + i] = otherList.getEdgeId(i);
        }
        size += otherList.getSize();
        sort();
    }

    /**
     * Returns the neighbour ID at the given {@code index}.
     *
     * @param index The index of the neighbour ID.
     * @return The neighbour ID at the given index.
     * @throws ArrayIndexOutOfBoundsException If {@code index} is greater than the size of this
     * {@code SortedAdjacencyList}.
     */
    public int getNeighbourId(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No edge at index " + index + ". Therefore " +
                "cannot return the neighbour ID.");
        }
        return neighbourIds[index];
    }

    /**
     * Returns the edge type at the given {@code index}.
     *
     * @param index The index of the edge type.
     * @return The edge type at the given index.
     * @throws ArrayIndexOutOfBoundsException If {@code index} is greater than the size of this
     * {@code SortedAdjacencyList}.
     */
    public short getEdgeType(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No edge at index " + index + ". Therefore " +
                "cannot return the edge type.");
        }
        return edgeTypes[index];
    }

    /**
     * Returns the edge ID at the given {@code index}.
     *
     * @param index The index of the edge ID.
     * @return The edge ID at the given index.
     * @throws ArrayIndexOutOfBoundsException If {@code index} is greater than the size of this
     * {@code SortedAdjacencyList}.
     */
    public long getEdgeId(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No edge at index " + index + ". Therefore " +
                "cannot return the edge ID.");
        }
        return edgeIds[index];
    }

    /**
     * Returns the ID of the edge with the given neighbour ID and edgeType.
     *
     * @param neighbourId The neighbour ID of the edge.
     * @param edgeType The type of the edge.
     * @return The edge ID at the given index.
     */
    public long getEdgeId(int neighbourId, short edgeType) {
        int index = search(neighbourId, edgeType);
        return (index != -1) ? edgeIds[index] : -1;
    }

    /**
     * Returns the subset of the neighbour IDs whose type matches the given {@code edgeTypeFilter}.
     *
     * @param edgeTypeFilter The edge type for filtering.
     * @param edgePropertyEqualityFilters The edge properties for filtering.
     * @return IntArrayList The subset of neighbour IDs matching {@code edgeTypeFilter} and {@code
     * edgePropertyEqualityFilters}.
     */
    public IntArrayList getFilteredNeighbourIds(short edgeTypeFilter,
        Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters) {
        //TODO(amine): Reconsider where we want to apply the edgePropertyFilters
        IntArrayList filteredList = new IntArrayList(size);
        if (TypeAndPropertyKeyStore.ANY == edgeTypeFilter && (null == edgePropertyEqualityFilters ||
            edgePropertyEqualityFilters.isEmpty())) {
            filteredList.addAll(Arrays.copyOf(neighbourIds, size));
        } else {
            for (int i = 0; i < size; i++) {
                if ((TypeAndPropertyKeyStore.ANY == edgeTypeFilter || edgeTypes[i] ==
                    edgeTypeFilter) &&
                    ((null == edgePropertyEqualityFilters) || EdgeStore.getInstance().
                        checkEqualityFilters(edgeIds[i], edgePropertyEqualityFilters))) {
                    filteredList.add(neighbourIds[i]);
                }
            }
        }
        return filteredList;
    }

    /**
     * Removes the neighbour with the given {@code neighbourId} and {@code edgeTypeFilter}. The
     * properties of the edge are not deleted. The ID of the edge is recycled and the properties
     * are overwritten by those of the edge that gets the recycled id next.
     *
     * @param neighbourId The ID of the neighbour in the edge to remove.
     * @param edgeTypeFilter The type of the edge to the neighbour to remove.
     */
    public void removeNeighbour(int neighbourId, short edgeTypeFilter) {
        int index = search(neighbourId, edgeTypeFilter);
        if (index != -1) {
            int numElementsToShiftLeft = size - index - 1;
            if (numElementsToShiftLeft > 0) {
                System.arraycopy(neighbourIds, index + 1, neighbourIds, index,
                    numElementsToShiftLeft);
                System.arraycopy(edgeTypes, index + 1, edgeTypes, index, numElementsToShiftLeft);
                System.arraycopy(edgeIds, index + 1, edgeIds, index, numElementsToShiftLeft);
            }
            --size;
        }
    }

    /**
     * Intersects the current {@link SortedAdjacencyList} with the given {@code
     * sortedListToIntersect}. If {@code edgeTypeFilter} equals {@link TypeAndPropertyKeyStore#ANY}
     * and {@code edgePropertyEqualityFilters} is {@code null}, only the vertex ID will be
     * considered when intersecting. Otherwise, a valid intersection will match both the vertex ID,
     * the {@code edgeTypeFilter}, and the {@code edgePropertyEqualityFilters}.
     * Warning: We assume that the edges in {@code sortedListToIntersect} already satisfy the
     * {@code edgeTypeFilter} and {@code edgePropertyEqualityFilters}. Also, we assume that it is
     * sorted in monotonically increasing order of neighbourIds first and then types.
     *
     * @param sortedListToIntersect The {@link IntArrayList} to intersect.
     * @param edgeTypeFilter The edge type for filtering the intersections.
     * @param edgePropertyEqualityFilters The edge properties for filtering the intersections.
     * @return The set of intersected vertices as an {@link IntArrayList}.
     */
    public IntArrayList getIntersection(IntArrayList sortedListToIntersect, short edgeTypeFilter,
        Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters) {
        IntArrayList intersection = new IntArrayList();
        int index = 0;
        for (int i = 0; i < sortedListToIntersect.getSize(); i++) {
            // We return only one neighbour vertex regardless of how many times neighbour vertex
            // may be present in the adjacency list, with different edge types.
            int resultIndex = search(sortedListToIntersect.get(i), edgeTypeFilter,
                edgePropertyEqualityFilters, index);
            if (resultIndex != -1) {
                intersection.add(sortedListToIntersect.get(i));
                index = resultIndex;
            }
        }
        return intersection;
    }

    /**
     * Returns the size of the collections in {@code neighbourIds} and {@code edgeTypes}.
     *
     * @return The size of the above mentioned collections.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns a string representation of {@link SortedAdjacencyList}.
     *
     * @return String representation.
     */
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (int i = 0; i < size; i++) {
            sj.add("{" + neighbourIds[i] + ": " + edgeTypes[i] + "}");
        }
        return "[" + sj.toString() + "]";
    }

    /**
     * Returns true if the given {@code neighbourId} with edge {@code edgeTypeFilter} and
     * properties {@code edgePropertyEqualityFilters} exists in the sorted adjacency list, and
     * false otherwise.
     *
     * @param neighbourId The neighbour ID to be searched.
     * @param edgeTypeFilter The edge type to filter by.
     * @param edgePropertyEqualityFilters The edge properties to filter by.
     * @return True if the {@code neighbourId} and {@code typeId} pair exists; and false otherwise.
     */
    public boolean contains(int neighbourId, short edgeTypeFilter,
        Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters) {
        return search(neighbourId, edgeTypeFilter, edgePropertyEqualityFilters,
            0 /* start index */) != -1;
    }

    /**
     * @see #contains(int, short, Map)
     */
    public boolean contains(int neighbourId, short edgeTypeFilter) {
        return search(neighbourId, edgeTypeFilter, null /* no property equality filters */,
            0 /* start index */) != -1;
    }

    /**
     * A linear search for the given {@code neighbourId} in {@code neighbourIds} starting from the
     * given {@code startIndex}. Returns either the index of {@code neighbourId} or -1 if it is
     * not found.
     *
     * @param neighbourId The neighbour ID to be searched.
     * @param edgeTypeFilter The type of the edge searched for.
     * @param edgePropertyEqualityFilters The set of equality filters to match the properties of
     * the edge being searched for.
     * @param startIndex The index to start the search from.
     * @return Index of the neighbour or -1 if the neighbour is not in the list.
     */
    public int search(int neighbourId, short edgeTypeFilter,
        Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters, int startIndex) {
        int next = startIndex;
        while (next < size) {
            if (neighbourIds[next] == neighbourId) {
                if ((TypeAndPropertyKeyStore.ANY == edgeTypeFilter ||
                    edgeTypeFilter == edgeTypes[next]) && (null == edgePropertyEqualityFilters ||
                    EdgeStore.getInstance().checkEqualityFilters(edgeIds[next],
                        edgePropertyEqualityFilters))) {
                    return next;
                } else if (edgeTypes[next] > edgeTypeFilter) {
                    return -1;
                }
            } else if (neighbourIds[next] > neighbourId) {
                return -1;
            }
            next++;
        }
        return -1;
    }

    /**
     * @see #search(int, short, Map, int)
     */
    public int search(int neighbourId, short edgeTypeFilter) {
        return search(neighbourId, edgeTypeFilter, null /* no edge property equality filters */,
            0 /* start index */);
    }

    /**
     * Sorts {@code neighbourIds} first in ascending order of their IDs and then by edge type.
     * The {@code edgeTypes} and {@code edgeIds} are also sorted to match the neighbor ID ordering.
     */
    private void sort() {
        for (int i = 1; i < size; i++) {
            int tempNeighbourId = neighbourIds[i];
            short tempNeighbourType = edgeTypes[i];
            long tempNeighbourEdgeId = edgeIds[i];
            int j = i;
            while ((j > 0) && ((tempNeighbourId < neighbourIds[j - 1]) || ((tempNeighbourId ==
                neighbourIds[j - 1]) && (tempNeighbourType < edgeTypes[j - 1])))) {
                neighbourIds[j] = neighbourIds[j - 1];
                edgeTypes[j] = edgeTypes[j - 1];
                edgeIds[j] = edgeIds[j - 1];
                j--;
            }
            neighbourIds[j] = tempNeighbourId;
            edgeTypes[j] = tempNeighbourType;
            edgeIds[j] = tempNeighbourEdgeId;
        }
    }

    private void ensureCapacity(int minCapacity) {
        neighbourIds = ArrayUtils.resizeIfNecessary(neighbourIds, minCapacity);
        edgeTypes = ArrayUtils.resizeIfNecessary(edgeTypes, minCapacity);
        edgeIds = ArrayUtils.resizeIfNecessary(edgeIds, minCapacity);
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     * @return {@code true} if the {@code a} object values are the same as the
     * {@code b} object values, {@code false} otherwise.
     */
    @UsedOnlyByTests
    public static boolean isSameAs(SortedAdjacencyList a, SortedAdjacencyList b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (a.size != b.size) {
            return false;
        }
        for (int i = 0; i < a.size; i++) {
            if ((a.getNeighbourId(i) != b.getNeighbourId(i)) ||
                (a.getEdgeType(i) != b.getEdgeType(i)) || (a.getEdgeId(i) != b.getEdgeId(i))) {
                return false;
            }
        }
        return true;
    }
}

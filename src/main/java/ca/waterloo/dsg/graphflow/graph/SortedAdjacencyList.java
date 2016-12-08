package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.IntArrayList;
import ca.waterloo.dsg.graphflow.util.PackagePrivateForTesting;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Represents the adjacency list of a vertex. Stores the IDs of the vertex's neighbours and the
 * types of edges the vertex has to these neighbours in sorted arrays. Arrays are sorted first by
 * neighbour IDs and then by edge type IDs.
 */
public class SortedAdjacencyList {

    private static final int INITIAL_CAPACITY = 2;
    // TODO: Refactor RESIZE_MULTIPLIER by using ArrayUtils.resize() method from Siddahrtha's PR.
    private static final float RESIZE_MULTIPLIER = 1.2f;
    @PackagePrivateForTesting
    int[] neighbourIds;
    @PackagePrivateForTesting
    short[] edgeTypes;
    @PackagePrivateForTesting
    int capacity;
    @PackagePrivateForTesting
    int size;

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
        capacity = initialCapacity;
    }

    /**
     * Adds a new neighbour with the given ID and type.
     *
     * @param neighbourId The ID of the neighbour.
     * @param edgeType The type of the edge to the neighbour.
     */
    public void add(int neighbourId, short edgeType) {
        ensureCapacity(size + 1);
        neighbourIds[size] = neighbourId;
        edgeTypes[size] = edgeType;
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
            edgeTypes[size + i] = otherList.getEdgeTypeId(i);
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
            throw new ArrayIndexOutOfBoundsException("No neighbour ID at index" + index);
        }
        return neighbourIds[index];
    }

    /**
     * Gets the edge type ID at the given {@code index}.
     *
     * @param index The index of the edge type ID.
     * @return The edge type ID at the given index.
     * @throws ArrayIndexOutOfBoundsException If {@code index} is greater than size of this
     * {@code SortedAdjacencyList}.
     */
    public short getEdgeTypeId(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No Edge type ID at index" + index);
        }
        return edgeTypes[index];
    }

    /**
     * Returns the subset of the neighbour IDs of this {@link SortedAdjacencyList} whose type
     * matches the given {@code edgeType}.
     *
     * @param edgeType The edge type ID for filtering.
     * @return IntArrayList The subset of neighbour IDs matching {@code edgeType}.
     */
    public IntArrayList getFilteredNeighbourIds(short edgeType) {
        IntArrayList filteredList = new IntArrayList(size);
        if (Graph.ANY_TYPE == edgeType) {
            filteredList.addAll(Arrays.copyOf(neighbourIds, size));
        } else {
            for (int i = 0; i < size; i++) {
                if (edgeTypes[i] == edgeType) {
                    filteredList.add(neighbourIds[i]);
                }
            }
        }
        return filteredList;
    }

    /**
     * Removes the neighbour with the given {@code neighbourId} and {@code edgeTypeId}.
     *
     * @param neighbourId The ID of the neighbour in the edge to remove.
     * @param edgeTypeId The type ID of the edge to the neighbour to remove.
     */
    public void removeNeighbour(int neighbourId, short edgeTypeId) {
        int index = search(neighbourId, edgeTypeId);
        if (index != -1) {
            int numElementsToShiftLeft = size - index - 1;
            if (numElementsToShiftLeft > 0) {
                System.arraycopy(neighbourIds, index + 1, neighbourIds, index, numElementsToShiftLeft);
                System.arraycopy(edgeTypes, index + 1, edgeTypes, index, numElementsToShiftLeft);
            }
            --size;
        }
    }

    /**
     * Intersects the current {@link SortedAdjacencyList} with the given {@code sortedListToIntersect}. If, 1)
     * {@code edgeType} equals {@link Graph#ANY_TYPE}, only node ID will be considered when
     * intersecting. 2) Else a valid intersection will match both node ID and edge type ID.
     *
     * @param sortedListToIntersect The {@link IntArrayList} to intersect.
     * @param edgeType The edge type ID for filtering the intersections.
     * @return The set of intersected vertices as an {@link IntArrayList}.
     */
    public IntArrayList getIntersection(IntArrayList sortedListToIntersect, short edgeType) {
        // Warning: We assume that {@code sortedListToIntersect} is filtered with edgeType and
        // that it is sorted in monotonically increasing order. Execution will also be faster if
        // {@code sortedListToIntersect} is shorter than {@code this}.
        IntArrayList intersection = new IntArrayList();
        int index = 0;
        for (int i = 0; i < sortedListToIntersect.getSize(); i++) {
            // We return only one neighbour vertex regardless of how many times neighbour vertex
            // may be present in the adjacency list, with different edge types.
            int resultIndex = search(sortedListToIntersect.get(i), edgeType, index);
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
     * Returns the size of the arrays, {@code neighbourIds} and {@code edgeTypes}.
     *
     * @return The underlying array size of the above mentioned collections.
     */
    @PackagePrivateForTesting
    public int getCapacity() {
        return capacity;
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

    public boolean isSameAs(SortedAdjacencyList that) {
        if (null == this && null == that) {
            return true;
        }
        if ((null == this && null != that) || (null == that && null != this)) {
            return false;
        }
        if (this.getSize() != that.getSize()) {
            return false;
        }
        for (int i = 0; i < that.getSize(); i++) {
            if ((this.getNeighbourId(i) != that.getNeighbourId(i)) || (this.getEdgeTypeId(i) != that
                .getEdgeTypeId(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     *Searches for the given {@code neighbourId} in {@code neighbourIds} starting from the
     * given zero and continuing in monotonically increasing fashion. Returns either
     * the index of {@code neighbourId} or -1 if it is not found.
     *
     * @param neighbourId The neighbour ID to be searched.
     * @param typeId The typeId to filter by.
     * @return Index of {@code neighbourId} or -1.
     */
    int search(int neighbourId, int typeId) {
        return search(neighbourId, typeId, 0);
    }

    /**
     * Returns true if the given {@code neighbourId} and {@code typeId} pair exists, and false
     * otherwise.
     * @param neighbourId The neighbour ID to be searched.
     * @param typeId The typeId to filter by.
     * @return boolean Value representing whether the {@code neighbourId} and {@code typeId} pair
     * exists.
     */
    public boolean contains(int neighbourId, int typeId) {
        return search(neighbourId, typeId, 0) != -1;
    }

    /**
     * Searches for the given {@code neighbourId} in {@code neighbourIds} starting from the
     * given {@code startIndex} and searching one by one to the right. Returns either
     * the index of {@code neighbourId} or -1 if it is not found.
     *
     * @param neighbourId The neighbour ID to be searched.
     * @param typeId The type ID to filter by.
     * @param startIndex The index to start the search from.
     * @return Index of the neighbour or -1 if the neighbour is not in the list.
     */
     @PackagePrivateForTesting
     int search(int neighbourId, int typeId, int startIndex) {
         int next = startIndex;
         while (next < size) {
             if (neighbourIds[next] == neighbourId) {
                 if (Graph.ANY_TYPE == typeId || typeId == edgeTypes[next]) {
                     return next;
                 } else if (edgeTypes[next] > typeId) {
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
     * Sorts {@code neighbourIds} first in ascending order of their IDs and then by edge type.
     * The {@code edgeTypes} are also sorted to match the neighbor ID ordering.
     */
    private void sort() {
        for (int i = 1; i < size; i++) {
            int tempNeighbourId = neighbourIds[i];
            short tempNeighbourType = edgeTypes[i];
            int j = i;
            while ((j > 0) && ((tempNeighbourId < neighbourIds[j - 1]) || ((tempNeighbourId ==
                neighbourIds[j - 1]) && (tempNeighbourType < edgeTypes[j - 1])))) {
                neighbourIds[j] = neighbourIds[j - 1];
                edgeTypes[j] = edgeTypes[j - 1];
                j--;
            }
            neighbourIds[j] = tempNeighbourId;
            edgeTypes[j] = tempNeighbourType;
        }
    }

    private void ensureCapacity(int minCapacity) {
        // TODO: Refactor ensureCapacity by using ArrayUtils.resize() method from Siddahrtha's PR.
        if (minCapacity > capacity) {
            int newCapacity = (int) Double.max(capacity * RESIZE_MULTIPLIER + 1, minCapacity);
            neighbourIds = Arrays.copyOf(neighbourIds, newCapacity);
            edgeTypes = Arrays.copyOf(edgeTypes, newCapacity);
            capacity = newCapacity;
        }
    }
}

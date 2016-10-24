package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;

/**
 * A list of int primitives implemented using an array.
 */
public class SortedIntArrayList {

    private static final int INITIAL_CAPACITY = 2;
    private int[] data;
    private int size = 0;

    public SortedIntArrayList() {
        data = new int[INITIAL_CAPACITY];
    }

    public SortedIntArrayList(int capacity) {
        data = new int[capacity];
    }

    public boolean add(int i) {
        ensure_capacity(size + 1);
        data[size++] = i;
        this.sort();
        return true;
    }

    public int get(int index) throws ArrayIndexOutOfBoundsException {
        return data[index];
    }

    public boolean addAll(int[] i) {
        int numnew = i.length;
        ensure_capacity(size + numnew);
        System.arraycopy(i, 0, data, size, numnew);
        size += numnew;
        this.sort();
        return true;
    }

    public int remove(int index) throws ArrayIndexOutOfBoundsException {
        int numMoved = size - index - 1;
        int valueToBeRemoved = data[index];
        if (numMoved > 0)
            System.arraycopy(data, index + 1, data, index, numMoved);
        data[--size] = 0;
        this.sort();
        return valueToBeRemoved;
    }

    public int size() {
        return size;
    }

    public int[] toArray() {
        return Arrays.copyOfRange(data, 0, size);
    }

    /**
     * Sorts the underlying array in place.
     */
    private void sort() {
        Arrays.sort(this.data, 0, size - 1);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < size - 1; i++) {
            builder.append(data[i] + ", ");
        }
        if (size > 0) {
            builder.append(data[size - 1]);
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Checks if the current capacity exceeds size and increases the capacity if it doesn't.
     * @param minCapacity
     */
    private void ensure_capacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    /**
     * Searches for the given value in the list using binary search and returns the index.
     * Returns -1 if value not found.
     * @param value value to find in list.
     * @return int index of matched value or -1 if value does not exist
     */
    public int search(int value) {
        int lowindex = 0,
            highIndex = this.size - 1,
            result = -1;
        while (lowindex <= highIndex) {
            int mid = (lowindex + highIndex) / 2;
            if (data[mid] == value) {
                result = mid;
                break;
            } else if (data[mid] < value) {
                lowindex = mid + 1;
            } else {
                highIndex = mid - 1;
            }
        }
        return result;
    }

    /**
     * Intersects @code{this} @code{SortedIntArrayList} and the given newList
     * and returns the result as @code{SortedIntArrayList}.
     * @param newList
     * @return SortedIntArrayList
     */
    public SortedIntArrayList getIntersection(SortedIntArrayList newList) {
        SortedIntArrayList shorter, longer, intersection;
        if (this.size() > newList.size()) {
            shorter = newList;
            longer = this;
        } else {
            shorter = this;
            longer = newList;
        }
        intersection = new SortedIntArrayList(shorter.size());
        for (int i = 0; i < shorter.size(); i++) {
            int resultIndex = longer.search(shorter.get(i));

            if (resultIndex >= 0) {
                intersection.add(shorter.get(i));
            }
        }
        return intersection;
    }
}

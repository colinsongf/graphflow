package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * A list of sorted int primitives implemented using an array.
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

    public void add(int i) {
        data = ArrayUtils.resizeIfNecessary(data, size + 1);
        data[size++] = i;
        sort();
    }

    public int get(int index) throws ArrayIndexOutOfBoundsException {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return data[index];
    }

    public void addAll(int[] i) {
        int numnew = i.length;
        data = ArrayUtils.resizeIfNecessary(data, size + numnew);
        System.arraycopy(i, 0, data, size, numnew);
        size += numnew;
        sort();
    }

    public int removeFromIndex(int index) throws ArrayIndexOutOfBoundsException {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        int numMoved = size - index - 1;
        int valueToBeRemoved = data[index];
        if (numMoved > 0) {
            System.arraycopy(data, index + 1, data, index, numMoved);
        }
        --size;
        sort();
        return valueToBeRemoved;
    }

    public int removeElement(int value) throws ArrayIndexOutOfBoundsException {
        int index = search(value);
        if (index != -1) {
            removeFromIndex(index);
        }
        return index;
    }

    public int getSize() {
        return size;
    }

    public int[] toArray() {
        return Arrays.copyOf(data, size);
    }

    /**
     * Sorts the underlying array in place.
     */
    private void sort() {
        Arrays.sort(data, 0, size);
    }

    /**
     * Searches for the given value in the list using binary search and returns the index of the
     * value in the list. Returns -1 if value not found.
     *
     * @param value value to find in list.
     * @return int index of matched value or -1 if value does not exist
     */
    public int search(int value) {
        int lowIndex = 0;
        int highIndex = size - 1;
        int result = -1;
        while (lowIndex <= highIndex) {
            int mid = lowIndex + (highIndex - lowIndex) / 2;
            if (data[mid] == value) {
                result = mid;
                break;
            } else if (data[mid] < value) {
                lowIndex = mid + 1;
            } else {
                highIndex = mid - 1;
            }
        }
        return result;
    }

    /**
     * Intersects this {@code SortedIntArrayList} and the given {@code newList} and returns the
     * result as
     * {@code SortedIntArrayList}.
     *
     * @param newList
     * @return SortedIntArrayList
     */
    public SortedIntArrayList getIntersection(SortedIntArrayList newList) {
        SortedIntArrayList shorter, longer, intersection;
        if (size > newList.getSize()) {
            shorter = newList;
            longer = this;
        } else {
            shorter = this;
            longer = newList;
        }
        intersection = new SortedIntArrayList(shorter.getSize());
        for (int i = 0; i < shorter.getSize(); i++) {
            int resultIndex = longer.search(shorter.get(i));
            if (resultIndex >= 0) {
                intersection.add(shorter.get(i));
            }
        }
        return intersection;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (int i = 0; i < size; i++) {
            sj.add("" + data[i]);
        }
        return "[" + sj.toString() + "]";
    }

    /**
     * Used in unit testing to assert the correct state of the {@code SortedIntArrayList}.
     *
     * @param expectedArray the expected array representation of {@code SortedIntArrayList}.
     * @return {@code true} if the {@code SortedIntArrayList} length and values match the
     * {@code expectedArray} length and values, {@code false} otherwise.
     */
    @ExistsForTesting
    public boolean isSameAsArray(int[] expectedArray) {
        if (null == expectedArray) {
            return false;
        }
        if (this.size != expectedArray.length) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.data[i] != expectedArray[i]) {
                return false;
            }
        }
        return true;
    }
}

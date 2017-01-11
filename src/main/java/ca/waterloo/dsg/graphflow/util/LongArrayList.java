package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * A list of long primitives implemented using an array.
 */
public class LongArrayList {

    private static final int INITIAL_CAPACITY = 2;
    private long[] data;
    private int size = 0;

    /**
     * Creates {@link LongArrayList} with default capacity.
     */
    public LongArrayList() {
        data = new long[INITIAL_CAPACITY];
    }

    /**
     * Creates {@link LongArrayList} with the given {@code capacity}.
     *
     * @param capacity The initial capacity of the array underlying the {@link LongArrayList}.
     */
    public LongArrayList(int capacity) {
        data = new long[capacity];
    }

    /**
     * Adds a new long to the list.
     *
     * @param element The new long to be added.
     */
    public void add(long element) {
        data = ArrayUtils.resizeIfNecessary(data, size + 1);
        data[size++] = element;
    }

    /**
     * Appends the given array of longs to the list.
     *
     * @param elements The array of longs to be added.
     */
    public void addAll(long[] elements) {
        data = ArrayUtils.resizeIfNecessary(data, size + elements.length);
        System.arraycopy(elements, 0, data, size, elements.length);
        size += elements.length;
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index The index in the underlying array of the element to be returned.
     * @return long The value at index {@code index}.
     * @throws ArrayIndexOutOfBoundsException Exception thrown when {@code index} is larger than
     * the size of the collection.
     */
    public long get(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No element at index " + index);
        }
        return data[index];
    }

    /**
     * Removes the value at the given index.
     *
     * @param index The index from which the value should be removed.
     * @return long Removed value.
     * @throws ArrayIndexOutOfBoundsException Exception thrown when {@code index} is larger than
     * the size of the collection.
     */
    public long removeFromIndex(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No element at index " + index);
        }
        int numElementsToShiftLeft = size - index - 1;
        long valueToBeRemoved = data[index];
        if (numElementsToShiftLeft > 0) {
            System.arraycopy(data, index + 1, data, index, numElementsToShiftLeft);
        }
        --size;
        return valueToBeRemoved;
    }

    public int getSize() {
        return size;
    }

    /**
     * Returns a copy of the array underlying this {@link LongArrayList}.
     *
     * @return Array containing all the elements in the collection.
     */
    public long[] toArray() {
        return Arrays.copyOf(data, size);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (int i = 0; i < size; i++) {
            sj.add("" + data[i]);
        }
        return "[" + sj.toString() + "]";
    }
}

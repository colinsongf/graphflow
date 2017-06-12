package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;

/**
 * A list of int primitives implemented using an array.
 */
public class IntArrayList {

    private static final int INITIAL_CAPACITY = 2;
    private int[] data;
    private int size = 0;

    /**
     * Creates {@link IntArrayList} with default capacity.
     */
    public IntArrayList() {
        data = new int[INITIAL_CAPACITY];
    }

    /**
     * Creates {@link IntArrayList} with the given {@code capacity}.
     *
     * @param capacity The initial capacity of the array underlying the {@link IntArrayList}.
     */
    public IntArrayList(int capacity) {
        data = new int[capacity];
    }

    /**
     * Adds a new integer to the list.
     *
     * @param element The new integer to be added.
     */
    public void add(int element) {
        data = ArrayUtils.resizeIfNecessary(data, size + 1);
        data[size++] = element;
    }

    /**
     * Appends the given array of integers to the list.
     *
     * @param elements The array of integers to be added.
     */
    public void addAll(int[] elements) {
        data = ArrayUtils.resizeIfNecessary(data, size + elements.length);
        System.arraycopy(elements, 0, data, size, elements.length);
        size += elements.length;
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index The index in the underlying array of the element to be returned.
     *
     * @return int The value at index {@code index}.
     *
     * @throws ArrayIndexOutOfBoundsException Exception thrown when {@code index} is larger than the
     * size of the collection.
     */
    public int get(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No element at index " + index);
        }
        return data[index];
    }

    public int getSize() {
        return size;
    }

    public int[] toArray() {
        return Arrays.copyOf(data, size);
    }
}

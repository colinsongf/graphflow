package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * A list of int primitives implemented using an array.
 */
public class ShortArrayList {

    private static final int INITIAL_CAPACITY = 2;
    private short[] data;
    private int size = 0;

    /**
     * Creates {@link ShortArrayList} with default capacity.
     */
    public ShortArrayList() {
        data = new short[INITIAL_CAPACITY];
    }

    /**
     * Adds a new short to the {@link ShortArrayList}.
     *
     * @param element The new short to be added to the collection.
     */
    public void add(short element) {
        data = ArrayUtils.resizeIfNecessary(data, size + 1);
        data[size++] = element;
    }

    /**
     * Sets the given value at the given {@code index}, resizing the underlying array if necessary
     * . If the
     * index is greater than the current array capacity, all values between the current array
     * size and {@code index} will be initialized to zero.
     *
     * @param index Index in the underlying array to be be updated with {@code newItem}.
     * @param newItem The value to be placed at {@code index}.
     */
    public void set(int index, short newItem) {
        if (index >= data.length) {
            data = ArrayUtils.resizeIfNecessary(data, index + 1); // Index is zero based.
        }
        data[index] = newItem;
        if (index >= size) {
            size = index + 1;
        }
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index The index in the underlying array of the element to be returned.
     * @return short The value at {@code index}.
     * @throws ArrayIndexOutOfBoundsException Throws exception if index is greater than the size
     * of the {@link ShortArrayList} collection.
     */
    public short get(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No element at index " + index);
        }
        return data[index];
    }

    public int getSize() {
        return size;
    }

    @VisibleForTesting
    short[] toArray() {
        return Arrays.copyOf(data, size);
    }

    /**
     * Sets the size of the collection to zero.
     */
    public void clear() {
        size = 0;
    }
}

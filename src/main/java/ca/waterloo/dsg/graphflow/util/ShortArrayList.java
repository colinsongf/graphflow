package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * A list of int primitives implemented using an array.
 */
public class ShortArrayList {

    private static final int INITIAL_CAPACITY = 2;
    private static final float RESIZE_MULTIPLIER = 1.2f;
    private short[] data;
    private int size = 0;

    /**
     * Creates {@link ShortArrayList} with default capacity.
     */
    public ShortArrayList() {
        data = new short[INITIAL_CAPACITY];
    }

    /**
     * Creates {@link ShortArrayList} with the given {@code capacity}.
     *
     * @param capacity The initial capacity of the underlying array.
     */
    public ShortArrayList(int capacity) {
        data = new short[capacity];
    }

    /**
     * Adds a new short to the {@link ShortArrayList}.
     *
     * @param element The new short to be added to the collection.
     */
    public void add(short element) {
        ensureCapacity(size + 1);
        data[size++] = element;
    }

    /**
     * Appends the given array of integers to the list.
     *
     * @param elements The array of integers to be appended to the collection.
     */
    public void addAll(short[] elements) {
        ensureCapacity(size + elements.length);
        System.arraycopy(elements, 0, data, size, elements.length);
        size += elements.length;
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
            ensureCapacity(index + 1); // Add one because index is zero based.
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
     * of the {@link SortedIntArrayList} collection.
     */
    public short get(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No element at index " + index);
        }
        return data[index];
    }

    /**
     * Removes the value at the given index.
     *
     * @param index The index in the underlying array from which the value should be removed.
     * @return short Removed value.
     * @throws ArrayIndexOutOfBoundsException Throws exception if index is greater than the size
     * of the {@link SortedIntArrayList} collection.
     */
    public short removeFromIndex(int index) {
        if (index >= size) {
            throw new ArrayIndexOutOfBoundsException("No element at index " + index);
        }
        int numElementsToShiftLeft = size - index - 1;
        short valueToBeRemoved = data[index];
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
     * Returns a copy of the collection stored in {@link ShortArrayList} as an array.
     *
     * @return short[] The copy of {@code data}.
     */
    public short[] toArray() {
        return Arrays.copyOf(data, size);
    }

    /**
     * Gives a string representation of {@link ShortArrayList}.
     *
     * @return String The string representation that is returned.
     */
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(", ");
        for (int i = 0; i < size; i++) {
            sj.add("" + data[i]);
        }
        return "[" + sj.toString() + "]";
    }

    /**
     * Sets the size of the collection to zero.
     */
    public void clear() {
        size = 0;
    }

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The other object.
     * @return {@code true} if the current object values match perfectly with the other object's
     * values, {@code false} otherwise.
     */
    @ExistsForTesting
    public boolean isSameAs(short[] that) {
        if (null == that) {
            return false;
        }
        if (this.size != that.length) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (this.data[i] != that[i]) {
                return false;
            }
        }
        return true;
    }

    private void ensureCapacity(int minCapacity) {
        // TODO: Refactor ensureCapacity by using ArrayUtils.resize() method from Siddahrtha's PR.
        if (minCapacity > data.length) {
            int newCapacity = (int) Double.max(data.length * RESIZE_MULTIPLIER + 1, minCapacity);
            data = Arrays.copyOf(data, newCapacity);
        }
    }
}

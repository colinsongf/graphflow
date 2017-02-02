package ca.waterloo.dsg.graphflow.util;

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
     * Adds a new long to the list.
     *
     * @param element The new long to be added.
     */
    public void add(long element) {
        data = ArrayUtils.resizeIfNecessary(data, size + 1);
        data[size++] = element;
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index The index being searched.
     * @return The value at the given index.
     */
    public long get(int index) {
        return data[index];
    }

    public int getSize() {
        return size;
    }
}

package ca.waterloo.dsg.graphflow.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Represents a list of sorted objects of type {@code T} using an array.
 */
public class SortedArrayList<T> {

    private static final int INITIAL_CAPACITY = 2;
    /**
     * TODO(Chathura): Check if initializing to 2 dimensional array of int at declaration time
     * gives contiguous memory allocation.
     */
    private T[] data;
    private int size = 0;
    private Comparator<T> comparator;

    public SortedArrayList(int capacity) {
        this.data = (T[]) new Object[capacity];
    }

    public SortedArrayList() {
        this.data = (T[]) new Object[INITIAL_CAPACITY];
    }

    public SortedArrayList(int capacity, Comparator<T> comparator) {
        this(capacity);
        this.comparator = comparator;
    }

    public SortedArrayList(Comparator<T> comparator) {
        this();
        this.comparator = comparator;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public boolean add(T t) {
        ensureCapacity(size + 1);
        data[size++] = t;
        sort();
        return true;
    }

    public T get(int index) throws IndexOutOfBoundsException {
        if (index >= data.length) {
            throw new IndexOutOfBoundsException();
        }
        return data[index];
    }

    public boolean addAll(T[] t) {
        int numnew = t.length;
        ensureCapacity(size + numnew);
        System.arraycopy(t, 0, data, size, numnew);
        size += numnew;
        sort();
        return true;
    }

    public T remove(int index) throws IndexOutOfBoundsException {
        if (index >= data.length) {
            throw new IndexOutOfBoundsException();
        }
        int numMoved = size - index - 1;
        T valueToBeRemoved = data[index];
        if (numMoved > 0) {
            System.arraycopy(data, index + 1, data, index, numMoved);
        }
        data[--size] = null;
        sort();
        return valueToBeRemoved;
    }

    private void sort() {
        Arrays.sort(data, 0, size, comparator);
    }

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = data.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    public int search(T t) {
        return Arrays.binarySearch(data, 0, size, t, comparator);
    }

    public SortedArrayList<T> getIntersection(SortedArrayList<T> newList) {
        SortedArrayList<T> shorter, longer, intersection;
        if (this.size() > newList.size()) {
            shorter = newList;
            longer = this;
        } else {
            shorter = this;
            longer = newList;
        }
        intersection = new SortedArrayList<T>(shorter.size());
        for (int i = 0; i < shorter.size(); i++) {
            int resultIndex = longer.search(shorter.get(i));
            if (resultIndex >= 0) {
                intersection.add(shorter.get(i));
            }
        }
        return intersection;
    }

    public int size() {
        return size;
    }

    public T[] toArray() {
        return Arrays.copyOfRange(data, 0, size);
    }
}

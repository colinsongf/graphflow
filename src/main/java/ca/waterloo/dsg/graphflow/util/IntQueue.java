package ca.waterloo.dsg.graphflow.util;

import java.util.NoSuchElementException;

/**
 * Represents a queue of {@code T} objects using a array based circular buffer.
 */
public class IntQueue {

    private static final int INITIAL_CAPACITY = 2;
    private static final int RESIZE_MULTIPLIER = 2;
    /** TODO(Chathura): Check if initializing to 2 dimensional array of int at declaration time
     * gives contiguous memory allocation.
     */
    private int[] queue;
    private int size = 0;
    private int first = 0;
    private int next = 0;
    private int capacity;

    public IntQueue(int capacity) {
        queue = new int[capacity];
        this.capacity = capacity;
    }

    public IntQueue() {
        queue = new int[INITIAL_CAPACITY];
        this.capacity = INITIAL_CAPACITY;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void put(int element) {
        if(size >= capacity) {
            resize(capacity * RESIZE_MULTIPLIER);
        }
        queue[next++] = element;
        if(next == capacity) {
            next = 0;
        }
        size++;
    }

    public int get() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        int result = queue[first++];
        if(--size == 0) {
            first = 0;
            next = 0;
        }
        return result;
    }

    public int peekNext() {
        if(size == 0) {
            throw new NoSuchElementException();
        }
        return queue[first];
    }

    @VisibleForTesting
    public void resize(int capacity) {
        int[] temp = new int[capacity];
        for (int i=0; i < size; i++) {
            temp[i] = queue[(first+i) % this.capacity];
        }
        queue = temp;
        next = size;
        first = 0;
        this.capacity = capacity;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int size() {
        return size;
    }

    public int getFirst() {
        return first;
    }

    public int getNext() {
        return next;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int length = first <= next? next: first+size - next;
        for (int i = 0; i < length - 1; i++) {
            builder.append(queue[i] + ", ");
        }
        if (length > 0) {
            builder.append(queue[length - 1]);
        }
        builder.append("]");
        return builder.toString();
    }
}

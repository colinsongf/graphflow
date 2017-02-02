package ca.waterloo.dsg.graphflow.util;

import java.util.NoSuchElementException;

/**
 * Represents a queue of integers using an array based circular buffer.
 */
public class IntQueue {

    private static final int STANDARD_INITIAL_CAPACITY = 2;
    private static final float RESIZE_MULTIPLIER = 1.2f;
    private int[] queue;
    // Stores the number of items currently in the queue.
    private int size = 0;
    // Stores the index of the first element of the queue in the circular array.
    private int firstItemIndex = 0;
    // Stores the index of the next item in the circular array. This circles back to 0 after
    // index equals capacity - 1.
    private int nextItemIndex = 0;
    // Stores the current size of the array backing the queue.
    private int capacity;
    // Stores the initial size of the array so it can be used in resetting.
    private int initialCapacity;

    /**
     * Creates a {@link IntQueue} with the given {@code capacity}.
     *
     * @param capacity Represents the initial capacity.
     */
    public IntQueue(int capacity) {
        queue = new int[capacity];
        this.capacity = capacity;
        this.initialCapacity = capacity;
    }

    /**
     * Creates a {@link IntQueue} with the default capacity.
     */
    public IntQueue() {
        queue = new int[STANDARD_INITIAL_CAPACITY];
        this.capacity = STANDARD_INITIAL_CAPACITY;
        this.initialCapacity = STANDARD_INITIAL_CAPACITY;
    }

    /**
     * Resets the capacity of the queue to the value at initialization.
     */
    public void reset() {
        if (capacity > initialCapacity) {
            queue = new int[initialCapacity];
        }
        size = 0;
        firstItemIndex = 0;
        nextItemIndex = 0;
    }

    /**
     * Checks if the queue is empty.
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Adds a new element to the queue. Calls {@link IntQueue#resize(int)} if the queue is full.
     *
     * @param element
     */
    public void enqueue(int element) {
        if (size >= capacity) {
            resize(Integer.max((int) (capacity * RESIZE_MULTIPLIER), capacity + 1));
        }
        queue[nextItemIndex++] = element;
        if (nextItemIndex == capacity) {
            nextItemIndex = 0;
        }
        size++;
    }

    /**
     * Returns the value at the head of the queue if one exists and removes it from the queue.
     *
     * @return int Dequeued value.
     */
    public int dequeue() throws NoSuchElementException {
        if (size == 0) {
            throw new NoSuchElementException("IntQueue is empty. Cannot dequeue from an empty" +
                " IntQueue.");
        }
        int result = queue[firstItemIndex++];
        if (--size == 0) {
            firstItemIndex = 0;
            nextItemIndex = 0;
        }
        return result;
    }

    /**
     * Returns the value at the head of the queue if one exists without actually dequeuing it.
     *
     * @return int Value at the head of the queue.
     */
    public int peekNext() throws NoSuchElementException {
        if (size == 0) {
            throw new NoSuchElementException("IntQueue is empty. Cannot peek the first " +
                "element of an empty IntQueue.");
        }
        return queue[firstItemIndex];
    }

    /**
     * Returns the number of items in the query.
     *
     * @return int The number of items in the query.
     */
    public int getSize() {
        return size;
    }

    @UsedOnlyByTests
    void setSize(int size) {
        this.size = size;
    }

    private void resize(int newCapacity) {
        int[] temp = new int[newCapacity];
        for (int i = 0; i < size; i++) {
            temp[i] = queue[(firstItemIndex + i) % this.capacity];
        }
        queue = temp;
        nextItemIndex = size;
        firstItemIndex = 0;
        this.capacity = newCapacity;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int length = (firstItemIndex < nextItemIndex) ? nextItemIndex : firstItemIndex + size
            - nextItemIndex;
        for (int i = 0; i < length - 1; i++) {
            builder.append(queue[i] + ", ");
        }
        if (length > 0) {
            builder.append(queue[length - 1]);
        }
        builder.append("]");
        return builder.toString();
    }

    @UsedOnlyByTests
    int getFirstItemIndex() {
        return firstItemIndex;
    }

    @UsedOnlyByTests
    void setFirstItemIndex(int firstItemIndex) {
        this.firstItemIndex = firstItemIndex;
    }

    @UsedOnlyByTests
    int getNextItemIndex() {
        return nextItemIndex;
    }

    @UsedOnlyByTests
    void setNextItemIndex(int nextItemIndex) {
        this.nextItemIndex = nextItemIndex;
    }
}

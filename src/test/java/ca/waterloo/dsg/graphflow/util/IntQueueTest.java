package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@code IntQueue}.
 */
public class IntQueueTest {

    @Test
    public void testEnqueueAdjustsFistAndNextItemIndicesAndSize() throws Exception {
        IntQueue intQueue = new IntQueue();
        intQueue.enqueue(2);
        Assert.assertEquals(0, intQueue.getFirstItemIndex());
        Assert.assertEquals(1, intQueue.getNextItemIndex());
        Assert.assertEquals(1, intQueue.getSize());
    }

    @Test
    public void testEnqueueWrapsAroundNextItemIndex() throws Exception {
        IntQueue intQueue = new IntQueue(10);
        intQueue.setFirstItemIndex(4);
        intQueue.setNextItemIndex(9);
        intQueue.setSize(5);
        intQueue.enqueue(2);
        Assert.assertEquals(4, intQueue.getFirstItemIndex());
        Assert.assertEquals(0, intQueue.getNextItemIndex());
        Assert.assertEquals(6, intQueue.getSize());
    }

    @Test
    public void testResizeAdjustsFirstAndNextItemIndicesAndSize() throws Exception {
        IntQueue intQueue = new IntQueue(10);
        intQueue.setFirstItemIndex(4);
        intQueue.setNextItemIndex(2);
        intQueue.setSize(10);
        intQueue.enqueue(2);
        Assert.assertEquals(0, intQueue.getFirstItemIndex());
        Assert.assertEquals(11, intQueue.getNextItemIndex());
        Assert.assertEquals(11, intQueue.getSize());
    }

    @Test
    public void testDequeueFirstAdjustsFirstAndNextItemIndicesAndSize() throws Exception {
        IntQueue intQueue = new IntQueue(10);
        intQueue.enqueue(2);
        intQueue.enqueue(3);
        intQueue.dequeue();
        Assert.assertEquals(1, intQueue.getFirstItemIndex());
        Assert.assertEquals(2, intQueue.getNextItemIndex());
        Assert.assertEquals(1, intQueue.getSize());
    }

    @Test
    public void testDequeueLastAdjustsFirstAndNextItemIndicesAndSize() throws Exception {
        IntQueue intQueue = new IntQueue(10);
        intQueue.setFirstItemIndex(9);
        intQueue.setNextItemIndex(0);
        intQueue.setSize(1);
        intQueue.dequeue();
        Assert.assertEquals(0, intQueue.getFirstItemIndex());
        Assert.assertEquals(0, intQueue.getNextItemIndex());
        Assert.assertEquals(0, intQueue.getSize());
    }

    @Test
    public void test5Enqueue3Dequeue() throws Exception {
        IntQueue intQueue = new IntQueue();
        intQueue.enqueue(0);
        int result = intQueue.dequeue();
        Assert.assertEquals(0, result);
        intQueue.enqueue(1);
        intQueue.enqueue(2);
        result = intQueue.dequeue();
        Assert.assertEquals(1, result);
        intQueue.enqueue(4);
        intQueue.enqueue(5);
        result = intQueue.dequeue();
        Assert.assertEquals(2, result);
    }

    @Test
    public void test5Enqueue4Dequeue() throws Exception {
        IntQueue intQueue = new IntQueue();
        intQueue.enqueue(0);
        int result = intQueue.dequeue();
        Assert.assertEquals(0, result);
        intQueue.enqueue(1);
        intQueue.enqueue(2);
        result = intQueue.dequeue();
        Assert.assertEquals(1, result);
        intQueue.enqueue(3);
        intQueue.enqueue(4);
        result = intQueue.dequeue();
        Assert.assertEquals(2, result);
        intQueue.enqueue(4);
        intQueue.enqueue(5);
        result = intQueue.dequeue();
        Assert.assertEquals(3, result);
    }
}
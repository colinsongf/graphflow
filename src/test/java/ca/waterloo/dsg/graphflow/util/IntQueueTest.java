package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@code IntQueue}.
 */
public class IntQueueTest {
    private IntQueue intQueue;
    @Before
    public void setUp() throws Exception {
        intQueue = new IntQueue(10);
    }

    @Test
    public void testPutFirst() throws Exception {
        intQueue.put(2);
        Assert.assertEquals(0, intQueue.getFirst());
        Assert.assertEquals(1, intQueue.getNext());
        Assert.assertEquals(1, intQueue.size());
    }

    @Test
    public void testPutLast() throws Exception {
        intQueue.setFirst(4);
        intQueue.setNext(9);
        intQueue.setSize(5);
        intQueue.put(2);
        Assert.assertEquals(4, intQueue.getFirst());
        Assert.assertEquals(0, intQueue.getNext());
        Assert.assertEquals(6, intQueue.size());
    }

    @Test
    public void testPutResize() throws Exception {
        intQueue.setFirst(4);
        intQueue.setNext(4);
        intQueue.setSize(10);
        intQueue.put(2);
        Assert.assertEquals(0, intQueue.getFirst());
        Assert.assertEquals(11, intQueue.getNext());
        Assert.assertEquals(11, intQueue.size());
    }

    @Test
    public void testGetFirst() throws Exception {
        intQueue.put(2);
        intQueue.put(3);
        intQueue.get();
        Assert.assertEquals(1, intQueue.getFirst());
        Assert.assertEquals(2, intQueue.getNext());
        Assert.assertEquals(1, intQueue.size());
    }

    @Test
    public void testGetLast() throws Exception {
        intQueue.setFirst(9);
        intQueue.setNext(0);
        intQueue.setSize(1);
        intQueue.get();
        Assert.assertEquals(0, intQueue.getFirst());
        Assert.assertEquals(0, intQueue.getNext());
        Assert.assertEquals(0, intQueue.size());
    }

    @Test
    public void testGetAndPut() throws Exception {
        intQueue.put(0);
        int result = intQueue.get();
        Assert.assertEquals(0, result);
        intQueue.put(1);
        intQueue.put(2);
        result = intQueue.get();
        Assert.assertEquals(1, result);
        intQueue.put(4);
        intQueue.put(5);
        result = intQueue.get();
        Assert.assertEquals(2, result);
    }

    @Test
    public void testGetAndPut0to5() throws Exception {
        intQueue.put(0);
        int result = intQueue.get();
        Assert.assertEquals(0, result);
        intQueue.put(1);
        intQueue.put(2);
        result = intQueue.get();
        Assert.assertEquals(1, result);
        intQueue.put(3);
        intQueue.put(4);
        result = intQueue.get();
        Assert.assertEquals(2, result);
        intQueue.put(4);
        intQueue.put(5);
        result = intQueue.get();
        Assert.assertEquals(3, result);
    }
}
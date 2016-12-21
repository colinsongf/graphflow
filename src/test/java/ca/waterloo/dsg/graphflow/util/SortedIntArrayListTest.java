package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@code SortedIntArrayList}.
 */
public class SortedIntArrayListTest {

    @Test
    public void testCreationAndSearch() throws Exception {
        SortedIntArrayList testList;
        // Test empty list.
        testList = new SortedIntArrayList();
        Assert.assertEquals(-1, testList.search(42));
        // Insert values.
        int[] randomList = {74, 21, 7, 93, 42, 3, 27};
        testList = new SortedIntArrayList();
        testList.addAll(randomList);
        testList.add(10);
        testList.add(2);
        testList.add(99);
        // Test search. The elements should be present in a increasing sorted order.
        Assert.assertEquals(0, testList.search(2));
        Assert.assertEquals(1, testList.search(3));
        Assert.assertEquals(2, testList.search(7));
        Assert.assertEquals(3, testList.search(10));
        Assert.assertEquals(4, testList.search(21));
        Assert.assertEquals(5, testList.search(27));
        Assert.assertEquals(6, testList.search(42));
        Assert.assertEquals(7, testList.search(74));
        Assert.assertEquals(8, testList.search(93));
        Assert.assertEquals(9, testList.search(99));
        Assert.assertEquals(-1, testList.search(1));
        Assert.assertEquals(-1, testList.search(30));
        Assert.assertEquals(-1, testList.search(172));
        Assert.assertEquals(-1, testList.search(-122));
    }

    @Test
    public void testGetIntersection() throws Exception {
        int[] sortedList = {2, 4, 7, 23, 23, 45, 56, 76, 78};
        SortedIntArrayList testList = new SortedIntArrayList();
        testList.addAll(sortedList);
        int[] otherList = {4, 23, 456, 56, 43, 76};
        SortedIntArrayList testList2 = new SortedIntArrayList();
        testList2.addAll(otherList);
        SortedIntArrayList intersection = testList.getIntersection(testList2);
        int[] results = {4, 23, 56, 76};
        Assert.assertArrayEquals(results, intersection.toArray());
    }
}

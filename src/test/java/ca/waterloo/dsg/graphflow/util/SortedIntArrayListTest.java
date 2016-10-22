package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the @code{SortedIntArrayList} class.
 */
public class SortedIntArrayListTest {

  @Test
  public void testSearch() throws Exception {
    int[] sortedList = {2, 4, 7, 23, 23, 45, 56, 76, 78};
    SortedIntArrayList testList = new SortedIntArrayList();
    testList.addAll(sortedList);
    int index = testList.search(7);
    Assert.assertEquals(2, index);
    index = testList.search(750);
    Assert.assertEquals(-1, index);
    index = testList.search(-750);
    Assert.assertEquals(-1, index);
    testList = new SortedIntArrayList();
    index = testList.search(7);
    Assert.assertEquals(-1, index);
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
    System.out.println(intersection);
    int[] results = {4, 23, 56, 76};
    Assert.assertArrayEquals(results, intersection.toArray());
  }
}
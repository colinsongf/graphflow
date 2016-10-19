package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the IntArrayList class.
 */
public class IntArrayListTest {

  @Test
  public void search() throws Exception {
    int[] sortedList = {2, 4, 7, 23, 23, 45, 56, 76, 78};
    IntArrayList testList = new IntArrayList();
    testList.addAll(sortedList);
    int index = testList.search(7);
    Assert.assertEquals(2, index);
    index = testList.search(750);
    Assert.assertEquals(-1, index);
    index = testList.search(-750);
    Assert.assertEquals(-1, index);

    testList = new IntArrayList();
    index = testList.search(7);
    Assert.assertEquals(-1, index);
  }

  @Test
  public void getIntersection() throws Exception {
    int[] sortedList = {2, 4, 7, 23, 23, 45, 56, 76, 78};
    IntArrayList testList = new IntArrayList();
    testList.addAll(sortedList);
    int[] otherList = {4, 23, 456, 56, 43, 76};
    IntArrayList testList2 = new IntArrayList();
    testList2.addAll(otherList);
    IntArrayList intersection = testList.getIntersection(testList2);
    System.out.println(intersection);
    int[] results = {4, 23, 56, 76};
    Assert.assertArrayEquals(results, intersection.toArray());
  }

  @Test
  public void sort() throws Exception {

    int[] unsortedList = {4, 7, 23, 76, 2, 56, 23, 78, 45};
    IntArrayList testList = new IntArrayList();
    testList.addAll(unsortedList);
    testList.sort();
    int[] sortedList = {2, 4, 7, 23, 23, 45, 56, 76, 78};

    Assert.assertArrayEquals(sortedList, testList.toArray());
  }

}
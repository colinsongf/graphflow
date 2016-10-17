package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the IntArrayList class
 */
public class IntArrayListTest {

  @Test
  public void search() throws Exception {

  }

  @Test
  public void getIntersection() throws Exception {

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
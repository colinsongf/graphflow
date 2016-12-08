package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link ShortArrayList}.
 */
public class ShortArrayListTest {

    private ShortArrayList shortArrayList;

    @Test
    public void testAdd() throws Exception {
        short[] inputValues = {12, 23, 45, 34, 67, 12, 67, 34, 89, 12};
        constructShortArrayListWithValues(inputValues);
        Assert.assertEquals(inputValues.length, shortArrayList.getSize());
        Assert.assertArrayEquals(inputValues, shortArrayList.toArray());
    }

    private void constructShortArrayListWithValues(short[] inputValues) {
        shortArrayList = new ShortArrayList();
        for (short value : inputValues) {
            shortArrayList.add(value);
        }
    }

    @Test
    public void testSetIndexLessThanCapacity() throws Exception {
        short[] inputValues = {12, 23, 45, 34, 67, 12, 67, 34, 89, 12};
        constructShortArrayListWithValues(inputValues);
        shortArrayList.set(2, (short) 36);
        Assert.assertEquals(36, shortArrayList.get(2));
    }

    @Test
    public void testSetIndexMoreThanCapacity() throws Exception {
        short[] inputValues = {12, 23, 45, 34, 67, 12, 67, 34, 89, 12};
        constructShortArrayListWithValues(inputValues);
        shortArrayList.set(20, (short) 36);
        Assert.assertEquals(36, shortArrayList.get(20));
        Assert.assertEquals(21, shortArrayList.getSize());
    }
}

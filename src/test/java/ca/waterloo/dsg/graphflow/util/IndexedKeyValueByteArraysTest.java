package ca.waterloo.dsg.graphflow.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * Tests {@code SortedIntArrayList}.
 */
public class IndexedKeyValueByteArraysTest {

    private IndexedKeyValueByteArrays testIndexedKeyValueByteArrays = new
        IndexedKeyValueByteArrays();

    @Test
    public void testAppendAllAndGetAllMethods() {
        HashMap<Short,String> keyValues = new HashMap<Short,String>();
        keyValues.put((short) 2,"Barca");
        keyValues.put((short) 1, "Madrid");
        testIndexedKeyValueByteArrays.set(3, keyValues);
        Assert.assertEquals(12 + "Barca".length() + ("Madrid").length(),
            testIndexedKeyValueByteArrays.getSize(3));

        keyValues.clear();
        keyValues.put((short) 0, "Liverpool");
        keyValues.put((short) 5, "Chelsea");
        testIndexedKeyValueByteArrays.set(3, keyValues);
        Assert.assertEquals(12 + "Liverpool".length() + "Chelsea".length(),
            testIndexedKeyValueByteArrays.getSize(3));
    }
}

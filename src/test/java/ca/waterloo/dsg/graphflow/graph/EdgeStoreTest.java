package ca.waterloo.dsg.graphflow.graph;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Tests {@code EdgeStore}.
 */
public class EdgeStoreTest {

    @Test
    public void testGetNextIDNeverYetAssignedMethod() {
        EdgeStore testEdgeStore = new EdgeStore();
        Assert.assertEquals(0, testEdgeStore.getNextIdToAssign());
        Assert.assertEquals(1, testEdgeStore.getNextIdToAssign());
        testEdgeStore.deleteEdge(0);
        Assert.assertEquals(0, testEdgeStore.getNextIdToAssign());
        testEdgeStore.deleteEdge(1);
        Assert.assertEquals(1, testEdgeStore.getNextIdToAssign());

        testEdgeStore.setNextIDParams(1,24, (byte) 2);
        testEdgeStore.getNextIdToAssign(); // This discards the 2 and updates based on the new IDs.
        Assert.assertEquals((long) Math.pow(2,40) + 24 * 256 + 2 + 1, testEdgeStore.
            getNextIdToAssign());
    }

    @Test
    public void testSetAndGetPropertiesMethod() {
        EdgeStore testEdgeStore = new EdgeStore();
        HashMap<Short, String> properties = new HashMap<>();
        properties.put((short) 0, "fruit1");
        properties.put((short) 1, "fruit2");
        properties.put((short) 2, "fruit3");
        testEdgeStore.addEdge(properties);
        Assert.assertEquals(36, testEdgeStore.edgePropertyData[0][0].length);
        Assert.assertEquals(36, testEdgeStore.edgePropertyDataOffsets[0][0][1]);
        testEdgeStore.setProperties(1, null);
        Assert.assertEquals(36, testEdgeStore.edgePropertyData[0][0].length);
        Assert.assertEquals(36, testEdgeStore.edgePropertyDataOffsets[0][0][2]);
        testEdgeStore.setProperties(2, properties);
        Assert.assertEquals(72, testEdgeStore.edgePropertyData[0][0].length);
        testEdgeStore.setProperties(3, properties);
        Assert.assertEquals(36 * 3, testEdgeStore.edgePropertyData[0][0].length);

        properties.put((short) 3, "fruit4");
        properties.put((short) 4, "fruit5");
        testEdgeStore.setProperties(4, properties);
        Assert.assertEquals(12 * (9 + 5), testEdgeStore.edgePropertyData[0][0].length);
        testEdgeStore.setProperties(2, properties);
        Assert.assertEquals(12 * (6 + 10), testEdgeStore.edgePropertyData[0][0].length);

        byte[] fruit1 = new byte[6];
        System.arraycopy(testEdgeStore.edgePropertyData[0][0], 6, fruit1, 0, 6);
        Assert.assertEquals(new String(fruit1, StandardCharsets.UTF_8), "fruit1");

        HashMap<Short, String> propertiesStored = testEdgeStore.getEdgeProperties(0);
        Assert.assertEquals("fruit1", propertiesStored.get((short) 0));
    }
}

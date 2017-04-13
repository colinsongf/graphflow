package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.DataType;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests {@code EdgeStore}.
 */
public class EdgeStoreTest {

    private String[] values = {"Fruit", "Ninja", "NinjaFruit", "139999", "77", "true", "14.0"};
    private short[] keys = {0, 1, 2, 3, 4, 5, 6};
    private Map<Short, Pair<DataType, String>> propertiesOfEdgeToAdd = new HashMap<>();
    private int propertiesLengthInBytes;

    @Before
    public void resetGraphDB() {
        GraphDBState.reset();
    }

    private void populateTypeStoreAndPropertiesMap() {
        for (short i = 0; i < 3; ++i) {
            TypeAndPropertyKeyStore.getInstance().propertyDataTypeStore.put(keys[i], DataType.
                STRING);
        }
        for (short i = 3; i < 5; ++i) {
            TypeAndPropertyKeyStore.getInstance().propertyDataTypeStore.put(keys[i], DataType.INTEGER);
        }
        TypeAndPropertyKeyStore.getInstance().propertyDataTypeStore.put(keys[5], DataType.BOOLEAN);
        TypeAndPropertyKeyStore.getInstance().propertyDataTypeStore.put(keys[6], DataType.DOUBLE);

        for (short i = 0; i < 3; ++i) {
            propertiesOfEdgeToAdd.put(keys[i], new Pair<>(DataType.STRING, values[i]));
        }
        propertiesLengthInBytes = 3 * 6 /* 2 bytes for short key + 4 bytes for int length */ +
            values[0].length() + values[1].length() + values[2].length();
    }

    @Test
    public void testGetNextIdNeverYetAssignedInsertsFrom0AndGivesLastDeletedId() {
        Assert.assertEquals(0, EdgeStore.getInstance().getNextIdToAssign());
        Assert.assertEquals(1, EdgeStore.getInstance().getNextIdToAssign());
        EdgeStore.getInstance().deleteEdge(0);
        EdgeStore.getInstance().deleteEdge(1);
        Assert.assertEquals(1, EdgeStore.getInstance().getNextIdToAssign());
        Assert.assertEquals(0, EdgeStore.getInstance().getNextIdToAssign());
    }

    @Test
    public void testGetNextIdNeverYetAssignedConstructsIdCorrectly() {
        EdgeStore.getInstance().setNextIDNeverYetAssigned(1 /* partition ID */, 24 /* bucket ID */,
            (byte) 2 /* offset ID */);
        Assert.assertEquals((long) Math.pow(2, 40) + 24 * 256 + 2, EdgeStore.getInstance().
            getNextIdToAssign());
    }

    @Test
    public void testAddEdgeAndVerifyProperties() {
        populateTypeStoreAndPropertiesMap();
        // Adds an edge with ID 0. Ensures length of the bucket and dataOffset are set correctly.
        EdgeStore.getInstance().addEdge(propertiesOfEdgeToAdd);

        Map<Short, Object> propertiesStored = EdgeStore.getInstance().getProperties(0);
        Assert.assertEquals(3 /* setUp stores 3 properties */, propertiesStored.size());
        for (int i = 0; i < propertiesOfEdgeToAdd.size(); ++i) {
            Assert.assertEquals(values[i], propertiesStored.get(keys[i]));
        }
        Assert.assertEquals(propertiesLengthInBytes, EdgeStore.getInstance().data[0][0].length);
        Assert.assertEquals(propertiesLengthInBytes, EdgeStore.getInstance().dataOffsets[0][0][1]);
    }

    @Test
    public void testAddEdgeWithNoPropertiesAtStartMiddleAndEndOfABucket() {
        populateTypeStoreAndPropertiesMap();
        EdgeStore edgeStore = EdgeStore.getInstance();

        // Add 7 edges with IDs from 0 to 6.
        edgeStore.addEdge(null);
        edgeStore.addEdge(propertiesOfEdgeToAdd);
        EdgeStore.getInstance().addEdge(null);
        for (int i = 0; i < 4; ++i) {
            edgeStore.addEdge(propertiesOfEdgeToAdd);
        }
        EdgeStore.getInstance().addEdge(null);

        Map<Short, Object> propertiesStored = edgeStore.getProperties(0 /* edge ID */);
        Assert.assertEquals(0, propertiesStored.size());
        propertiesStored = edgeStore.getProperties(2 /* edge ID */);
        Assert.assertEquals(0, propertiesStored.size());
        propertiesStored = edgeStore.getProperties(7 /* edge ID */);
        Assert.assertEquals(0, propertiesStored.size());

        Assert.assertEquals(5 * propertiesLengthInBytes, edgeStore.data[0][0].length);
        Assert.assertEquals(0, edgeStore.dataOffsets[0][0][0]);
        Assert.assertEquals(0, edgeStore.dataOffsets[0][0][1]);
        Assert.assertEquals(propertiesLengthInBytes, edgeStore.dataOffsets[0][0][2]);
        Assert.assertEquals(propertiesLengthInBytes, edgeStore.dataOffsets[0][0][3]);
        Assert.assertEquals(2 * propertiesLengthInBytes, edgeStore.dataOffsets[0][0][4]);
        Assert.assertEquals(3 * propertiesLengthInBytes, edgeStore.dataOffsets[0][0][5]);
        Assert.assertEquals(4 * propertiesLengthInBytes, edgeStore.dataOffsets[0][0][6]);
        Assert.assertEquals(5 * propertiesLengthInBytes, edgeStore.dataOffsets[0][0][7]);
    }

    @Test
    public void testAddEdgeWithMultipleDataTypeProperties() {
        populateTypeStoreAndPropertiesMap();
        for (short i = 0; i < 3; ++i) {
            propertiesOfEdgeToAdd.put(keys[i], new Pair<>(DataType.STRING, values[i]));
        }
        for (short i = 3; i < 5; ++i) {
            propertiesOfEdgeToAdd.put(keys[i], new Pair<>(DataType.INTEGER, values[i]));
        }
        propertiesOfEdgeToAdd.put(keys[5], new Pair<>(DataType.BOOLEAN, values[5]));
        propertiesOfEdgeToAdd.put(keys[6], new Pair<>(DataType.DOUBLE, values[6]));

        EdgeStore.getInstance().addEdge(propertiesOfEdgeToAdd);

        Map<Short, Object> propertiesStored = EdgeStore.getInstance().getProperties(
            0 /* edge ID */);
        Assert.assertEquals(values.length, propertiesStored.size());
        for (int i = 0; i < 3; ++i) {
            Assert.assertEquals(values[i], propertiesStored.get(keys[i]));
        }
        for (int i = 3; i < 5; ++i) {
            Assert.assertEquals(Integer.parseInt(values[i]), propertiesStored.get(keys[i]));
        }
        Assert.assertEquals(Boolean.parseBoolean(values[5]), propertiesStored.get(keys[5]));
        Assert.assertEquals(Double.parseDouble(values[6]), propertiesStored.get(keys[6]));
    }

    @Test
    public void testAddEdgeWithLargeEdgeIds() {
        populateTypeStoreAndPropertiesMap();
        EdgeStore edgeStore = EdgeStore.getInstance();

        edgeStore.setNextIDNeverYetAssigned(0 /* partition ID */, 10 /* bucket ID */,
            (byte) 5 /* offset ID */);
        edgeStore.addEdge(propertiesOfEdgeToAdd);
        Assert.assertEquals(propertiesLengthInBytes, edgeStore.data[0][10].length);
        for (int i = 0; i < EdgeStore.MAX_EDGES_PER_BUCKET; ++i) {
            if (i <= 5) {
                Assert.assertEquals(0, edgeStore.dataOffsets[0][10][i]);
            } else {
                Assert.assertEquals(propertiesLengthInBytes, edgeStore.dataOffsets[0][10][6]);
            }
        }

        Map<Short, Object> propertiesStored = EdgeStore.getInstance().getProperties(10 * 256 + 5);
        for (int i = 0; i < propertiesOfEdgeToAdd.size(); ++i) {
            Assert.assertEquals(values[i], propertiesStored.get(keys[i]));
        }

        edgeStore.setNextIDNeverYetAssigned(1 /* partition ID */, 24 /* bucket ID */,
            (byte) 2 /* offset ID */);
        EdgeStore.getInstance().addEdge(propertiesOfEdgeToAdd); // added edge has offset ID 3.
        Assert.assertEquals(propertiesLengthInBytes, edgeStore.data[1][24].length);
        for (int i = 0; i < EdgeStore.MAX_EDGES_PER_BUCKET; ++i) {
            if (i <= 2) {
                Assert.assertEquals(0, edgeStore.dataOffsets[1][24][i]);
            } else {
                Assert.assertEquals(propertiesLengthInBytes, edgeStore.dataOffsets[1][24][i]);
            }
        }
        propertiesStored = EdgeStore.getInstance().getProperties((long) Math.pow(2, 40) +
            24 * 256 + 2);
        for (int i = 0; i < propertiesOfEdgeToAdd.size(); ++i) {
            Assert.assertEquals(values[i], propertiesStored.get(keys[i]));
        }
    }
}

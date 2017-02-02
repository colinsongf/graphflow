package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.DataType;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

/**
 * Tests {@code VertexProperyStore}.
 */
public class VertexProperyStoreTest {

    @Test
    public void testSetMethod() {
        VertexPropertyStore vertexPropertyStore = new VertexPropertyStore();

        HashMap<Short, Pair<DataType, String>> keyToDataTypeValuePairMap = new HashMap<>();
        keyToDataTypeValuePairMap.put((short) 2, new Pair<>(DataType.STRING, "Barca"));
        keyToDataTypeValuePairMap.put((short) 1, new Pair<>(DataType.STRING, "Madrid"));
        vertexPropertyStore.set(2, keyToDataTypeValuePairMap);

        Assert.assertEquals(null, vertexPropertyStore.vertexProperties[0]);
        Assert.assertEquals(null, vertexPropertyStore.vertexProperties[1]);
        Assert.assertEquals(12 + "Barca".length() + "Madrid".length(), vertexPropertyStore.
            vertexProperties[2].length);
    }
}

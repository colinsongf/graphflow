package ca.waterloo.dsg.graphflow.query.utils;

import org.junit.Assert;
import org.junit.Test;

public class StructuredQueryEdgeTest {

    private StructuredQueryEdge obj_equal_1 = new StructuredQueryEdge("a", "b");
    private StructuredQueryEdge obj_equal_2 = new StructuredQueryEdge("a", "b");
    private StructuredQueryEdge obj_different_1 = new StructuredQueryEdge("a", "c");

    @Test
    public void equalObjects() throws Exception {
        Assert.assertTrue(obj_equal_1.equalsTo(obj_equal_2));
    }

    @Test
    public void notEqualObjects() throws Exception {
        Assert.assertFalse(obj_equal_1.equalsTo(obj_different_1));
        Assert.assertFalse(obj_equal_2.equalsTo(obj_different_1));
    }
}

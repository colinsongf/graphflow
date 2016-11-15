package ca.waterloo.dsg.graphflow.query.utils;

import org.junit.Assert;
import org.junit.Test;

public class StructuredQueryEdgeTest {

    private StructuredQueryEdge obj_equal_1 = new StructuredQueryEdge("a", "b");
    private StructuredQueryEdge obj_equal_2 = new StructuredQueryEdge("a", "b");
    private StructuredQueryEdge obj_different_1 = new StructuredQueryEdge("a", "c");

    @Test
    public void testEqualObjects() throws Exception {
        Assert.assertTrue(obj_equal_1.isSameAs(obj_equal_2));
    }

    @Test
    public void testNotEqualObjects() throws Exception {
        Assert.assertFalse(obj_equal_1.isSameAs(obj_different_1));
        Assert.assertFalse(obj_equal_2.isSameAs(obj_different_1));
    }
}

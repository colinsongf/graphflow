package ca.waterloo.dsg.graphflow.query.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StructuredQueryTest {

    private StructuredQuery obj_equal_1 = new StructuredQuery();
    private StructuredQuery obj_equal_2 = new StructuredQuery();
    private StructuredQuery obj_different_1 = new StructuredQuery();
    private StructuredQuery obj_different_2 = new StructuredQuery();

    @Before
    public void setup() {
        obj_equal_1.addEdge(new StructuredQueryEdge("a", "b"));
        obj_equal_1.addEdge(new StructuredQueryEdge("b", "c"));
        obj_equal_1.addEdge(new StructuredQueryEdge("c", "a"));

        obj_equal_2.addEdge(new StructuredQueryEdge("a", "b"));
        obj_equal_2.addEdge(new StructuredQueryEdge("b", "c"));
        obj_equal_2.addEdge(new StructuredQueryEdge("c", "a"));

        obj_different_1.addEdge(new StructuredQueryEdge("a", "b"));
        obj_different_1.addEdge(new StructuredQueryEdge("b", "c"));

        obj_different_2.addEdge(new StructuredQueryEdge("a", "b"));
        obj_different_2.addEdge(new StructuredQueryEdge("b", "c"));
        obj_different_2.addEdge(new StructuredQueryEdge("c", "d"));
    }

    @Test
    public void equalObjects() throws Exception {
        Assert.assertTrue(obj_equal_1.equalsTo(obj_equal_2));
    }

    @Test
    public void notEqualObjects() throws Exception {
        Assert.assertFalse(obj_equal_1.equalsTo(obj_different_1));
        Assert.assertFalse(obj_equal_2.equalsTo(obj_different_1));
        Assert.assertFalse(obj_equal_2.equalsTo(obj_different_2));
        Assert.assertFalse(obj_equal_2.equalsTo(obj_different_2));
        Assert.assertFalse(obj_different_1.equalsTo(obj_different_2));
    }
}

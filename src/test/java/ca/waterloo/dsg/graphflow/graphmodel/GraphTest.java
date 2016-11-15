package ca.waterloo.dsg.graphflow.graphmodel;

import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Tests {@code Graph} class.
 */
public class GraphTest {

    @Test
    public void testCreateInstance() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("graph.json").getPath());
        Graph g = GraphBuilder.createInstance(file);
        System.out.println(g);
        Assert.assertEquals(6, g.getVertexCount());
        int[] result = {2};
        Assert.assertArrayEquals(result, g.getAdjacencyList(1, true).toArray());
    }

    @Test
    public void testGetVertices() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("graph.json").getPath());
        Graph g = GraphBuilder.createInstance(file);
        SortedIntArrayList vertices = g.getVertices();
        Assert.assertEquals(6, vertices.getSize());
    }
}

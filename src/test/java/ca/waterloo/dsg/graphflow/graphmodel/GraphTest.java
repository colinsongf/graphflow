package ca.waterloo.dsg.graphflow.graphmodel;

import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Tests graphmodel.Graph class.
 */
public class GraphTest {
  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testGetInstance() throws Exception {
    String testFile = "src/test/Fixtures/graph.json";
    File file = new File(testFile);
    Graph g = Graph.getInstance(file);
    Assert.assertEquals(6, g.getVertexCount());
    int[] result = {2};
    Assert.assertArrayEquals(result, g.getAdjacencyList(1, true).toArray());
  }

  @Test
  public void testGetVertices() throws Exception {
    String testFile = "src/test/Fixtures/graph.json";
    File file = new File(testFile);
    Graph g = Graph.getInstance(file);
    SortedIntArrayList vertices = g.getVertices();
    Assert.assertEquals(6, vertices.size());
  }
}

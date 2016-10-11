package ca.waterloo.dsg.graphflow.queryprocessor;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Tests for GenericJoinProcessor.
 */
public class GenericJoinProcessorTest {

  Graph testGraph;
  GenericJoinProcessor processor;

  @Before
  public void setUp() throws Exception {
    String testFile = "src/test/Fixtures/graph.json";
    File file = new File(testFile);
    testGraph = Graph.getInstance(file);

    processor = new GenericJoinProcessor(testGraph);
  }

  @Test
  public void processTriangles() throws Exception {
    ArrayList<ArrayList<Integer>> triangles =  processor.processTriangles();
    System.out.println(triangles.toString());
    Assert.assertEquals(3, triangles.size());
  }

}
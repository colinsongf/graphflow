package ca.waterloo.dsg.graphflow.queryprocessor;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Tests PrefixExtender class.
 */
public class PrefixExtenderTest {


  Graph testGraph;
  PrefixExtender extender;

  @Before
  public void setUp() throws Exception {
    String testFile = "src/test/Fixtures/graph.json";
    File file = new File(testFile);
    testGraph = Graph.getInstance(file);
    ArrayList<ArrayList<Integer>> prefixes = new ArrayList<>(2);

    prefixes.add(new ArrayList<Integer>());
    prefixes.get(0).add(1);
    prefixes.add(new ArrayList<Integer>());
    prefixes.get(1).add(3);

    extender = new PrefixExtender(prefixes, 0, true, testGraph);
  }


  @Test
  public void getPrefixNodes() throws Exception {

    int[] prefixNodes = extender.getPrefixNodes();
    Assert.assertArrayEquals(new int[]{1,3},prefixNodes);

    extender = new PrefixExtender(null, -1, false, testGraph);
    prefixNodes = extender.getPrefixNodes();
    Assert.assertArrayEquals(new int[]{0,1,2,3,4,5},prefixNodes);
  }

  @Test
  public void count() throws Exception {

    int count = extender.count();
    Assert.assertEquals(2, count);

  }

  @Test
  public void propose() throws Exception {
    ArrayList<Integer> proposals = extender.propose();
    Assert.assertArrayEquals(new Integer[]{2,0}, proposals.toArray());
  }

  @Test
  public void intersect() throws Exception {
    ArrayList<Integer> proposals = new ArrayList<>();
    proposals.add(2);
    proposals.add(0);
    proposals.add(4);
    proposals.add(5);

    ArrayList<ArrayList<Integer>> intersections = extender.intersect(proposals);
    Assert.assertEquals(2, intersections.size());
    System.out.println(intersections);
    ArrayList<ArrayList<Integer>> prefixes = new ArrayList<>(2);

    prefixes.add(new ArrayList<Integer>());
    prefixes.get(0).add(1);
    prefixes.add(new ArrayList<Integer>());
    prefixes.get(1).add(3);
    extender = new PrefixExtender(prefixes, 0, false, testGraph);
    intersections = extender.intersect(proposals);
    System.out.println(intersections);
  }

}
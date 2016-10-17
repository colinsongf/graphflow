package ca.waterloo.dsg.graphflow.queryprocessor;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.util.IntArrayList;
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
    IntArrayList[] prefixes = new IntArrayList[2];
    prefixes[0] = new IntArrayList();
    prefixes[0].add(1);
    prefixes[1] = new IntArrayList();
    prefixes[1].add(3);

    extender = new PrefixExtender(0, true, testGraph);
    extender.setPrefixes(prefixes);
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
    IntArrayList proposals = extender.propose();
    int[] results = {0, 2};
    Assert.assertArrayEquals(results, proposals.toArray());
  }

  @Test
  public void intersect() throws Exception {
    IntArrayList proposals = new IntArrayList();
    proposals.add(2);
    proposals.add(0);
    proposals.add(4);
    proposals.add(5);
    IntArrayList[] intersections = extender.intersect(proposals);
    Assert.assertEquals(2, intersections.length);

    IntArrayList[] prefixes = new IntArrayList[2];
    prefixes[0] = new IntArrayList();
    prefixes[0].add(1);
    prefixes[1] = new IntArrayList();
    prefixes[1].add(3);
    extender = new PrefixExtender(prefixes, 0, false, testGraph);
    intersections = extender.intersect(proposals);
    Assert.assertEquals(2, intersections.length);

    extender = new PrefixExtender(prefixes, -1, false, testGraph);
    intersections = extender.intersect(proposals);
    Assert.assertEquals(2, intersections.length);
    for(IntArrayList intersection: intersections) {
      Assert.assertEquals(3, intersection.size());
    }
  }

}
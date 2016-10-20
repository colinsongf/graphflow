package ca.waterloo.dsg.graphflow.queryprocessor;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.queryprocessor.outputsink.FileOutputSink;
import ca.waterloo.dsg.graphflow.queryprocessor.outputsink.OutputSink;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Tests for GenericJoinProcessor.
 */
public class GenericJoinProcessorTest {

  Graph testGraph;
  GenericJoinProcessor processor;
  OutputSink outputSink;
  String outputDir;

  @Before
  public void setUp() throws Exception {
    String testFile = "src/test/Fixtures/graph.json";
    File file = new File(testFile);
    testGraph = Graph.getInstance(file);
    outputDir = "src/test/Fixtures/generated";
    System.out.println(testGraph);
    processor = new GenericJoinProcessor();
  }

  @Test
  public void processTriangles() throws Exception {
    String name = "triangles.out";
    outputSink = new FileOutputSink(new File(outputDir), name);
    processor.processTriangles(outputSink);
  }

  @Test
  public void processSquares() throws Exception {
    String name = "squares.out";
    outputSink = new FileOutputSink(new File(outputDir), name);
    processor.processSquares(outputSink);
  }
}

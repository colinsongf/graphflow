package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.outputsink.ShortestPathOutputSink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests {@code ShortestPathProcesso}
 */
public class ShortestPathProcessorTest {

    Graph graph;
    ShortestPathProcessor processor;
    ShortestPathOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("graph2.json").getPath());
        graph = Graph.createInstance(file);
        outputSink = new ShortestPathOutputSink();
        processor = new ShortestPathProcessor(graph, outputSink);
    }

    @Test
    public void testEvaluate0To9() throws Exception {
        processor.evaluate(0, 9);
        Assert.assertEquals(8, outputSink.getResults().size());
        int[] vertices = Arrays.stream(outputSink.getResults().keySet().toArray(new Integer[outputSink
            .getResults().keySet().size()]))
            .mapToInt(Integer::intValue).toArray();
        Assert.assertArrayEquals(new int[]{0, 1, 2, 3, 4, 5, 6, 7}, vertices);
//        Assert.assertArrayEquals(new int[]{0, 1, 4, 6, 9}, memSink.getResults().get(1));
//        Assert.assertArrayEquals(new int[]{0, 2, 5, 7, 9}, memSink.getResults().get(2));
    }

    @Test
    public void testEvaluate1to4() throws Exception {
        processor.evaluate(1, 4);
        Assert.assertEquals(1, outputSink.getResults().size());
    }

    @Test
    public void testBackTrackIntersection0To9() throws Exception {
        processor.setVisitedStages(new short[]{1, 2, 2, 3, 3, 3, -2, -2, 0, -1, 0, 0});
        Map<Integer, Set<Integer>> results = new HashMap<>();
        processor.setResults(results);
        processor.backTrackIntersection(6, (short)4, 0, 9);
         int[] vertices = Arrays.stream(results.keySet().toArray(new
            Integer[results.keySet().size()]))
            .mapToInt(Integer::intValue).toArray();
        System.out.println(Arrays.toString(vertices));
        Assert.assertArrayEquals(new int[]{0, 1, 2, 3, 4, 6}, vertices);
    }
}
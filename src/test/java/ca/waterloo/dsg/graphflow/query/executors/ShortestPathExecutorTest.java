package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.outputsink.ShortestPathOutputSink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests {@link ShortestPathExecutor}.
 */
public class ShortestPathExecutorTest {

    private Graph graph;
    private ShortestPathExecutor executor;
    private ShortestPathOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        graph = new Graph();
        int[][] edges = {{0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 4}, {2, 5}, {3, 6}, {4, 6}, {4, 7},
            {5, 7}, {6, 8}, {6, 9}, {7, 9}, {7, 10}, {8, 11}, {9, 11}, {10, 11}};
        for (int[] edge : edges) {
            graph.addEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();
        outputSink = new ShortestPathOutputSink();
        executor = ShortestPathExecutor.getInstance();
        if (!executor.isInitialized()) {
            executor.init(graph);
        }
    }

    @Test
    public void testEvaluateQuerySource0Target9() throws Exception {
        ShortestPathOutputSink expectedResultOutputSink = new ShortestPathOutputSink();
        expectedResultOutputSink.setResults(new HashMap<>());
        expectedResultOutputSink.getResults().put(0, new HashSet<>(Arrays.asList(new Integer[]{1,
            2})));
        expectedResultOutputSink.getResults().put(1, new HashSet<>(Arrays.asList(new Integer[]{3,
            4})));
        expectedResultOutputSink.getResults().put(2, new HashSet<>(Arrays.asList(new Integer[]{4,
            5})));
        expectedResultOutputSink.getResults().put(3, new HashSet<>(Arrays.asList(new
            Integer[]{6})));
        expectedResultOutputSink.getResults().put(4, new HashSet<>(Arrays.asList(new Integer[]{6,
            7})));
        expectedResultOutputSink.getResults().put(5, new HashSet<>(Arrays.asList(new
            Integer[]{7})));
        expectedResultOutputSink.getResults().put(6, new HashSet<>(Arrays.asList(new
            Integer[]{9})));
        expectedResultOutputSink.getResults().put(7, new HashSet<>(Arrays.asList(new
            Integer[]{9})));
        executor.evaluate(0, 9, outputSink);
        Assert.assertTrue(expectedResultOutputSink.isSameAs(outputSink));
    }

    @Test
    public void testEvaluateQuerySource1Target4() throws Exception {
        executor.evaluate(1, 4, outputSink);
        Assert.assertEquals(1, outputSink.getResults().size());
    }

    @Test
    public void testBackTrackIntersectionSource0Target9() throws Exception {
        short[] visitedLevels = new short[]{1, 2, 2, 3, 3, 3, -2, -2, 0, -1, 0, 0};
        int[] visitedQueryId = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0};
        int queryId = 1;
        executor = new ShortestPathExecutor(graph, visitedLevels, visitedQueryId, queryId);
        Map<Integer, Set<Integer>> results = new HashMap<>();
        Set<Integer> intersectNodes = new HashSet<>();
        intersectNodes.add(6);
        intersectNodes.add(7);
        executor.backTrackIntersection(intersectNodes, Direction.BACKWARD, (short) 4, results);
        Map<Integer, Set<Integer>> expectedResults = new HashMap<>();
        Integer[] adjList0 = {1, 2};
        Integer[] adjList1 = {3, 4};
        Integer[] adjList2 = {4, 5};
        Integer[] adjList3 = {6};
        Integer[] adjList4 = {6, 7};
        Integer[] adjList5 = {7};
        expectedResults.put(0, new HashSet<>(Arrays.asList(adjList0)));
        expectedResults.put(1, new HashSet<>(Arrays.asList(adjList1)));
        expectedResults.put(2, new HashSet<>(Arrays.asList(adjList2)));
        expectedResults.put(3, new HashSet<>(Arrays.asList(adjList3)));
        expectedResults.put(4, new HashSet<>(Arrays.asList(adjList4)));
        expectedResults.put(5, new HashSet<>(Arrays.asList(adjList5)));
        Assert.assertTrue(expectedResults.equals(results));
    }
}
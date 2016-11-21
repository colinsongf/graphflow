package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
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

    @Before
    public void setUp() throws Exception {
        graph = new Graph();
        int[][] edges = {{0, 1}, {0, 2}, {1, 3}, {1, 4}, {2, 4}, {2, 5}, {3, 6}, {4, 6}, {4, 7},
            {5, 7}, {6, 8}, {6, 9}, {7, 9}, {7, 10}, {8, 11}, {9, 11}, {10, 11}};
        for (int[] edge : edges) {
            graph.addEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();
        executor = ShortestPathExecutor.getInstance();
        if (!executor.isInitialized()) {
            executor.init(graph);
        }
    }

    @Test
    public void testEvaluateQuerySource0Target9() throws Exception {
        Map<Integer, Set<Integer>> expectedResults = new HashMap<>();
        expectedResults.put(0, new HashSet<>(Arrays.asList(new Integer[]{1, 2})));
        expectedResults.put(1, new HashSet<>(Arrays.asList(new Integer[]{3, 4})));
        expectedResults.put(2, new HashSet<>(Arrays.asList(new Integer[]{4, 5})));
        expectedResults.put(3, new HashSet<>(Arrays.asList(new Integer[]{6})));
        expectedResults.put(4, new HashSet<>(Arrays.asList(new Integer[]{6, 7})));
        expectedResults.put(5, new HashSet<>(Arrays.asList(new Integer[]{7})));
        expectedResults.put(6, new HashSet<>(Arrays.asList(new Integer[]{9})));
        expectedResults.put(7, new HashSet<>(Arrays.asList(new Integer[]{9})));
        InMemoryOutputSink expectedInMemoryOutputSink = new InMemoryOutputSink();
        expectedInMemoryOutputSink.append(ShortestPathExecutor.getStringOutput(expectedResults));
        expectedInMemoryOutputSink.append(ShortestPathExecutor.getStringOutput(expectedResults));

        InMemoryOutputSink actualInMemoryOutputSink = new InMemoryOutputSink();
        executor.execute(0, 9, actualInMemoryOutputSink);

        Assert.assertTrue(actualInMemoryOutputSink.isSameAs(expectedInMemoryOutputSink));
    }

    @Test
    public void testBackTrackIntersectionSource0Target9() throws Exception {
        short[] visitedLevels = new short[]{1, 2, 2, 3, 3, 3, -2, -2, 0, -1, 0, 0};
        int[] visitedQueryId = new int[]{1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0};
        int queryId = 1;
        executor = new ShortestPathExecutor(graph, visitedLevels, visitedQueryId, queryId);
        Map<Integer, Set<Integer>> actualResults = new HashMap<>();
        Set<Integer> intersectNodes = new HashSet<>();
        intersectNodes.add(6);
        intersectNodes.add(7);
        executor.backTrackIntersection(intersectNodes, Direction.BACKWARD, (short) 4,
            actualResults);

        Map<Integer, Set<Integer>> expectedResults = new HashMap<>();
        expectedResults.put(0, new HashSet<>(Arrays.asList(new Integer[]{1, 2})));
        expectedResults.put(1, new HashSet<>(Arrays.asList(new Integer[]{3, 4})));
        expectedResults.put(2, new HashSet<>(Arrays.asList(new Integer[]{4, 5})));
        expectedResults.put(3, new HashSet<>(Arrays.asList(new Integer[]{6})));
        expectedResults.put(4, new HashSet<>(Arrays.asList(new Integer[]{6, 7})));
        expectedResults.put(5, new HashSet<>(Arrays.asList(new Integer[]{7})));

        Assert.assertTrue(expectedResults.equals(actualResults));
    }
}

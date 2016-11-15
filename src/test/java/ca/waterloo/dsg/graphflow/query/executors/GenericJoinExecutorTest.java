package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@code GenericJoinExecutor}.
 */
public class GenericJoinExecutorTest {

    @Test
    public void testProcessTriangles() throws Exception {
        // Create the stages for the triangle query.
        List<List<GenericJoinIntersectionRule>> triangleQueryStages = new ArrayList<>();
        List<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.FORWARD));
        triangleQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        triangleQueryStages.add(stage);

        int[][] results1 = {{0, 1, 3}, {1, 3, 0}, {1, 3, 4}, {3, 0, 1}, {3, 4, 1}, {4, 1, 3}};
        int[][] results2 = {{0, 1, 3}, {1, 3, 0}, {3, 0, 1}};
        executeGJ(triangleQueryStages, results1, results2);
    }

    @Test
    public void testProcessSquares() throws Exception {
        // Create the stages for the triangle query.
        List<List<GenericJoinIntersectionRule>> squareQueryStages = new ArrayList<>();
        List<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.FORWARD));
        squareQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        squareQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(2, EdgeDirection.FORWARD));
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        squareQueryStages.add(stage);

        int[][] results1 =
            {{0, 1, 2, 3}, {1, 2, 3, 0}, {1, 2, 3, 4}, {2, 3, 0, 1}, {2, 3, 4, 1}, {3, 0, 1, 2},
                {3, 4, 1, 2}, {4, 1, 2, 3}};
        int[][] results2 = {{0, 1, 2, 3}, {1, 2, 3, 0}, {2, 3, 0, 1}, {3, 0, 1, 2}};
        executeGJ(squareQueryStages, results1, results2);
    }

    private void executeGJ(List<List<GenericJoinIntersectionRule>> stages, int[][] results1,
        int[][] results2) {
        Graph graph = new Graph();
        InMemoryOutputSink outputSink;
        GenericJoinExecutor genericJoinExecutor;

        // Initialize the graph.
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {1, 3}, {3, 4}, {3, 0}, {4, 1}};
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Execute the triangle query and test.
        outputSink = new InMemoryOutputSink();
        genericJoinExecutor = new GenericJoinExecutor(stages, outputSink, graph);
        genericJoinExecutor.execute();
        Assert.assertArrayEquals(results1, outputSink.getResults().toArray());

        // Delete one of the edges.
        int[][] deletedEdges = {{4, 1}};
        for (int[] edge : deletedEdges) {
            graph.deleteEdge(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Execute the triangle query again and test.
        outputSink = new InMemoryOutputSink();
        genericJoinExecutor = new GenericJoinExecutor(stages, outputSink, graph);
        genericJoinExecutor.execute();
        Assert.assertArrayEquals(results2, outputSink.getResults().toArray());
    }
}

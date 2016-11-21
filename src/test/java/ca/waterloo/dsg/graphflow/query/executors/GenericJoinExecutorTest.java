package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
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
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD));
        triangleQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD));
        triangleQueryStages.add(stage);

        int[][] expectedMotifsAfterAdditions = {{0, 1, 3}, {1, 3, 0}, {1, 3, 4}, {3, 0, 1},
            {3, 4, 1}, {4, 1, 3}};
        int[][] expectedMotifsAfterDeletion = {{0, 1, 3}, {1, 3, 0}, {3, 0, 1}};
        assertGenericJoinOutput(triangleQueryStages, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    @Test
    public void testProcessSquares() throws Exception {
        // Create the stages for the square query.
        List<List<GenericJoinIntersectionRule>> squareQueryStages = new ArrayList<>();
        List<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD));
        squareQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD));
        squareQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(2, Direction.FORWARD));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD));
        squareQueryStages.add(stage);

        int[][] expectedMotifsAfterAdditions = {{0, 1, 2, 3}, {1, 2, 3, 0}, {1, 2, 3, 4},
            {2, 3, 0, 1}, {2, 3, 4, 1}, {3, 0, 1, 2}, {3, 4, 1, 2}, {4, 1, 2, 3}};
        int[][] expectedMotifsAfterDeletion = {{0, 1, 2, 3}, {1, 2, 3, 0}, {2, 3, 0, 1},
            {3, 0, 1, 2}};
        assertGenericJoinOutput(squareQueryStages, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    private void assertGenericJoinOutput(List<List<GenericJoinIntersectionRule>> stages,
        int[][] expectedMotifsAfterAdditions, int[][] expectedMotifsAfterDeletion) {
        Graph graph = new Graph();
        InMemoryOutputSink outputSink;

        // Initialize a graph.
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {1, 3}, {3, 4}, {3, 0}, {4, 1}};
        for (int[] edge : edges) {
            graph.addEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Execute the query and test.
        outputSink = new InMemoryOutputSink();
        new GenericJoinExecutor(stages, outputSink, graph).execute();
        Assert.assertTrue(outputSink.isSameAs(getInMemoryOutputSinkForMotifs(
            expectedMotifsAfterAdditions)));

        // Delete one of the edges.
        int[][] deletedEdges = {{4, 1}};
        for (int[] edge : deletedEdges) {
            graph.deleteEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Execute the query again and test.
        outputSink = new InMemoryOutputSink();
        new GenericJoinExecutor(stages, outputSink, graph).execute();
        Assert.assertTrue(outputSink.isSameAs(getInMemoryOutputSinkForMotifs(
            expectedMotifsAfterDeletion)));
    }

    private InMemoryOutputSink getInMemoryOutputSinkForMotifs(int[][] motifs) {
        InMemoryOutputSink inMemoryOutputSink = new InMemoryOutputSink();
        for (int[] motif : motifs) {
            inMemoryOutputSink.append(GenericJoinExecutor.getStringOutput(motif,
                MatchQueryResultType.MATCHED));
        }
        return inMemoryOutputSink;
    }
}

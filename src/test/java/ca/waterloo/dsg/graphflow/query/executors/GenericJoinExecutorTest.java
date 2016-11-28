package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@code GenericJoinExecutor}.
 */
public class GenericJoinExecutorTest {
    private Graph graph;

    private short defaultId = TypeStore.ANY_TYPE;

    @Test
    public void testProcessTriangles() throws Exception {
        // Create the stages for the triangle query.
        List<List<GenericJoinIntersectionRule>> triangleQueryStages = new ArrayList<>();
        List<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            defaultId));
        triangleQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            defaultId));
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
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            defaultId));
        squareQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            defaultId));
        squareQueryStages.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(2, Direction.FORWARD,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            defaultId));
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

        InMemoryOutputSink outputSink;

        // Initialize a graph.
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {1, 3}, {3, 4}, {3, 0}, {4, 1}};
        short[] edgeTypes = {5, 6, 7, 7, 8, 4, 5};
        short[][] vertexTypes = {{0, 4}, {4, 8}, {8, 12}, {4, 12}, {12, 16}, {12, 0}, {16, 4}};
        graph = TestUtils.initializeGraph(edges, edgeTypes, vertexTypes);
        // Execute the query and test.
        outputSink = new InMemoryOutputSink();
        new GenericJoinExecutor(stages, outputSink, graph).execute();
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, getInMemoryOutputSinkForMotifs(
            expectedMotifsAfterAdditions)));

        // Delete one of the edges.
        int[] deletedEdge = {4, 1};
        short deletedEdgeType = 5;
        graph.deleteEdgeTemporarily(deletedEdge[0], deletedEdge[1], deletedEdgeType);
        graph.finalizeChanges();
        // Execute the query again and test.
        outputSink = new InMemoryOutputSink();
        new GenericJoinExecutor(stages, outputSink, graph).execute();
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, getInMemoryOutputSinkForMotifs(
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

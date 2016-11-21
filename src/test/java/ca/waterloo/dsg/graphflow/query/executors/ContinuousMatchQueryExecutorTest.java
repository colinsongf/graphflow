package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.planner.ContinuousMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.junit.Test;

/**
 * Tests for {@code GenericJoinExecutor}.
 */
public class ContinuousMatchQueryExecutorTest {

    /**
     * Tests the execution of a triangle Delta Generic Join query.
     */
    @Test
    public void testProcessTriangles() throws Exception {
        Graph graph = new Graph();
        InMemoryOutputSink outputSink;

        // Create a triangle Delta Generic Join plan.
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.addEdge(new StructuredQueryEdge("b", "a"));
        structuredQuery.addEdge(new StructuredQueryEdge("a", "c"));
        structuredQuery.addEdge(new StructuredQueryEdge("c", "b"));
        ContinuousMatchQueryPlan continuousMatchQueryPlan = (ContinuousMatchQueryPlan) new
            ContinuousMatchQueryPlanner(structuredQuery, new InMemoryOutputSink()).plan();

        // Initialize a graph.
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {1, 3}, {3, 4}, {3, 0}, {4, 1}};
        for (int[] edge : edges) {
            graph.addEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Create a diff graph by temporarily adding and deleting edges.
        graph.addEdgeTemporarily(2, 0);
        graph.deleteEdgeTemporarily(3, 4);
        graph.deleteEdgeTemporarily(1, 2);
        // Execute the Delta Generic Join query.
        outputSink = new InMemoryOutputSink();
        continuousMatchQueryPlan.execute(graph);
        graph.finalizeChanges();
    }
}

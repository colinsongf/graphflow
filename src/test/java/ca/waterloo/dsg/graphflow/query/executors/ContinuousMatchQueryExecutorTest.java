package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.planner.ContinuousMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariable;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import org.junit.Test;

/**
 * Tests for {@code GenericJoinExecutor}.
 */
public class ContinuousMatchQueryExecutorTest {
    private Graph graph;
    /**
     * Tests the execution of a triangle Delta Generic Join query.
     */
    @Test
    public void testProcessTriangles() throws Exception {
        InMemoryOutputSink outputSink;

        // Create a triangle Delta Generic Join plan.
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.addEdge(new QueryEdge(new QueryVariable("b"),new QueryVariable("a")));
        structuredQuery.addEdge(new QueryEdge(new QueryVariable("a"),new QueryVariable("c")));
        structuredQuery.addEdge(new QueryEdge(new QueryVariable("c"),new QueryVariable("b")));
        ContinuousMatchQueryPlan continuousMatchQueryPlan = (ContinuousMatchQueryPlan) new
            ContinuousMatchQueryPlanner(structuredQuery, new InMemoryOutputSink()).plan();

        // Initialize a graph.
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {1, 3}, {3, 4}, {3, 0}, {4, 1}};
        short[] edgeTypes = {2, 4, 6, 6, 8, 0, 2};
        short[][] vertexTypes = {{10, 11}, {11, 12}, {12, 13}, {11, 13}, {13, 14}, {13, 10}, {14,
            11}};
        graph = TestUtils.initializeGraph(edges, edgeTypes, vertexTypes);

        // Create a diff graph by temporarily adding and deleting edges.
        graph.addEdgeTemporarily(2, 0, (short)12, (short)10, (short)0);
        graph.deleteEdgeTemporarily(3, 4, (short)8);
        graph.deleteEdgeTemporarily(1, 2, (short)4);
        // Execute the Delta Generic Join query.
        outputSink = new InMemoryOutputSink();
        continuousMatchQueryPlan.execute(graph);
        graph.finalizeChanges();
    }
}

package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.OneTimeMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.QueryOutputUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@code GenericJoinExecutor}.
 */
public class GenericJoinExecutorTest {

    @Before
    public void setUp() throws Exception {
        Graph.getInstance().reset();
    }

    /**
     * Tests the execution of a simple path query with no types.
     */
    @Test
    public void testPathQueryWithoutTypes() throws Exception {
        StructuredQuery pathQueryPlan = new StructuredQueryParser().parse("MATCH (a)->(b)");
        int[][] expectedMotifsAfterAddition = {{0, 1}, {1, 2}, {1, 3}, {2, 3}, {3, 0}, {3, 4},
            {4, 1}, {4, 4}};
        int[][] expectedMotifsAfterDeletion = {{0, 1}, {1, 2}, {1, 3}, {2, 3}, {3, 0}, {3, 4}, {4, 4}};
        assertSimpleMatchQueryOutput(pathQueryPlan, expectedMotifsAfterAddition,
            expectedMotifsAfterDeletion);
    }

    /**
     * Tests the execution of a simple path query with types.
     */
    @Test
    public void testPathQueryWithTypes() throws Exception {
        StructuredQuery pathQueryPlan = new StructuredQueryParser().parse("MATCH (a)-[:FOLLOWS]->" +
            "(b)");
        int[][] expectedMotifsAfterAddition = {{0, 1}, {3, 0}, {3, 4}, {4, 1}};
        int[][] expectedMotifsAfterDeletion = {{3, 0}, {3, 4}, {4, 1}};
        assertComplexMatchQueryOutput(pathQueryPlan, expectedMotifsAfterAddition,
            expectedMotifsAfterDeletion);
    }

    /**
     * Tests the execution of a triangle query with no types.
     */
    @Test
    public void testTriangleQueryWithoutTypes() throws Exception {
        // Create a one time MATCH query plan for a simple triangle query with no types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)->(b),(b)->(c),(c)->(a)");
        int[][] expectedMotifsAfterAdditions = {{0, 1, 3}, {1, 3, 0}, {1, 3, 4}, {3, 0, 1},
            {3, 4, 1}, {4, 1, 3}, {4, 4, 4}};
        int[][] expectedMotifsAfterDeletion = {{0, 1, 3}, {1, 3, 0}, {3, 0, 1}, {4, 4, 4}};
        assertSimpleMatchQueryOutput(triangleStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    /**
     * Tests the execution of a triangle query with types.
     */
    @Test
    public void testTriangleQueryWithTypes() throws Exception {
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("FOLLOWS");
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("LIKES");
        // Create a one time MATCH query plan for a complex triangle query with multiple
        // relations between variable having different edge types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[:FOLLOWS]->(b),(a)-[:LIKES]->(b),(b)-[:LIKES]->(a),(b)->(c),(c)->(b)," +
            "(c)-[:FOLLOWS]->(a)");
        int[][] expectedMotifsAfterAdditions = {{1, 0, 3}, {1, 4, 3}};
        int[][] expectedMotifsAfterDeletion = {{1, 4, 3}};
        assertComplexMatchQueryOutput(triangleStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    /**
     * Tests the execution of a square query with no types.
     */
    @Test
    public void testSquareQueryWithoutTypes() throws Exception {
        // Create a one time MATCH query plan for a simple square query with no types.
        StructuredQuery squareStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)->(b),(b)->(c),(c)->(d),(d)->(a)");
        int[][] expectedMotifsAfterAdditions = {{0, 1, 2, 3}, {1, 2, 3, 0}, {1, 2, 3, 4},
            {1, 3, 4, 4}, {2, 3, 0, 1}, {2, 3, 4, 1}, {3, 0, 1, 2}, {3, 4, 1, 2}, {3, 4, 4, 1},
            {4, 1, 2, 3}, {4, 1, 3, 4}, {4, 4, 1, 3}, {4, 4, 4, 4}};
        int[][] expectedMotifsAfterDeletion = {{0, 1, 2, 3}, {1, 2, 3, 0}, {2, 3, 0, 1},
            {3, 0, 1, 2}, {4, 4, 4, 4}};
        assertSimpleMatchQueryOutput(squareStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    /**
     * Tests the execution of a square query with types.
     */
    @Test
    public void testSquareQueryWithTypes() {
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("FOLLOWS");
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("LIKES");
        //Create a one time MATCH query plan for a square pattern with types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[:FOLLOWS]->(b),(b)-[:LIKES]->(c),(c)-[:LIKES]->(d),(d)-[:FOLLOWS]->(a)");
        int[][] expectedMotifsAfterAdditions = {{0, 1, 4, 3}, {4, 1, 4, 3}};
        int[][] expectedMotifsAfterDeletion = {{4, 1, 4, 3}};

        assertComplexMatchQueryOutput(triangleStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    /**
     * Tests the execution of a diamond query with no types.
     */
    @Test
    public void testDiamondQueryWithoutTypes() throws Exception {
        //Create a a one time MATCH query plan for a simple diamond pattern with no types.
        StructuredQuery diamondStructuredQuery = new StructuredQueryParser().parse("MATCH (a)->" +
            "(b),(a)->(c),(b)->(d),(c)->(d)");
        int[][] expectedMotifsAfterAdditions = {{0, 1, 1, 2}, {0, 1, 1, 3}, {1, 2, 2, 3},
            {1, 3, 3, 0}, {1, 3, 3, 4}, {2, 3, 3, 0}, {2, 3, 3, 4}, {3, 0, 0, 1}, {3, 0, 4, 1},
            {3, 4, 0, 1}, {3, 4, 4, 1}, {3, 4, 4, 4}, {4, 1, 1, 2}, {4, 1, 1, 3}, {4, 4, 4, 1},
            {4, 4, 4, 4}};
        int[][] expectedMotifsAfterDeletion = {{0, 1, 1, 2}, {0, 1, 1, 3}, {1, 2, 2, 3},
            {1, 3, 3, 0}, {1, 3, 3, 4}, {2, 3, 3, 0}, {2, 3, 3, 4}, {3, 0, 0, 1}, {3, 4, 4, 4},
            {4, 4, 4, 4}};

        assertSimpleMatchQueryOutput(diamondStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    /**
     * Tests the execution of a diamond query with types.
     */
    @Test
    public void testDiamondQueryWithTypes() throws Exception{
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("FOLLOWS");
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("LIKES");
        //Create a one time MATCH query plan for a simple diamond pattern with types.
        StructuredQuery diamondStructuredQuery = new StructuredQueryParser().parse("MATCH (a)" +
            "-[:FOLLOWS]->(b),(a)-[:FOLLOWS]->(c),(b)-[:LIKES]->(d),(c)-[:LIKES]->(d)");
        int[][] expectedMotifsAfterAdditions = {{0, 1, 1, 0}, {0, 1, 1, 4}, {3, 0, 0, 1},
            {3, 0, 4, 1}, {3, 4, 0, 1}, {3, 4, 4, 1}, {3, 4, 4, 3}, {4, 1, 1, 0}, {4, 1, 1, 4}};
        int[][] expectedMotifsAfterDeletion = {{3, 0, 0, 1}, {3, 0, 4, 1}, {3, 4, 0, 1},
            {3, 4, 4, 1}, {3, 4, 4, 3}, {4, 1, 1, 0}, {4, 1, 1, 4}};

        assertComplexMatchQueryOutput(diamondStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }


    private void assertSimpleMatchQueryOutput(StructuredQuery structuredQuery,
        int[][] expectedMotifsAfterAdditions, int[][] expectedMotifsAfterDeletion) {

        // Initialize a graph.
        Graph graph = Graph.getInstance();
        TestUtils.createEdgesPermanently(graph, "CREATE (0:Person)-[:FOLLOWS]->" +
            "(1:Person),(1:Person)-[:FOLLOWS]->(2:Person), (1:Person)-[:FOLLOWS]->(3:Person)," +
            "(2:Person)-[:FOLLOWS]->(3:Person), (3:Person)-[:FOLLOWS]->(4:Person)," +
            "(3:Person)-[:FOLLOWS]->(0:Person), (4:Person)-[:FOLLOWS]->(1:Person)," +
            "(4:Person)-[:LIKES]->(4:Person);");

        // Execute the query and test.
        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        OneTimeMatchQueryPlan actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan) new
            OneTimeMatchQueryPlanner(structuredQuery, outputSink).plan();
        actualOneTimeMatchQueryPlan.execute(graph);
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, getInMemoryOutputSinkForMotifs(
            expectedMotifsAfterAdditions)));

        // Delete one of the edges.
        TestUtils.deleteEdgesPermanently(graph, "DELETE (4)->(1);");

        // Execute the query again and test.
        outputSink = new InMemoryOutputSink();
        actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(
            structuredQuery, outputSink).plan();
        actualOneTimeMatchQueryPlan.execute(graph);
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, getInMemoryOutputSinkForMotifs(
            expectedMotifsAfterDeletion)));
    }

    private void assertComplexMatchQueryOutput(StructuredQuery structuredQuery,
        int[][] expectedMotifsAfterAdditions, int[][] expectedMotifsAfterDeletion) {
        Graph graph = Graph.getInstance();
        TestUtils.createEdgesPermanently(graph, "CREATE (0:Person)-[:FOLLOWS]->" +
            "(1:Person),(0:Person)-[:LIKES]->(1:Person),(1:Person)-[:LIKES]->(0:Person)," +
            "(1:Person)-[:TAGGED]->(3:Person),(3:Person)-[:LIKES]->(1:Person)," +
            "(3:Person)-[:FOLLOWS]->(0:Person),(4:Person)-[:FOLLOWS]->(1:Person)," +
            "(4:Person)-[:LIKES]->(1:Person),(1:Person)-[:LIKES]->(4:Person)," +
            "(3:Person)-[:FOLLOWS]->(4:Person), (4:Person)-[:LIKES]->(3:Person);");

        // Execute the query and test.

        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        OneTimeMatchQueryPlan actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan) new
            OneTimeMatchQueryPlanner(structuredQuery, outputSink).plan();
        actualOneTimeMatchQueryPlan.execute(graph);
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, getInMemoryOutputSinkForMotifs(
            expectedMotifsAfterAdditions)));

        // Delete one of the edges.
        TestUtils.deleteEdgesPermanently(graph, "DELETE (0)-[:FOLLOWS]->(1);");

        // Execute the query again and test.
        outputSink = new InMemoryOutputSink();
        actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(
            structuredQuery, outputSink).plan();
        actualOneTimeMatchQueryPlan.execute(graph);
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, getInMemoryOutputSinkForMotifs(
            expectedMotifsAfterDeletion)));
    }

    private InMemoryOutputSink getInMemoryOutputSinkForMotifs(int[][] motifs) {
        InMemoryOutputSink inMemoryOutputSink = new InMemoryOutputSink();
        for (int[] motif : motifs) {
            inMemoryOutputSink.append(QueryOutputUtils.getStringMatchQueryOutput(motif,
                MatchQueryResultType.MATCHED));
        }
        return inMemoryOutputSink;
    }
}

package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.OneTimeMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests {@code GenericJoinExecutor}.
 */
public class GenericJoinExecutorTest {

    @Before
    public void setUp() throws Exception {
        GraphDBState.reset();
    }

    /**
     * Tests the execution of a simple path query with no types.
     */
    @Test
    public void testPathQueryWithoutTypes() throws Exception {
        StructuredQuery pathQueryPlan = new StructuredQueryParser().parse("MATCH (a)->(b)");
        Object[][] expectedMotifsAfterAddition = {{0, 1}, {1, 2}, {1, 3}, {2, 3}, {3, 0}, {3, 4},
            {4, 1}, {4, 4}};
        Object[][] expectedMotifsAfterDeletion = {{0, 1}, {1, 2}, {1, 3}, {2, 3}, {3, 0}, {3, 4},
            {4, 4}};
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
        Object[][] expectedMotifsAfterAddition = {{0, 1}, {3, 0}, {3, 4}, {4, 1}};
        Object[][] expectedMotifsAfterDeletion = {{3, 0}, {3, 4}, {4, 1}};
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
        Object[][] expectedMotifsAfterAdditions = {{0, 1, 3}, {1, 3, 0}, {1, 3, 4}, {3, 0, 1},
            {3, 4, 1}, {4, 1, 3}, {4, 4, 4}};
        Object[][] expectedMotifsAfterDeletion = {{0, 1, 3}, {1, 3, 0}, {3, 0, 1}, {4, 4, 4}};
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
        Object[][] expectedMotifsAfterAdditions = {{1, 0, 3}, {1, 4, 3}};
        Object[][] expectedMotifsAfterDeletion = {{1, 4, 3}};
        assertComplexMatchQueryOutput(triangleStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    @Test
    public void testTriangleQueryWithProjection() {
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("FOLLOWS");
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("LIKES");
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[:FOLLOWS]->(b),(b)->(c),(c)-[:FOLLOWS]->(a) RETURN a, b;");
        Object[][] expectedMotifsAfterAdditions = {{0, 1}, {4, 1}};
        Object[][] expectedMotifsAfterDeletion = {{4, 1}};
        assertComplexMatchQueryOutput(triangleStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    @Test
    public void testTriangleQueryWithPropertyProjections() {
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("FOLLOWS");
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("LIKES");
        Map<String, Pair<String, String>> ageProperty = new HashMap<>();
        ageProperty.put("age", new Pair<>("integer", null));
        TypeAndPropertyKeyStore.getInstance().mapStringPropertiesToShortAndDataTypeOrInsert(
            ageProperty);
        Map<String, Pair<String, String>> viewsProperty = new HashMap<>();
        viewsProperty.put("views", new Pair<>("integer", null));
        TypeAndPropertyKeyStore.getInstance().mapStringPropertiesToShortAndDataTypeOrInsert(
            viewsProperty);
        Map<String, Pair<String, String>> nameProperty = new HashMap<>();
        nameProperty.put("name", new Pair<>("string", null));
        TypeAndPropertyKeyStore.getInstance().mapStringPropertiesToShortAndDataTypeOrInsert(
            nameProperty);
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[d:FOLLOWS]->(b),(b)->(c),(c)-[:FOLLOWS]->(a) " +
            "RETURN a.age, b.views, c.name, d.views;");
        Object[][] expectedMotifsAfterAdditions = {{20, 70, "name3", 60}, {40, 70, "name3", 4}};
        Object[][] expectedMotifsAfterDeletion = {{40, 70, "name3", 4}};
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
        Object[][] expectedMotifsAfterAdditions = {{0, 1, 2, 3}, {1, 2, 3, 0}, {1, 2, 3, 4},
            {1, 3, 4, 4}, {2, 3, 0, 1}, {2, 3, 4, 1}, {3, 0, 1, 2}, {3, 4, 1, 2}, {3, 4, 4, 1},
            {4, 1, 2, 3}, {4, 1, 3, 4}, {4, 4, 1, 3}, {4, 4, 4, 4}};
        Object[][] expectedMotifsAfterDeletion = {{0, 1, 2, 3}, {1, 2, 3, 0}, {2, 3, 0, 1},
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
        Object[][] expectedMotifsAfterAdditions = {{0, 1, 4, 3}, {4, 1, 4, 3}};
        Object[][] expectedMotifsAfterDeletion = {{4, 1, 4, 3}};

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
        Object[][] expectedMotifsAfterAdditions = {{0, 1, 1, 2}, {0, 1, 1, 3}, {1, 2, 2, 3},
            {1, 3, 3, 0}, {1, 3, 3, 4}, {2, 3, 3, 0}, {2, 3, 3, 4}, {3, 0, 0, 1}, {3, 0, 4, 1},
            {3, 4, 0, 1}, {3, 4, 4, 1}, {3, 4, 4, 4}, {4, 1, 1, 2}, {4, 1, 1, 3}, {4, 4, 4, 1},
            {4, 4, 4, 4}};
        Object[][] expectedMotifsAfterDeletion = {{0, 1, 1, 2}, {0, 1, 1, 3}, {1, 2, 2, 3},
            {1, 3, 3, 0}, {1, 3, 3, 4}, {2, 3, 3, 0}, {2, 3, 3, 4}, {3, 0, 0, 1}, {3, 4, 4, 4},
            {4, 4, 4, 4}};

        assertSimpleMatchQueryOutput(diamondStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    /**
     * Tests the execution of a diamond query with types.
     */
    @Test
    public void testDiamondQueryWithTypes() throws Exception {
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("FOLLOWS");
        TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortOrInsert("LIKES");
        //Create a one time MATCH query plan for a simple diamond pattern with types.
        StructuredQuery diamondStructuredQuery = new StructuredQueryParser().parse("MATCH (a)" +
            "-[:FOLLOWS]->(b),(a)-[:FOLLOWS]->(c),(b)-[:LIKES]->(d),(c)-[:LIKES]->(d)");
        Object[][] expectedMotifsAfterAdditions = {{0, 1, 1, 0}, {0, 1, 1, 4}, {3, 0, 0, 1},
            {3, 0, 4, 1}, {3, 4, 0, 1}, {3, 4, 4, 1}, {3, 4, 4, 3}, {4, 1, 1, 0}, {4, 1, 1, 4}};
        Object[][] expectedMotifsAfterDeletion = {{3, 0, 0, 1}, {3, 0, 4, 1}, {3, 4, 0, 1},
            {3, 4, 4, 1}, {3, 4, 4, 3}, {4, 1, 1, 0}, {4, 1, 1, 4}};

        assertComplexMatchQueryOutput(diamondStructuredQuery, expectedMotifsAfterAdditions,
            expectedMotifsAfterDeletion);
    }

    private void assertSimpleMatchQueryOutput(StructuredQuery structuredQuery,
        Object[][] expectedMotifsAfterAdditions, Object[][] expectedMotifsAfterDeletion) {

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
        actualOneTimeMatchQueryPlan.execute();
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, TestUtils.
            getInMemoryOutputSinkForMotifs(expectedMotifsAfterAdditions)));

        // Delete one of the edges.
        TestUtils.deleteEdgesPermanently(graph, "DELETE (4)->(1);");

        // Execute the query again and test.
        outputSink = new InMemoryOutputSink();
        actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(
            structuredQuery, outputSink).plan();
        actualOneTimeMatchQueryPlan.execute();
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, TestUtils.
            getInMemoryOutputSinkForMotifs(expectedMotifsAfterDeletion)));
    }

    private void assertComplexMatchQueryOutput(StructuredQuery structuredQuery,
        Object[][] expectedMotifsAfterAdditions, Object[][] expectedMotifsAfterDeletion) {
        Graph graph = Graph.getInstance();
        TestUtils.initializeGraphPermanentlyWithProperties("CREATE " +
            "(0:Person { name: 'name0', age: 20, views: 120 })-[:FOLLOWS { views: 60 }]" +
            "->(1:Person { name: 'name1', age: 25, views: 70 })," +
            "(0:Person)-[:LIKES { views: 2 }]->(1:Person)," +
            "(1:Person)-[:LIKES { views: 250 }]->(0:Person)," +
            "(1:Person)-[:TAGGED]->(3:Person { name: 'name3', age: 22, views: 250})," +
            "(3:Person)-[:LIKES { views: 44 }]->(1:Person)," +
            "(3:Person)-[:FOLLOWS { views: 234 }]->(0:Person)," +
            "(4:Person{ name: 'name4', age: 40, views: 20})-[:FOLLOWS {views: 4}]->" +
            "(1:Person),(4:Person)-[:LIKES { views: 56 }]->(1:Person)," +
            "(1:Person)-[:LIKES { views: 68 }]->(4:Person)," +
            "(3:Person)-[:FOLLOWS { views: 123 }]->(4:Person)," +
            "(4:Person)-[:LIKES { views: 2 }]->(3:Person);");

        // Execute the query and test.

        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        OneTimeMatchQueryPlan actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan) new
            OneTimeMatchQueryPlanner(structuredQuery, outputSink).plan();
        actualOneTimeMatchQueryPlan.execute();
        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, TestUtils.
            getInMemoryOutputSinkForMotifs(expectedMotifsAfterAdditions)));

        // Delete one of the edges.
        TestUtils.deleteEdgesPermanently(graph, "DELETE (0)-[:FOLLOWS]->(1);");

        // Execute the query again and test.
        outputSink = new InMemoryOutputSink();
        actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(
            structuredQuery, outputSink).plan();
        actualOneTimeMatchQueryPlan.execute();

        Assert.assertTrue(InMemoryOutputSink.isSameAs(outputSink, TestUtils.
            getInMemoryOutputSinkForMotifs(expectedMotifsAfterDeletion)));
    }
}

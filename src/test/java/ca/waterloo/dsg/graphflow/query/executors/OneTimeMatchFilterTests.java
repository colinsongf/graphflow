package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.OneTimeMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * End-to-end tests of the different types of filter queries. Each query matches a triangle
 * pattern against the following graph.
 * <ul>
 * <li>Vertex IDs: 0, 1, 3, 4 , 5</li>
 * <li>Each vertex is of type PERSON and has properties name(string), age(int), views(int).</li>
 * <li>Each vertex is of type FOLLOWS and has the property views(int).</li>
 * </ul>
 * Edges: Form 3 interconnected triangles
 * <ul>
 * <li>0 -> 1, 1 -> 3, 3 -> 0</li>
 * <li>3 -> 4, 4 -> 1, 1 -> 3</li>
 * <li>4 -> 1, 1 -> 5, 5 -> 4</li>
 * </ul>
 */
public class OneTimeMatchFilterTests {

    @Before
    public void setUp() {
        GraphDBState.reset();
        constructGraph();
    }

    @Test
    public void testTwoVertexFilterQuery() {
        String matchQuery = "MATCH (v1)-[e1:FOLLOWS]->(v2),(v2)-[e2:FOLLOWS]->(v3),(v3)" +
            "-[:FOLLOWS]->(v1) WHERE v1.views > v2.views RETURN v1, v2, v3;";
        Object[][] expectedResults = {{0, 1, 3}, {3, 0, 1}, {3, 4, 1}, {5, 4, 1}};
        runTest(matchQuery, expectedResults);
    }

    @Test
    public void testTwoEdgeFilterQuery() {
        String matchQuery = "MATCH (v1)-[e1:FOLLOWS]->(v2),(v2)-[e2:FOLLOWS]->(v3),(v3)" +
            "-[:FOLLOWS]->(v1) WHERE e1.views > e2.views RETURN v1, v2, v3;";
        Object[][] expectedResults = {{0, 1, 3}, {1, 5, 4}, {3, 4, 1}, {4, 1, 3}};
        runTest(matchQuery, expectedResults);
    }

    @Test
    public void testVertexAndEdgeFilterQuery() {
        String matchQuery = "MATCH (v1)-[e1:FOLLOWS]->(v2),(v2)-[e2:FOLLOWS]->(v3),(v3)" +
            "-[:FOLLOWS]->(v1) WHERE v1.views > e1.views RETURN v1, v2, v3;";
        Object[][] expectedResults = {{1, 3, 0}, {1, 3, 4}, {3, 0, 1}, {3, 4, 1}, {5, 4, 1}};
        runTest(matchQuery, expectedResults);
    }

    @Test
    public void testEdgeAndLiteralFilterQuery() {
        String matchQuery = "MATCH (v1)-[e1:FOLLOWS]->(v2),(v2)-[e2:FOLLOWS]->(v3),(v3)" +
            "-[:FOLLOWS]->(v1) WHERE e1.views > 50 RETURN v1, v2, v3;";
        Object[][] expectedResults = {{0, 1, 3}, {1, 5, 4}, {3, 0, 1}};
        runTest(matchQuery, expectedResults);
    }

    @Test
    public void testVertexAndLiteralFilterQuery() {
        String matchQuery = "MATCH (v1)-[e1:FOLLOWS]->(v2),(v2)-[e2:FOLLOWS]->(v3),(v3)" +
            "-[:FOLLOWS]->(v1) WHERE v1.views > 100 RETURN v1, v2, v3;";
        Object[][] expectedResults = {{0, 1, 3}, {3, 0, 1}, {3, 4, 1}, {5, 4, 1}};
        runTest(matchQuery, expectedResults);
    }

    @Test
    public void testOneVariableExistsMultipleTimesInAFilter() {
        String matchQuery = "MATCH (v1)-[e1:FOLLOWS]->(v2),(v2)-[e2:FOLLOWS]->(v3),(v3)" +
            "-[:FOLLOWS]->(v1) WHERE v1.views > v2.views AND v1.views > v3.views " +
            "RETURN v1, v2, v3;";
        Object[][] expectedResults = {{3, 0, 1}, {3, 4, 1}, {5, 4, 1}};
        runTest(matchQuery, expectedResults);
    }

    private void runTest(String query, Object[][] expectedResults) {
        InMemoryOutputSink inMemoryOutputSink = new InMemoryOutputSink();
        ((OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(new StructuredQueryParser().parse(
            query), inMemoryOutputSink).plan()).execute(Graph.getInstance());
        InMemoryOutputSink expectedResultOutputSink = TestUtils.getInMemoryOutputSinkForMotifs(
            expectedResults);
        Assert.assertArrayEquals(expectedResultOutputSink.getResults().toArray(),
            inMemoryOutputSink.getResults().toArray());
    }

    private void constructGraph() {
        TestUtils.initializeGraphPermanentlyWithProperties("CREATE " +
            "(0:Person { name: 'name0', age: 20, views: 120 })" +
            "-[:FOLLOWS { views: 250, is_friends: true }]->" +
            "(1:Person { name: 'name1', age: 25, views: 70 })," +
            "(1:Person)-[:FOLLOWS { views: 12, is_friends: true }]->(0:Person)," +
            "(1:Person)-[:FOLLOWS { views: 40, is_friends: false }]->" +
            "(3:Person { name: 'name3', age: 22, views: 250 })," +
            "(3:Person)-[:FOLLOWS { views: 70, is_friends: true }]->(0:Person), " +
            "(4:Person { name: 'name4', age: 40, views: 20 })-" +
            "[:FOLLOWS { views: 45, is_friends: true }]->(1:Person)," +
            "(3:Person)-[:FOLLOWS { views: 50, is_friends: true }]->(4:Person)," +
            "(5:Person { name: 'name5', age: 30, views: 120 })-" +
            "[:FOLLOWS { views: 35, is_friends: true }]->(4:Person)," +
            "(1:Person)-[:FOLLOWS { views: 250, is_friends: false }]->(5:Person);");
    }
}

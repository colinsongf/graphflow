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
 *     <li>Vertex IDs: 0, 1, 3, 4 , 5</li>
 *     <li>Each vertex is of type PERSON and has properties name(string), age(int), views(int).</li>
 *     <li>Each vertex is of type FOLLOWS and has the property views(int).</li>
 * </ul>
 * Edges: Form 3 interconnected triangles
 * <ul>
 *     <li>0 -> 1, 1 -> 3, 3 -> 0</li>
 *     <li>3 -> 4, 4 -> 1, 1 -> 3</li>
 *     <li>4 -> 1, 1 -> 5, 5 -> 4</li>
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
            "-[:FOLLOWS]->(v1) WHERE v1.views > v2.views AND v1.views > v3.views RETURN v1, v2, " +
            "v3;";
        //Object[][] expectedResults = {{0, 1, 3}, {3, 0, 1}, {3, 4, 1}, {5, 4, 1}};
        Object[][] expectedResults = {/*{0, 1, 3},*/ {3, 0, 1}, {3, 4, 1}, {5, 4, 1}};
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
            "(0:Person{name:string='name0', age:int=20, views:int=120})-[:FOLLOWS{views:int=250,is_friends:boolean=true}]->(1:Person{name:string='name1', age:int=25, views:int=70})," +
            "(1:Person)-[:FOLLOWS{views:int=12, is_friends:boolean=true}]->(0:Person)," +
            "(1:Person)-[:FOLLOWS{views:int=40, is_friends:boolean=false}]->(3:Person{name:string='name3', age:int=22, views:int=250})," +
            "(3:Person)-[:FOLLOWS{views:int=70, is_friends:boolean=true}]->(0:Person), " +
            "(4:Person{name:string='name4', age:int=40, views:int=20})-[:FOLLOWS{views:int=45, is_friends:boolean=true}]->(1:Person)," +
            "(3:Person)-[:FOLLOWS{views:int=50, is_friends:boolean=true}]->(4:Person)," +
            "(5:Person{name:string='name5', age:int=30, views:int=120})-[:FOLLOWS{views:int=35, is_friends:boolean=true}]->(4:Person)," +
            "(1:Person)-[:FOLLOWS{views:int=250, is_friends:boolean=false}]->(5:Person);");
    }
}

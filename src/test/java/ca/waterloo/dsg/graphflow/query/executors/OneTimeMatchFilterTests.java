package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
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
        Graph.getInstance().reset();
        constructGraph();
    }

    @Test
    public void testTwoVertexFilterQuery() {
        String matchquery = "MATCH (a)-[d:FOLLOWS]->(b),(b)-[e:FOLLOWS]->(c),(c)-[:FOLLOWS]->(a) " +
            "WHERE a.views > b.views RETURN a, b, c, a.views, b.views;";
        Object[][] expectedResults = {{0, 1, 3, 120, 70}, {3, 0, 1, 250, 120}, {3, 4, 1, 250, 20},
            {5, 4, 1, 120, 20}};
        runTest(matchquery, expectedResults);
    }

    @Test
    public void testTwoEdgeFilterQuery() {
        String matchquery = "MATCH (a)-[d:FOLLOWS]->(b),(b)-[e:FOLLOWS]->(c),(c)-[:FOLLOWS]->(a) " +
            "WHERE d.views > e.views RETURN a, b, c, d.views, e.views;";
        Object[][] expectedResults = {{0, 1, 3, 250, 40}, {1, 5, 4, 250, 35}, {3, 4, 1, 50, 45},
            {4, 1, 3, 45, 40}};
        runTest(matchquery, expectedResults);
    }

    @Test
    public void testVertexAndEdgeFilerQuery() {
        String matchquery = "MATCH (a)-[d:FOLLOWS]->(b),(b)-[e:FOLLOWS]->(c),(c)-[:FOLLOWS]->(a) " +
            "WHERE a.views > d.views RETURN a, b, c, a.views, d.views;";
        Object[][] expectedResults = {{1, 3, 0, 70, 40}, {1, 3, 4, 70, 40}, {3, 0, 1, 250, 70},
            {3, 4, 1, 250, 50}, {5, 4, 1, 120, 35}};
        runTest(matchquery, expectedResults);
    }

    @Test
    public void testEdgeAndLiteralFilterQuery() {
        String matchquery = "MATCH (a)-[d:FOLLOWS]->(b),(b)-[e:FOLLOWS]->(c),(c)-[:FOLLOWS]->(a) " +
            "WHERE d.views > 50 RETURN a, b, c, d.views;";
        Object[][] expectedResults = {{0, 1, 3, 250}, {1, 5, 4, 250}, {3, 0, 1, 70}};
        runTest(matchquery, expectedResults);
    }

    @Test
    public void testVertexAndLiteralFilterQuery() {
        String matchquery = "MATCH (a)-[d:FOLLOWS]->(b),(b)-[e:FOLLOWS]->(c),(c)-[:FOLLOWS]->(a) " +
            "WHERE a.views > 100 RETURN a, b, c, a.views;";
        Object[][] expectedResults = {{0, 1, 3, 120}, {3, 0, 1, 250}, {3, 4, 1, 250}, {5, 4, 1,
            120}};
        runTest(matchquery, expectedResults);
    }

    private void runTest(String query, Object[][] expectedResults) {
        InMemoryOutputSink inMemoryOutputSink = new InMemoryOutputSink();
        ((OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(new StructuredQueryParser().parse
            (query),
            inMemoryOutputSink).plan()).execute(Graph.getInstance());
        System.out.println(inMemoryOutputSink);
        InMemoryOutputSink expectedResultOutputSink = TestUtils.
            getInMemoryOutputSinkForMotifs(expectedResults);
        Assert.assertArrayEquals(expectedResultOutputSink.getResults().toArray(),
            inMemoryOutputSink.getResults().toArray());
    }

    private void constructGraph() {
        TestUtils.initializeGraphPermanentlyWithProperties("CREATE " +
            "(0:Person{name:string='name0', age:int=20, views:int=120})-[:FOLLOWS{views:int=250, " +
            "is_friends:boolean=true}]->(1:Person{name:string='name1', age:int=25, views:int=70})" +
            "," +
            "(1:Person)-[:FOLLOWS{views:int=12, is_friends:boolean=true}]->(0:Person)," +
            "(1:Person)-[:FOLLOWS{views:int=40, is_friends:boolean=false}]->" +
            "(3:Person{name:string='name3', age:int=22, views:int=250})," +
            "(3:Person)-[:FOLLOWS{views:int=70, is_friends:boolean=true}]->(0:Person)," +
            "(4:Person{name:string='name4', age:int=40, views:int=20})-[:FOLLOWS{views:int=45, " +
            "is_friends:boolean=true}]->(1:Person)," +
            "(3:Person)-[:FOLLOWS{views:int=50, is_friends:boolean=true}]->(4:Person)," +
            "(5:Person{name:string='name5', age:int=30, views:int=120})-[:FOLLOWS{views:int=35, " +
            "is_friends:boolean=true}]->(4:Person)," +
            "(1:Person)-[:FOLLOWS{views:int=250, is_friends:boolean=false}]->(5:Person);");
    }
}

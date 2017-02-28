package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.CreateQueryPlanner;
import ca.waterloo.dsg.graphflow.query.planner.OneTimeMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.CreateQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * End to end tests for group by and aggregation. The tests in this class are executed on a
 * "wheel" graph that consists of 6 vertices with IDs from 0 to 5.The vertices are as follows:
 * <ul>
 *   <li> Vertex i has type: VType{i}.
 *   <li> Vertices 0, 2, and 4 have {@code String} property: strVP:strVPValueE (for "e"ven).
 *   <li> Vertices 1, 3, and 5 have {@code String} property: strVP:strVPValueO (for "o"dd).
 *   <li> Vertex i has intVP:i and doubleVP:{i}.0.
 * </ul>
 * The edges are as follows:
 * <ul>
 *   <li> Vertex 0 has 5 edges to each other vertex. edge(0, i) has:
 *     <ul>
 *       <li> type: StarEdge.
 *       <li> strEP: strEPValue0{i}.
 *       <li> intEP: {i}.
 *       <li> doubleEP: {i}.0.
 *     </ul>
 *   <li> There is the following cycle edges: (1, 2), (2, 3), (3, 4), (4, 5), and (5, 0). Edge
 *   (i, j) has:
 *     <ul>
 *       <li> type: CycleEdge.
 *       <li> strEP: strEPValueO (for "o"dd) if i + j is odd or strEPValueE (for "e"ven) if i + j is even.
 *       <li> intEP: {i + j}.
 *       <li> doubleEP: {i.0 + j.0}.
 *     </ul>
 * </ul>
 *
 */
public class OneTimeMatchQueryGroupByTests {

    @Before
    public void setUp() throws Exception {
        GraphDBState.reset();
        constructTestGraph();
    }

    private void constructTestGraph() {
        String[] verticesInQuery = {
            "(0:VType0{strVP:string='strVPValueE', intVP:int=0, doubleVP:double=0.0})",
            "(1:VType1{strVP:string='strVPValueO', intVP:int=1, doubleVP:double=1.0})",
            "(2:VType2{strVP:string='strVPValueE', intVP:int=2, doubleVP:double=2.0})",
            "(3:VType3{strVP:string='strVPValueO', intVP:int=3, doubleVP:double=3.0})",
            "(4:VType4{strVP:string='strVPValueE', intVP:int=4, doubleVP:double=4.0})",
            "(5:VType5{strVP:string='strVPValueO', intVP:int=5, doubleVP:double=5.0})"};
        StructuredQuery createQuery = new StructuredQueryParser().parse("CREATE " +
           verticesInQuery[0] + "-[:StarEdge{strEP:string='strEPValueO', intEP:int=1, doubleEP:double=1.0}]->"  + verticesInQuery[1] +
           "," + verticesInQuery[0] + "-[:StarEdge{strEP:string='strEPValueE', intEP:int=2, doubleEP:double=2.0}]->" + verticesInQuery[2] +
           "," + verticesInQuery[0] + "-[:StarEdge{strEP:string='strEPValueO', intEP:int=3, doubleEP:double=3.0}]->" + verticesInQuery[3] +
           "," + verticesInQuery[0] + "-[:StarEdge{strEP:string='strEPValueE', intEP:int=4, doubleEP:double=4.0}]->" + verticesInQuery[4] +
           "," + verticesInQuery[0] + "-[:StarEdge{strEP:string='strEPValueO', intEP:int=5, doubleEP:double=5.0}]->" + verticesInQuery[5] +
           "," + verticesInQuery[1] + "-[:CycleEdge{strEP:string='strEPValueO', intEP:int=3, doubleEP:double=3.0}]->" + verticesInQuery[2] +
           "," + verticesInQuery[2] + "-[:CycleEdge{strEP:string='strEPValueO', intEP:int=5, doubleEP:double=5.0}]->" + verticesInQuery[3] +
           "," + verticesInQuery[3] + "-[:CycleEdge{strEP:string='strEPValueO', intEP:int=7, doubleEP:double=7.0}]->" + verticesInQuery[4] +
           "," + verticesInQuery[4] + "-[:CycleEdge{strEP:string='strEPValueO', intEP:int=9, doubleEP:double=9.0}]->" + verticesInQuery[5] +
           "," + verticesInQuery[5] + "-[:CycleEdge{strEP:string='strEPValueE', intEP:int=6, doubleEP:double=6.0}]->" + verticesInQuery[1]
           );

        ((CreateQueryPlan) new CreateQueryPlanner(createQuery).plan()).execute(
            Graph.getInstance(), new InMemoryOutputSink());
    }

    @Test
    public void testAggregateNoGroupByKeyOneAggregation() {
        String queryString = "MATCH (a)->(b) return sum(b.intVP)";
        // There are 10 edges a->b. Each vertex in the outside has 2 edges: So the sum should be
        // 2*(1 + 2 + 3 + 4 + 5) = 30
        runTest(queryString, " 30");
    }

    @Test
    public void testAggregateNoGroupByKeyMultipleAggregations() {
        String queryString = "MATCH (a)->(b) return sum(b.intVP), avg(a.doubleVP)";
        // There are 10 edges a->b. Sum is 30. For average: 0 matches 5 times and contributes 0.0
        // each time. Every other vertex i contributes once ands a value of i.0, totaling 15. 15.0/10 = 1.5.
        runTest(queryString, " 30 1.5");
    }

    @Test
    public void testAggregateSingleGroupByKeySingleAggregation() {
        String queryString = "MATCH (a)-[e:CycleEdge]->(b) return b.strVP, avg(e.intEP)";
        // There are 5 star edges a-[e]->b.
        // (1, 2) has groupByKey: strVPValueE and value 3
        // (2, 3) has groupByKey: strVPValueO and value 5
        // (3, 4) has groupByKey: strVPValueE and value 7
        // (4, 5) has groupByKey: strVPValueO and value 9
        // (5, 1) has groupByKey: strVPValueO and value 6
        double strOAvg = (double) (5 + 9 + 6)/3;
        double strEAvg = (double) (3 + 7)/2;
        runTest(queryString, "strVPValueO " + strOAvg , "strVPValueE " + strEAvg);
    }

    @Test
    public void testAggregateSingleGroupByKeyMultipleAggregation() {
        String queryString = "MATCH (a)-[e:CycleEdge]->(b) return b.strVP, avg(e.intEP), count(*)";
        // see testAggregateSingleGroupByKeySingleAggregation
        double strVPValueOAvg = (double) (5 + 9 + 6)/3;
        double strVPValueEAvg = (double) (3 + 7)/2;
        runTest(queryString, "strVPValueO " + strVPValueOAvg + " 3" , "strVPValueE " + strVPValueEAvg + " 2");
    }

    @Test
    public void testAggregateMultipleGroupByKeySingleAggregation() {
        String queryString = "MATCH (a)-[e:CycleEdge]->(b) return a.strVP, e.strEP, sum(b.doubleVP)";
        // There are 5 cycle edges a-[e]->b.
        // (1, 2) has groupByKey: strVPValueO-strEPValueO and value 2.0
        // (2, 3) has groupByKey: strVPValueE-strEPValueO and value 3.0
        // (3, 4) has groupByKey: strVPValueO-strEPValueO and value 4.0
        // (4, 5) has groupByKey: strVPValueE-strEPValueO and value 5.0
        // (5, 1) has groupByKey: strVPValueO-strEPValueE and value 1.0
        runTest(queryString, "strVPValueO-strEPValueO 6.0", "strVPValueE-strEPValueO 8.0",
            "strVPValueO-strEPValueE 1.0");
    }

    @Test
    public void testAggregateMultipleGroupByKeyMultipleAggregations() {
        String queryString = "MATCH (a)-[e:CycleEdge]->(b) return a.strVP, e.strEP, sum(b.doubleVP), count(*)";
        // see testAggregateMultipleGroupByKeySingleAggregation for the matching edges.
        runTest(queryString, "strVPValueO-strEPValueO 6.0 2", "strVPValueE-strEPValueO 8.0 2",
            "strVPValueO-strEPValueE 1.0 1");
    }

    @Test
    public void testCountStar() {
        String queryString = "MATCH (a)->(b) return count(*)";
        runTest(queryString, " 10");
    }

    @Test
    public void testAverageInt() {
        String queryString = "MATCH (a)-[e]->(b) return e.strEP, avg(b.intVP);";
        // There are 7 strEPValueO edges (a, b) with b values: (0,1): 1, (0,3): 3, (0, 5): 5, (1,2): 2,
        // (2, 3): 3, (3,4): 4, (4, 5): 5
        // There are 3 strEPValueE edges (a, b) with b values: (0,2): 2, (0,4): 4, (5,1): 1
        double strEPValueOAvg = (1 + 3 + 5 + 2 + 3 + 4 + 5)/7.0;
        double strEPValueEAvg = (2 + 4 + 1)/3.0;
        runTest(queryString, "strEPValueO " + strEPValueOAvg, "strEPValueE " + strEPValueEAvg);
    }

    @Test
    public void testAverageDouble() {
        String queryString = "MATCH (a)-[e]->(b) return e.strEP, avg(a.doubleVP);";
        // There are 7 strEPValueO edges (a, b) with a values: (0,1): 0.0, (0,3): 0.0, (0, 5): 0.0, (1,2): 1.0,
        // (2, 3): 2.0, (3,4): 3.0, (4, 5): 4.0
        // There are 3 strEPValueE edges (a, b) with a values: (0,2): 0.0, (0,4): 0.0, (5,1): 5.0
        double strEPValueOAvg = (0.0 + 0.0 + 0.0 + 1.0 + 2.0 + 3.0 + 4.0)/7;
        double strEPValueEAvg = (0.0 + 0.0 + 5.0)/3;
        runTest(queryString, "strEPValueO " + strEPValueOAvg, "strEPValueE " + strEPValueEAvg);
    }

    @Test
    public void testSumInt() {
        String queryString = "MATCH (a)-[e]->(b) return e.strEP, sum(b.intVP);";
        // see testAverageInt
        long strEPValueOSum = 1 + 3 + 5 + 2 + 3 + 4 + 5;
        long strEPValueESum = 2 + 4 + 1;
        runTest(queryString, "strEPValueO " + strEPValueOSum, "strEPValueE " + strEPValueESum);
    }

    @Test
    public void testSumDouble() {
        String queryString = "MATCH (a)-[e]->(b) return e.strEP, sum(a.doubleVP);";
        // see testAverageDouble
        double strEPValueOSum = 0.0 + 0.0 + 0.0 + 1.0 + 2.0 + 3.0 + 4.0;
        double strEPValueESum = 0.0 + 0.0 + 5.0;
        runTest(queryString, "strEPValueO " + strEPValueOSum, "strEPValueE " + strEPValueESum);
    }

    @Test
    public void testMaxInt() {
        String queryString = "MATCH (a)-[e]->(b) return a.strVP, max(e.intEP);";
        // There are 3 strVPValueO edges (a, b) with a values: (1,2): 3, (3,4): 7, (5,1): 6
        // There are 7 strVPValueE edges (a, b) with e values: (0,1): 1, (0,2): 2, (0,3): 3, (0, 4): 4,
        // (0,5): 5, (2, 3): 5, (4,5): 9
        long strVPValueOMax = 7;
        long strVPValueEMax = 9;
        runTest(queryString, "strVPValueO " + strVPValueOMax, "strVPValueE " + strVPValueEMax);
    }

    @Test
    public void testMaxDouble() {
        String queryString = "MATCH (a)-[e]->(b) return e.strEP, max(a.doubleVP);";
        // see testAverageDouble
        double strEPValueOMax = 4.0;
        double strEPValueEMax = 5.0;
        runTest(queryString, "strEPValueO " + strEPValueOMax, "strEPValueE " + strEPValueEMax);
    }

    @Test
    public void testMinInt() {
        String queryString = "MATCH (a)-[e]->(b) return a.strVP, min(e.intEP);";
        // see testMaxInt
        long strVPValueOMin = 3;
        long strVPValueEMin = 1;
        runTest(queryString, "strVPValueO " + strVPValueOMin, "strVPValueE " + strVPValueEMin);
    }

    @Test
    public void testMinDouble() {
        String queryString = "MATCH (a)-[e]->(b) return e.strEP, min(a.doubleVP);";
        // see testAverageDouble
        double strEPValueOMax = 0.0;
        double strEPValueEMax = 0.0;
        runTest(queryString, "strEPValueO " + strEPValueOMax, "strEPValueE " + strEPValueEMax);
    }

    private void runTest(String queryString, String...expectedResultsList) {
        StructuredQuery query = new StructuredQueryParser().parse(queryString);
        InMemoryOutputSink outputSink = new InMemoryOutputSink();
        ((OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(query, outputSink).plan()).
            execute(Graph.getInstance());
        List<String> expectedResults = new ArrayList<>(Arrays.asList(expectedResultsList));
        List<String> actualResults = outputSink.getResults();
        Collections.sort(expectedResults);
        Collections.sort(actualResults);
        assertArrayEquals(expectedResults.toArray(), actualResults.toArray());
    }
}

package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.query.operator.FileOutputSink;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.ContinuousMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringJoiner;

/**
 * Tests {@link GenericJoinExecutor}.
 */
public class ContinuousMatchQueryExecutorTest {

    // Special JUnit defined temporary folder used to test I/O operations on files. Requires
    // {@code public} visibility.
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        GraphDBState.reset();
    }

    /**
     * Removes all registered Continuous Match queries after the
     */
    @AfterClass
    public static void resetContinuousMatchQueries() {
        ContinuousMatchQueryExecutor.getInstance().reset();
    }

    /**
     * Tests the execution of a triangle CONTINUOUS MATCH query.
     */
    @Test
    public void testProcessTriangles() throws Exception {
        // Register a triangle CONTINUOUS MATCH query.
        String continuousTriangleQuery = "CONTINUOUS MATCH (a)->(b),(b)->(c),(c)->(a)" +
            " FILE 'results';";
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(
            continuousTriangleQuery);
        String fileName = "continuous_match_query_" + structuredQuery.
            getFilePath();
        File location = temporaryFolder.newFile(fileName);
        AbstractDBOperator outputSink = new FileOutputSink(location);
        ContinuousMatchQueryExecutor.getInstance().addContinuousMatchQueryPlan(
            (ContinuousMatchQueryPlan) new ContinuousMatchQueryPlanner(structuredQuery,
                outputSink).plan());

        // Initialize a graph.
        Graph graph = Graph.getInstance();
        TestUtils.createEdgesPermanently(graph, "CREATE (0:Person)-[:FOLLOWS]->" +
            "(1:Person),(1:Person)-[:FOLLOWS]->(2:Person), (1:Person)-[:FOLLOWS]->(3:Person)," +
            "(2:Person)-[:FOLLOWS]->(3:Person), (3:Person)-[:FOLLOWS]->(4:Person)," +
            "(3:Person)-[:FOLLOWS]->(0:Person), (4:Person)-[:FOLLOWS]->(1:Person);");

        // Create a diff graph by temporarily adding and deleting edges.
        TestUtils.createEdgesTemporarily(graph, "CREATE (2:Person)-[:FOLLOWS]->(0:Person)");
        TestUtils.deleteEdgesTemporarily(graph, "DELETE (3)->(4)");
        TestUtils.deleteEdgesTemporarily(graph, "DELETE (1)->(2)");

        // Execute the registered CONTINUOUS MATCH query.
        ContinuousMatchQueryExecutor.getInstance().execute(graph);

        Object[][] expectedMotifs = {{2, 0, 1}, {3, 4, 1}, {3, 4, 1}, {3, 4, 1}, {1, 2, 0}};
        MatchQueryResultType[] expectedMatchQueryResultTypes = {MatchQueryResultType.EMERGED,
            MatchQueryResultType.DELETED, MatchQueryResultType.DELETED,
            MatchQueryResultType.DELETED, MatchQueryResultType.DELETED};

        // Test the output of the registered CONTINUOUS MATCH query.
        BufferedReader br = new BufferedReader(new FileReader(location));
        StringJoiner actualOutput = new StringJoiner(System.lineSeparator());
        String line;
        while ((line = br.readLine()) != null) {
            actualOutput.add(line);
        }
        Assert.assertEquals(actualOutput.toString(), TestUtils.getInMemoryOutputSinkForMotifs(
            expectedMotifs, expectedMatchQueryResultTypes).toString());
    }
}

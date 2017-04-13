package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.operator.udf.UDFAction;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.ContinuousMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.udfexamples.WriteToFileUDFAction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringJoiner;

/**
 * Tests the {@link UDFSink} class.
 */
public class UDFSinkTest {

    @After
    public void resetContinuousTests() {
        ContinuousMatchQueryExecutor.getInstance().reset();
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testUDFSink() throws IOException {
        ContinuousMatchQueryExecutor.getInstance().reset();
        GraphDBState.reset();

        String continuousMatchQuery = "CONTINUOUSLY MATCH (a)->(b), (b)->(c) ACTION UDF ca" +
            ".waterloo.FileSinkAction IN '/tmp/FileSinkAction.jar'";
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(continuousMatchQuery);

        File location = temporaryFolder.newFile("results.txt");
        UDFAction udf = new WriteToFileUDFAction();
        ((WriteToFileUDFAction) udf).setFileToWriteTo(location.getAbsolutePath());

        ContinuousMatchQueryExecutor.getInstance().addContinuousMatchQueryPlan(
            (ContinuousMatchQueryPlan) new ContinuousMatchQueryPlanner(structuredQuery, udf).
                plan());

        // Create through the TestUtils does not load any of the properties.
        TestUtils.createEdgesTemporarily(Graph.getInstance(), "CREATE (0:t{age: 4})-[:te]->" +
            "(1:t),(1:t)-[:t]->(2:t);");

        ContinuousMatchQueryExecutor.getInstance().execute(Graph.getInstance());

        // Test the output of the registered CONTINUOUS MATCH query.
        BufferedReader br = new BufferedReader(new FileReader(location));
        StringJoiner actualOutput = new StringJoiner(System.lineSeparator());
        String line;
        while ((line = br.readLine()) != null) {
            actualOutput.add(line);
        }
        Assert.assertEquals("vertex a : 0-t\n" + "vertex b : 1-t\n" +
                "vertex c : 2-t\n" + "Edge properties (0)->(1):\n" + "Edge properties (1)->(2):",
            actualOutput.toString());
    }
}

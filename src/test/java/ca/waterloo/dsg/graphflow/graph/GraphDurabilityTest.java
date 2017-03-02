package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.query.QueryProcessor;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.CreateQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.CreateQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

/**
 * Tests the durability feature of {@link Graph} and associated classes.
 */
public class GraphDurabilityTest {

    private static String FILENAME = "GraphDurabilityTest.out";
    private static Graph graph = Graph.getInstance();
    private static VertexPropertyStore vertexPropertyStore = VertexPropertyStore.getInstance();
    private static EdgeStore edgeStore = EdgeStore.getInstance();
    private static TypeAndPropertyKeyStore keyStore = TypeAndPropertyKeyStore.getInstance();
    // Special JUnit defined temporary folder used to test I/O operations on files. Requires
    // {@code public} visibility.
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File location;

    @Before
    public void setUp() throws IOException {
        GraphDBState.reset();
        location = temporaryFolder.newFile(FILENAME);
    }

    /**
     * Tests that saving a the graph state for a simple graph to disk and loading it back results
     * in the same graph state.
     */
    @Test
    public void testSavingAndLoadingSimpleGraph() throws Exception {
        String query = "CREATE " +
            "(1:Person { name: String = Olivier })" +
            "-[:FOLLOWS {date:int=3}]->" +
            "(2:Person { name: String = Mohannad })," +
            "(1:Person { name: String = Olivier })" +
            "-[:LIKES{date:int=2}]->" +
            "(2:Person { name: String = Mohannad })," +
            "(2:Person { name: String = Mohannad })" +
            "-[:FOLLOWS]->" +
            "(4:Person { name: String = Sid })," +
            "(1:Person {name: String = Olivier})" +
            "-[:FOLLOWS]->" +
            "(4:Person { name: String = Sid })," +
            "(4:Person {name: String = Sid})" +
            "-[:LIKES]->" +
            "(1:Person { name: String = Amine });";
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(query);
        ((CreateQueryPlan) new CreateQueryPlanner(structuredQuery).plan()).execute(graph,
            new InMemoryOutputSink());

        QueryProcessor queryProcessor = new QueryProcessor();
        queryProcessor.process("SAVE GRAPH '" + location.getAbsolutePath() + "'");

        Graph oldGraph = Graph.getInstance();
        EdgeStore oldEdgeStore = EdgeStore.getInstance();
        VertexPropertyStore oldVertexPropertyStore = VertexPropertyStore.getInstance();
        TypeAndPropertyKeyStore oldTypeAndPropertyKeyStore = TypeAndPropertyKeyStore.getInstance();
        GraphDBState.reset();

        queryProcessor.process("LOAD GRAPH '" + location.getAbsolutePath() + "'");

        Assert.assertTrue(Graph.isSamePermanentGraphAs(oldGraph, Graph.getInstance()));
        Assert.assertTrue(EdgeStore.isSameAs(oldEdgeStore, EdgeStore.getInstance()));
        Assert.assertTrue(VertexPropertyStore.isSameAs(oldVertexPropertyStore,
            VertexPropertyStore.getInstance()));
        Assert.assertTrue(TypeAndPropertyKeyStore.isSameAs(oldTypeAndPropertyKeyStore,
            TypeAndPropertyKeyStore.getInstance()));
    }
}

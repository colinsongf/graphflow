package ca.waterloo.dsg.graphflow.graph;

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
 * Tests the durability feature of Graphflow.
 */
public class GraphflowDurabilityTest {

    // Special JUnit defined temporary folder used to test I/O operations on files. Requires
    // {@code public} visibility.
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File saveDirectory;

    @Before
    public void setUp() throws IOException {
        GraphDBState.reset();
        saveDirectory = temporaryFolder.newFolder();
    }

    /**
     * Tests that saving the graph state for a simple graph to disk and loading it back results
     * in the same graph state.
     */
    @Test
    public void testSavingAndLoadingSimpleGraph() throws Exception {
        String query = "CREATE " +
            "(1:Person{ name: 'Olivier' })-[:FOLLOWS{ date:3 }]->(2:Person{ name: 'Mohannad'})," +
            "(1:Person{ name: 'Olivier' })-[:LIKES{ date:2 }]->(2:Person{ name: 'Mohannad' })," +
            "(2:Person{ name: 'Mohannad' })-[:FOLLOWS]->(4:Person { name: 'Sid' })," +
            "(1:Person{ name: 'Olivier' })-[:FOLLOWS]->(4:Person{ name: 'Sid' })," +
            "(4:Person{ name: 'Sid' })-[:LIKES]->(1:Person{ name: 'Amine' });";

        StructuredQuery structuredQuery = new StructuredQueryParser().parse(query);
        ((CreateQueryPlan) new CreateQueryPlanner(structuredQuery).plan()).execute(
            Graph.getInstance(), new InMemoryOutputSink());

        GraphDBState.serialize(saveDirectory.getAbsolutePath());

        Graph oldGraph = Graph.getInstance();
        EdgeStore oldEdgeStore = EdgeStore.getInstance();
        VertexPropertyStore oldVertexPropertyStore = VertexPropertyStore.getInstance();
        TypeAndPropertyKeyStore oldTypeAndPropertyKeyStore = TypeAndPropertyKeyStore.getInstance();

        GraphDBState.deserialize(saveDirectory.getAbsolutePath());

        Assert.assertTrue(Graph.isSamePermanentGraphAs(oldGraph, Graph.getInstance()));
        Assert.assertTrue(EdgeStore.isSameAs(oldEdgeStore, EdgeStore.getInstance()));
        Assert.assertTrue(VertexPropertyStore.isSameAs(oldVertexPropertyStore,
            VertexPropertyStore.getInstance()));
        Assert.assertTrue(TypeAndPropertyKeyStore.isSameAs(oldTypeAndPropertyKeyStore,
            TypeAndPropertyKeyStore.getInstance()));
    }
}

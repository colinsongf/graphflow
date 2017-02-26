package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.QueryProcessor;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.CreateQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.CreateQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.DataType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Tests the durability feature of {@link Graph} and associated classes.
 */
public class GraphDurabilityTest {

    private static String FILENAME = "GraphDurabilityTest.out";
    private static Graph graph = Graph.getInstance();
    private static VertexPropertyStore vertexPropertyStore = VertexPropertyStore.getInstance();
    private static EdgeStore edgeStore = EdgeStore.getInstance();
    private static TypeAndPropertyKeyStore keyStore = TypeAndPropertyKeyStore.getInstance();
    // Special JUnit defined temporary folder used to test IO operations on files. Requires
    // {@code public} visibility.
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private File location;

    @Before
    public void setUp() throws IOException {
        Graph.getInstance().reset();
        location = temporaryFolder.newFile(FILENAME);
    }

    /**
     * Tests that saving a simple graph to disk and loading it back results in the same graph.
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

        String originalGraph = getGraphDataAndIdsAsString();
        QueryProcessor queryProcessor = new QueryProcessor();
        queryProcessor.process("SAVE GRAPH '" + location.getAbsolutePath() + "'");

        graph.reset();

        queryProcessor.process("LOAD GRAPH '" + location.getAbsolutePath() + "'");
        String loadedGraph = getGraphDataAndIdsAsString();

        Assert.assertEquals(originalGraph, loadedGraph);
    }

    private String getGraphDataAndIdsAsString() {

        StringJoiner sj = new StringJoiner(",");
        for (int fromVertexId = 0; fromVertexId < graph.getVertexCount(); fromVertexId++) {
            SortedAdjacencyList sortedAdjacencyList = graph.getSortedAdjacencyList(fromVertexId,
                Direction.FORWARD, GraphVersion.PERMANENT);
            short fromVertexTypeId = graph.getVertexTypes().get(fromVertexId);
            String fromVertexType = keyStore.getTypeStringFromShort(fromVertexTypeId);
            String fromVertexProperties = getPropertiesAsString(vertexPropertyStore.getProperties(
                fromVertexId));
            for (int j = 0; j < sortedAdjacencyList.getSize(); j++) {
                int toVertexId = sortedAdjacencyList.neighbourIds[j];
                short toVertexTypeId = graph.getVertexTypes().get(toVertexId);
                String toVertexType = keyStore.getTypeStringFromShort(toVertexTypeId);
                String toVertexProperties = getPropertiesAsString(vertexPropertyStore.getProperties(
                    toVertexId));
                long edgeId = sortedAdjacencyList.edgeIds[j];
                short edgeTypeId = sortedAdjacencyList.edgeTypes[j];
                String edgeProperties = getPropertiesAsString(edgeStore.getProperties(
                    toVertexId));
                String edgeType = keyStore.getTypeStringFromShort(edgeTypeId);
                sj.add(String.format("(%s:%s(%s)%s)-[:%s(%s)%s](%s)->(%s:%s(%s)%s)", fromVertexId,
                    fromVertexType, fromVertexTypeId, fromVertexProperties, edgeType, edgeTypeId,
                    edgeProperties, edgeId, toVertexId, toVertexType, toVertexTypeId,
                    toVertexProperties));
            }
        }
        return sj.toString();
    }

    private String getPropertiesAsString(Map<Short, Object> properties) {
        StringJoiner output = new StringJoiner(",");
        for (Short keyId : properties.keySet()) {
            DataType dataType = keyStore.getPropertyDataType(keyId);
            String key = keyStore.getPropertyStringFromShort(keyId);
            String value = DataType.mapObjectOfDataTypeToString(dataType, properties.get(keyId));
            output.add(String.format("%s(%s):%s(%s)", key, keyId, value, dataType));
        }
        return "{" + output.toString() + "}";
    }
}

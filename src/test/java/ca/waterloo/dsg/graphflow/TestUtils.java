package ca.waterloo.dsg.graphflow;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinExecutor;
import ca.waterloo.dsg.graphflow.query.executors.MatchQueryResultType;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;

/**
 * Provides utility functions for tests.
 */
public class TestUtils {

    /**
     * Creates and returns a graph initialized with the given {@code edges}, {@code edgeTypes}
     * and {@code vertexTypes}.
     *
     * @param edges The edges {e=(u,v)} of the graph.
     * @param edgeTypes The type of each edge e.
     * @param vertexTypes The types {@code (t1, t2)} where t1 is the type of source vertex u and
     * t2 is the type of destination vertex v.
     * @return Graph The initialized graph.
     */
    public static Graph initializeGraphPermanently(int[][] edges, short[] edgeTypes,
        short[][] vertexTypes) {
        Graph graph = initializeGraphTemporarily(edges, edgeTypes, vertexTypes);
        graph.finalizeChanges();
        return graph;
    }

    /**
     * Creates and returns a graph with with the given {@code edges}, {@code edgeTypes}
     * and {@code vertexTypes} added temporarily.
     *
     * @param edges The edges {e=(u,v)} of the graph.
     * @param edgeTypes The type of each edge e.
     * @param vertexTypes The types {@code (t1, t2)} where t1 is the type of source vertex u and
     * t2 is the type of destination vertex v.
     * @return Graph The graph initialized with temporary edges.
     */
    public static Graph initializeGraphTemporarily(int[][] edges, short[] edgeTypes,
        short[][] vertexTypes) {
        Graph graph = new Graph();
        for (int i = 0; i < edges.length; i++) {
            graph.addEdgeTemporarily(edges[i][0], edges[i][1], vertexTypes[i][0],
                vertexTypes[i][1], edgeTypes[i]);
        }
        return graph;
    }

    /**
     * Adds a set of edges to the given {@code graph} by executing the given {@code createQuery}.
     *
     * @param graph The {@link Graph} instance to which the edges should be added.
     * @param createQuery The {@code String} create query to be executed.
     */
    public static void addEdgesToGraphUsingCreateQuery(Graph graph, String createQuery) {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(createQuery);
        for (QueryEdge queryEdge : structuredQuery.getQueryEdges()) {
            int fromVertex = Integer.parseInt(queryEdge.getFromQueryVariable().getVariableId());
            int toVertex = Integer.parseInt(queryEdge.getToQueryVariable().getVariableId());
            // Insert the types into the {@code TypeStore} if they do not already exist, and
            // get their {@code short} IDs. An exception in the above {@code parseInt()} calls
            // will prevent the insertion of any new type to the {@code TypeStore}.
            short fromVertexTypeId = TypeStore.getInstance().getShortIdOrAddIfDoesNotExist(
                queryEdge.getFromQueryVariable().getVariableType());
            short toVertexTypeId = TypeStore.getInstance().getShortIdOrAddIfDoesNotExist(
                queryEdge.getToQueryVariable().getVariableType());
            short edgeTypeId = TypeStore.getInstance().getShortIdOrAddIfDoesNotExist(
                queryEdge.getEdgeType());
            // Add the new edge to the graph.
            graph.addEdgeTemporarily(fromVertex, toVertex, fromVertexTypeId, toVertexTypeId,
                edgeTypeId);
        }
        graph.finalizeChanges();
    }

    /**
     * Returns an {@link InMemoryOutputSink} simulating output from {@link GenericJoinExecutor}.
     *
     * @param motifs The array of motifs which should be present in the {@link InMemoryOutputSink}
     * @return An {@link InMemoryOutputSink} containing {@code motifs} and a default
     * {@link MatchQueryResultType#MATCHED}.
     */
    public static InMemoryOutputSink getInMemoryOutputSinkForMotifs(int[][] motifs) {
        InMemoryOutputSink inMemoryOutputSink = new InMemoryOutputSink();
        for (int[] motif : motifs) {
            inMemoryOutputSink.append(GenericJoinExecutor.getStringOutput(motif,
                MatchQueryResultType.MATCHED));
        }
        return inMemoryOutputSink;
    }

    /**
     * Returns an {@link InMemoryOutputSink} simulating output from {@link GenericJoinExecutor}.
     *
     * @param motifs The array of motifs which should be present in the {@link InMemoryOutputSink}
     * @param matchQueryResultTypes The array of {@link MatchQueryResultType} representing the
     * output type of the {@code motifs}.
     * @return An {@link InMemoryOutputSink} containing {@code motifs} and a default
     * {@link MatchQueryResultType#MATCHED}.
     */
    public static InMemoryOutputSink getInMemoryOutputSinkForMotifs(int[][] motifs,
        MatchQueryResultType[] matchQueryResultTypes) {
        InMemoryOutputSink inMemoryOutputSink = new InMemoryOutputSink();
        for (int i = 0; i < motifs.length; i++) {
            inMemoryOutputSink.append(GenericJoinExecutor.getStringOutput(motifs[i],
                matchQueryResultTypes[i]));
        }
        return inMemoryOutputSink;
    }
}

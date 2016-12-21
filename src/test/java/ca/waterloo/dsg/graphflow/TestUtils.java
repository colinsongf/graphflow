package ca.waterloo.dsg.graphflow;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;

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
    public static void createEdgesPermanently(Graph graph, String createQuery) {
        createEdgesTemporarily(graph, createQuery);
        graph.finalizeChanges();
    }

    /**
     * Adds a set of edges to the given {@code graph} temporarily by executing the given
     * {@code createQuery}.
     *
     * @param graph The {@link Graph} instance to which the edges should be added.
     * @param createQuery The {@code String} create query to be executed.
     */
    public static void createEdgesTemporarily(Graph graph, String createQuery) {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(createQuery);
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            int fromVertex = Integer.parseInt(queryRelation.getFromQueryVariable().getVariableId());
            int toVertex = Integer.parseInt(queryRelation.getToQueryVariable().getVariableId());
            // Insert the types into the {@code TypeStore} if they do not already exist, and
            // get their {@code short} IDs. An exception in the above {@code parseInt()} calls
            // will prevent the insertion of any new type to the {@code TypeStore}.
            short fromVertexTypeId = TypeStore.getInstance().getShortIdOrAddIfDoesNotExist(
                queryRelation.getFromQueryVariable().getVariableType());
            short toVertexTypeId = TypeStore.getInstance().getShortIdOrAddIfDoesNotExist(
                queryRelation.getToQueryVariable().getVariableType());
            short edgeTypeId = TypeStore.getInstance().getShortIdOrAddIfDoesNotExist(
                queryRelation.getRelationType());
            // Add the new edge to the graph.
            graph.addEdgeTemporarily(fromVertex, toVertex, fromVertexTypeId, toVertexTypeId,
                edgeTypeId);
        }
    }

    /**
     * Deletes a set of edges from the given {@code graph} permanently by executing the given
     * {@code deleteQuery}.
     *
     * @param graph The {@link Graph} instance from which the edges should be deleted.
     * @param deleteQuery The {@code String} delete query to be executed.
     */
    public static void deleteEdgesPermanently(Graph graph, String deleteQuery) {
        deleteEdgesTemporarily(graph, deleteQuery);
        graph.finalizeChanges();
    }

    /**
     * Deletes a set of edges from the given {@code graph} temporarily by executing the given
     * {@code deleteQuery}.
     *
     * @param graph The {@link Graph} instance from which the edges should be deleted.
     * @param deleteQuery The {@code String} delete query to be executed.
     */
    public static void deleteEdgesTemporarily(Graph graph, String deleteQuery) {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(deleteQuery);
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            graph.deleteEdgeTemporarily(
                Integer.parseInt(queryRelation.getFromQueryVariable().getVariableId()),
                Integer.parseInt(queryRelation.getToQueryVariable().getVariableId()),
                TypeStore.getInstance().getShortIdOrAnyTypeIfNull(
                    queryRelation.getRelationType()));
        }
    }
}

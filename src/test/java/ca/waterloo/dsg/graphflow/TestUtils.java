package ca.waterloo.dsg.graphflow;


import ca.waterloo.dsg.graphflow.graph.Graph;
import org.junit.Assert;
import org.junit.Test;

/**
 * Provides utility functions for graph tests.
 */
public class TestUtils {

    /**
     * Creates and returns a graph initialized with the given {@code edges}, {@code edgeTypes}
     * and {@code vertexTypes}.
     * @param edges The edges {e=(u,v)} of the graph.
     * @param edgeTypes The type of each edge e.
     * @param vertexTypes The types {@code (t1, t2)} where t1 is the type of source vertex u and
     * t2 is the type of destination vertex v.
     * @return Graph The initialized graph.
     */
    public static Graph initializeGraph(int[][] edges, short[] edgeTypes, short[][] vertexTypes) {
        Graph graph = new Graph();
        for (int i = 0; i < edges.length; i++) {
            graph.addEdgeTemporarily(edges[i][0], edges[i][1], vertexTypes[i][0],
                vertexTypes[i][1], edgeTypes[i]);
        }
        graph.finalizeChanges();
        return graph;
    }

    /**
     * Creates and returns a graph with with the given {@code edges}, {@code edgeTypes}
     * and {@code vertexTypes} added temporarily.
     * @param edges The edges {e=(u,v)} of the graph.
     * @param edgeTypes The type of each edge e.
     * @param vertexTypes The types {@code (t1, t2)} where t1 is the type of source vertex u and
     * t2 is the type of destination vertex v.
     * @return Graph The graph initialized with temporary edges.
     */
    public static Graph initializeGraphWithoutFinalizing(int[][] edges, short[] edgeTypes,
        short[][] vertexTypes) {
        Graph graph = new Graph();
        for (int i = 0; i < edges.length; i++) {
            graph.addEdgeTemporarily(edges[i][0], edges[i][1], vertexTypes[i][0],
                vertexTypes[i][1], edgeTypes[i]);
        }
        graph.finalizeChanges();
        return graph;
    }
}

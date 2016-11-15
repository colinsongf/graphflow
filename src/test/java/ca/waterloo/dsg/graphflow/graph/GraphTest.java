package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Tests {@code Graph} class.
 */
public class GraphTest {

    @Test
    public void testInsertions() throws Exception {
        Graph graph = new Graph();

        // Create the graph.
        int[][] edges = {{0, 3}, {0, 1}, {1, 3}, {1, 2}, {4, 0}, {4, 1}};
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Test the vertex count.
        Assert.assertEquals(5, graph.getVertexCount());
        // Test the outgoing adjacency lists.
        int[][] expectedOutgoingAdjLists = {{1, 3}, {2, 3}, {}, {}, {0, 1}};
        for (int i = 0; i < expectedOutgoingAdjLists.length; i++) {
            Assert.assertTrue("Testing FORWARD vertex id: " + i,
                graph.getAdjacencyList(i, EdgeDirection.FORWARD, GraphVersion.CURRENT)
                    .isSameAs(expectedOutgoingAdjLists[i]));
        }
        // Test the incoming adjacency lists.
        int[][] expectedIncomingAdjLists = {{4}, {0, 4}, {1}, {0, 1}, {}};
        for (int i = 0; i < expectedIncomingAdjLists.length; i++) {
            Assert.assertTrue("Testing REVERSE vertex id: " + i,
                graph.getAdjacencyList(i, EdgeDirection.REVERSE, GraphVersion.CURRENT)
                    .isSameAs(expectedIncomingAdjLists[i]));
        }
    }

    @Test
    public void testRepeatInsertions() throws Exception {
        Graph graph = new Graph();

        // Create the graph. The edge insertions are in random order.
        int[][] edges =
            {{0, 6}, {0, 3}, {0, 5}, {0, 1}, {0, 4}, {0, 2}, {5, 6}, {1, 6}, {4, 6}, {3, 6},
                {2, 6}};
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        graph.finalizeChanges();
        assertRepeatInsertions(graph);

        // Insert the same edges again to the graph in a different random order.
        int[][] edges2 =
            {{0, 4}, {0, 1}, {0, 5}, {0, 3}, {0, 6}, {0, 2}, {3, 6}, {2, 6}, {4, 6}, {5, 6},
                {1, 6}};
        for (int[] edge : edges2) {
            graph.addEdge(edge[0], edge[1]);
        }
        assertRepeatInsertions(graph);
    }

    private void assertRepeatInsertions(Graph graph) throws Exception {
        // Test vertex count.
        Assert.assertEquals(7, graph.getVertexCount());
        // Test outgoing adjacency lists. The adjacency lists should be in sorted order.
        int[][] expectedOutgoingAdjLists2 = {{1, 2, 3, 4, 5, 6}, {6}, {6}, {6}, {6}, {6}, {}};
        for (int i = 0; i < expectedOutgoingAdjLists2.length; i++) {
            Assert.assertTrue("Testing FORWARD vertex id: " + i,
                graph.getAdjacencyList(i, EdgeDirection.FORWARD, GraphVersion.CURRENT)
                    .isSameAs(expectedOutgoingAdjLists2[i]));
        }
        // Test incoming adjacency lists. The adjacency lists should be in sorted order.
        int[][] expectedIncomingAdjLists2 = {{}, {0}, {0}, {0}, {0}, {0}, {0, 1, 2, 3, 4, 5}};
        for (int i = 0; i < expectedIncomingAdjLists2.length; i++) {
            Assert.assertTrue("Testing REVERSE vertex id: " + i,
                graph.getAdjacencyList(i, EdgeDirection.REVERSE, GraphVersion.CURRENT)
                    .isSameAs(expectedIncomingAdjLists2[i]));
        }
    }

    @Test
    public void testInsertionsUsingJson() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("graph.json").getPath());
        Graph graph = GraphBuilder.createInstance(file);

        // Test vertex count.
        Assert.assertEquals(6, graph.getVertexCount());
        // Test outgoing adjacency lists.
        int[][] expectedOutgoingAdjLists = {{1}, {2}, {0, 3, 4}, {0}, {5}, {}};
        for (int i = 0; i < expectedOutgoingAdjLists.length; i++) {
            Assert.assertTrue("Testing FORWARD vertex id: " + i,
                graph.getAdjacencyList(i, EdgeDirection.FORWARD, GraphVersion.CURRENT)
                    .isSameAs(expectedOutgoingAdjLists[i]));
        }
        // Test incoming adjacency lists.
        int[][] expectedIncomingAdjLists = {{2, 3}, {0}, {1}, {2}, {2}, {4}};
        for (int i = 0; i < expectedIncomingAdjLists.length; i++) {
            Assert.assertTrue("Testing REVERSE vertex id: " + i,
                graph.getAdjacencyList(i, EdgeDirection.REVERSE, GraphVersion.CURRENT)
                    .isSameAs(expectedIncomingAdjLists[i]));
        }
    }

    @Test
    public void testDeletions() throws Exception {
        Graph graph = new Graph();

        // Create the graph.
        int[][] edges = {{0, 3}, {0, 1}, {1, 3}, {1, 2}, {4, 0}, {4, 1}};
        for (int[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Delete a few edges.
        int[][] deleteEdges = {{0, 1}, {4, 1}};
        for (int[] edge : deleteEdges) {
            graph.deleteEdge(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Test the vertex count.
        Assert.assertEquals(5, graph.getVertexCount());
        // Test the outgoing adjacency lists.
        int[][] expectedOutgoingAdjLists = {{3}, {2, 3}, {}, {}, {0}};
        for (int i = 0; i < expectedOutgoingAdjLists.length; i++) {
            Assert.assertTrue("Testing FORWARD vertex id: " + i,
                graph.getAdjacencyList(i, EdgeDirection.FORWARD, GraphVersion.CURRENT)
                    .isSameAs(expectedOutgoingAdjLists[i]));
        }
        // Test the incoming adjacency lists.
        int[][] expectedIncomingAdjLists = {{4}, {}, {1}, {0, 1}, {}};
        for (int i = 0; i < expectedIncomingAdjLists.length; i++) {
            Assert.assertTrue("Testing REVERSE vertex id: " + i,
                graph.getAdjacencyList(i, EdgeDirection.REVERSE, GraphVersion.CURRENT)
                    .isSameAs(expectedIncomingAdjLists[i]));
        }
    }
}

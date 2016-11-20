package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests {@code Graph} class.
 */
public class GraphTest {

    /**
     * Tests that the insertion of edges that are already present in the graph do not result in any
     * changes to the graph.
     */
    @Test
    public void testInsertions() throws Exception {
        Graph graph = new Graph();

        // Create a graph.
        int[][] edges = {{0, 6}, {0, 3}, {0, 5}, {0, 1}, {0, 4}, {0, 2}, {5, 6}, {1, 6}, {4, 6},
            {3, 6}, {2, 6}};
        for (int[] edge : edges) {
            graph.addEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();
        assertInsertions(graph);

        // Randomly shuffle the array of edges and insert them again to the graph.
        Collections.shuffle(Arrays.asList(edges));
        for (int[] edge : edges) {
            graph.addEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();
        assertInsertions(graph);
    }

    private void assertInsertions(Graph graph) throws Exception {
        // Test vertex count.
        Assert.assertEquals(7, graph.getVertexCount());
        // Test outgoing adjacency lists. The adjacency lists should be in sorted order.
        int[][] expectedOutgoingAdjLists2 = {{1, 2, 3, 4, 5, 6}, {6}, {6}, {6}, {6}, {6}, {}};
        for (int i = 0; i < expectedOutgoingAdjLists2.length; i++) {
            Assert.assertTrue("Testing FORWARD vertex id: " + i, graph.getAdjacencyList(i,
                Direction.FORWARD, GraphVersion.PERMANENT).isSameAs
                (expectedOutgoingAdjLists2[i]));
        }
        // Test incoming adjacency lists. The adjacency lists should be in sorted order.
        int[][] expectedIncomingAdjLists2 = {{}, {0}, {0}, {0}, {0}, {0}, {0, 1, 2, 3, 4, 5}};
        for (int i = 0; i < expectedIncomingAdjLists2.length; i++) {
            Assert.assertTrue("Testing BACKWARD vertex id: " + i, graph.getAdjacencyList(i,
                Direction.BACKWARD, GraphVersion.PERMANENT).isSameAs
                (expectedIncomingAdjLists2[i]));
        }
    }

    /**
     * Tests that the deletion of edges that exist result in actual deletions and the deletion of
     * edges that don't exist do not result in any changes in the graph.
     */
    @Test
    public void testDeletions() throws Exception {
        Graph graph = new Graph();

        // Create a graph.
        int[][] edges = {{0, 3}, {0, 1}, {1, 3}, {1, 2}, {4, 0}, {4, 1}};
        for (int[] edge : edges) {
            graph.addEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Delete a list of edges.
        int[][] deleteEdges = {{0, 1}, {4, 1}, /* Edges exist in the graph.*/
            {120, 34}, {1, 42} /* Edges do not exist in the graph.*/};
        for (int[] edge : deleteEdges) {
            graph.deleteEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        // Test the vertex count.
        Assert.assertEquals(5, graph.getVertexCount());
        // Test the outgoing adjacency lists.
        int[][] expectedOutgoingAdjLists = {{3}, {2, 3}, {}, {}, {0}};
        for (int i = 0; i < expectedOutgoingAdjLists.length; i++) {
            Assert.assertTrue("Testing FORWARD vertex id: " + i, graph.getAdjacencyList(i,
                Direction.FORWARD, GraphVersion.PERMANENT).isSameAs
                (expectedOutgoingAdjLists[i]));
        }
        // Test the incoming adjacency lists.
        int[][] expectedIncomingAdjLists = {{4}, {}, {1}, {0, 1}, {}};
        for (int i = 0; i < expectedIncomingAdjLists.length; i++) {
            Assert.assertTrue("Testing BACKWARD vertex id: " + i, graph.getAdjacencyList(i,
                Direction.BACKWARD, GraphVersion.PERMANENT).isSameAs
                (expectedIncomingAdjLists[i]));
        }
    }
}

package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Tests the {@code Graph} class.
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
        int[][] deleteExistingEdges = {{0, 1}, {4, 1}};
        for (int[] edge : deleteExistingEdges) {
            graph.deleteEdgeTemporarily(edge[0], edge[1]);
        }
        graph.finalizeChanges();

        int[][] deleteNonExistingEdges = {{120, 34}, {1, 42}};
        for (int[] edge : deleteNonExistingEdges) {
            try {
                graph.deleteEdgeTemporarily(edge[0], edge[1]);
                Assert.fail("NoSuchElementException should have been thrown.");
            } catch (NoSuchElementException e) {
                // Expected exception caught.
            }
        }

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

    private Graph getGraphWithTemporaryChanges() {
        // Create a graph with some initial edges.
        Graph graph = new Graph();
        graph.addEdgeTemporarily(0, 1);
        graph.addEdgeTemporarily(11, 9);
        graph.finalizeChanges();

        // Add and delete some edges temporarily.
        int[][] edges = {{0, 6}, {2, 6}, {5, 6}, {4, 6}, {2, 5}};
        for (int[] edge : edges) {
            graph.addEdgeTemporarily(edge[0], edge[1]);
        }
        graph.deleteEdgeTemporarily(2, 6);
        graph.deleteEdgeTemporarily(0, 1);
        return graph;
    }

    /**
     * Tests that the iterator returned by {@code getEdgesIterator()} correctly returns the
     * list of edges for the {@link GraphVersion#PERMANENT} graph.
     */
    @Test
    public void testGetEdgesIteratorForPermanentGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();

        // Test the list of edges of the permanent graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.FORWARD),
            new int[][]{{0, 1}, {11, 9}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.BACKWARD),
            new int[][]{{1, 0}, {9, 11}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the permanent graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.FORWARD),
            new int[][]{{0, 6}, {2, 5}, {4, 6}, {5, 6}, {11, 9}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.BACKWARD),
            new int[][]{{5, 2}, {6, 0}, {6, 4}, {6, 5}, {9, 11}});
    }

    /**
     * Tests that the iterator returned by {@code getEdgesIterator()} correctly returns the
     * list of edges for the {@link GraphVersion#MERGED} graph.
     */
    @Test
    public void testGetEdgesIteratorForMergedGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();

        // Test the list of edges of the merged graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.FORWARD),
            new int[][]{{0, 6}, {2, 5}, {4, 6}, {5, 6}, {11, 9}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.BACKWARD),
            new int[][]{{5, 2}, {6, 0}, {6, 4}, {6, 5}, {9, 11}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the merged graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.FORWARD),
            new int[][]{{0, 6}, {2, 5}, {4, 6}, {5, 6}, {11, 9}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.BACKWARD),
            new int[][]{{5, 2}, {6, 0}, {6, 4}, {6, 5}, {9, 11}});
    }

    /**
     * Tests that the iterator returned by {@code getEdgesIterator()} correctly returns the
     * list of edges for the {@link GraphVersion#DIFF_PLUS} graph.
     */
    @Test
    public void testGetEdgesIteratorForDiffPlusGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();

        // Test the list of edges of the merged graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_PLUS, Direction.FORWARD),
            new int[][]{{0, 6}, {2, 6}, {5, 6}, {4, 6}, {2, 5}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the merged graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_PLUS, Direction.FORWARD),
            new int[][]{});
    }

    /**
     * Tests that the iterator returned by {@code getEdgesIterator()} correctly returns the
     * list of edges for the {@link GraphVersion#DIFF_MINUS} graph.
     */
    @Test
    public void testGetEdgesIteratorForDiffMinusGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();

        // Test the list of edges of the merged graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_MINUS, Direction.FORWARD),
            new int[][]{{2, 6}, {0, 1}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the merged graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_MINUS, Direction.FORWARD),
            new int[][]{});
    }

    private void assertEdgesIterator(Iterator<int[]> iterator, int[][] expectedEdges) {
        List<int[]> edgesList;
        edgesList = new ArrayList<>();
        while (iterator.hasNext()) {
            edgesList.add(iterator.next());
        }
        Assert.assertArrayEquals(edgesList.toArray(), expectedEdges);
    }
}

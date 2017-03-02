package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Tests {@link Graph}.
 */
public class GraphTest {

    @Before
    public void setUp() {
        GraphDBState.reset();
    }

    /**
     * Tests that the insertion of edges that are already present in the graph do not result in any
     * changes to the graph.
     */
    @Test
    public void testInsertions() throws Exception {
        // Create a graph.
        int[][] edges = {{0, 6} /* edgeId: 0 */, {0, 3} /* edgeId: 1 */, {0, 5} /* edgeId: 2 */,
            {0, 1} /* edgeId: 3 */, {0, 4} /* edgeId: 4 */, {0, 2} /* edgeId: 5 */,
            {5, 6} /* edgeId: 6 */, {1, 6} /* edgeId: 7 */, {4, 6} /* edgeId: 256 + 0 */,
            {3, 6} /* edgeId: 256 + 1 */, {2, 6} /* edgeId: 256 + 2 */};
        short[] edgeTypes = {6, 3, 5, 1, 4, 2, 6, 6, 6, 6, 6};
        short[][] vertexTypes = {{0, 12}, {0, 6}, {0, 10}, {0, 2}, {0, 8}, {0, 4}, {10, 12},
            {2, 12}, {8, 12}, {6, 12}, {4, 12}};

        Graph graph = TestUtils.initializeGraphPermanently(edges, edgeTypes, vertexTypes);
        assertInsertions(graph);

        // Reinsert the previously inserted edges to test whether duplication happens.
        for (int i = 0; i < edges.length; i++) {
            graph.addEdgeTemporarily(edges[i][0], edges[i][1], vertexTypes[i][0],
                vertexTypes[i][1], null /* no fromVertex properties */, null /* no toVertex
                properties */, edgeTypes[i], null /* no edge properties */);
        }
        graph.finalizeChanges();
        assertInsertions(graph);
    }

    private void assertInsertions(Graph graph) throws Exception {
        // Test vertex count.
        Assert.assertEquals(7, graph.getVertexCount());
        // Test outgoing adjacency lists. The adjacency lists should be in sorted order.
        int[][] expectedOutgoingAdjLists = {{1, 2, 3, 4, 5, 6}, {6}, {6}, {6}, {6}, {6}, {}};
        short[][] expectedOutgoingEdgeTypes = {{1, 2, 3, 4, 5, 6}, {6}, {6}, {6}, {6}, {6}, {}};
        long[][] expectedOutgoingEdgeIds = {{3, 5, 1, 4, 2, 0}, {7}, {258}, {257}, {256}, {6}, {}};
        for (int i = 0; i < expectedOutgoingAdjLists.length; i++) {
            SortedAdjacencyList expectedSortedAdjacencyList = new SortedAdjacencyList();
            for (int j = 0; j < expectedOutgoingAdjLists[i].length; j++) {
                expectedSortedAdjacencyList.add(expectedOutgoingAdjLists[i][j],
                    expectedOutgoingEdgeTypes[i][j], expectedOutgoingEdgeIds[i][j]);
            }
            Assert.assertTrue("Testing FORWARD vertex id: " + i, SortedAdjacencyList.isSameAs(graph.
                    getSortedAdjacencyList(i, Direction.FORWARD, GraphVersion.PERMANENT),
                expectedSortedAdjacencyList));
        }
        // Test incoming adjacency lists. The adjacency lists should be in sorted order.
        int[][] expectedIncomingAdjLists = {{}, {0}, {0}, {0}, {0}, {0}, {0, 1, 2, 3, 4, 5}};
        short[][] expectedIncomingEdgeTypes = {{}, {1}, {2}, {3}, {4}, {5}, {6, 6, 6, 6, 6, 6}};
        long[][] expectedIncomingEdgeIds = {{}, {3}, {5}, {1}, {4}, {2}, {0, 7, 258, 257, 256, 6}};
        for (int i = 0; i < expectedIncomingAdjLists.length; i++) {
            SortedAdjacencyList expectedSortedAdjacencyList = new SortedAdjacencyList();
            for (int j = 0; j < expectedIncomingEdgeTypes[i].length; j++) {
                expectedSortedAdjacencyList.add(expectedIncomingAdjLists[i][j],
                    expectedIncomingEdgeTypes[i][j], expectedIncomingEdgeIds[i][j]);
            }
            Assert.assertTrue("Testing BACKWARD vertex id: " + i, SortedAdjacencyList.isSameAs(
                graph.getSortedAdjacencyList(i, Direction.BACKWARD, GraphVersion.PERMANENT),
                expectedSortedAdjacencyList));
        }
    }

    /**
     * Tests that the deletion of edges that exist result in actual deletions and the deletion of
     * edges that don't exist do not result in any changes in the graph.
     */
    @Test
    public void testDeletionOfExistingEdges() throws Exception {
        // Create a graph.
        int[][] edges = {{0, 3} /* edgeId: 0 */, {0, 1}  /* edgeId: 1 */, {1, 3}  /* edgeId: 2 */,
            {1, 2}  /* edgeId: 3 */, {4, 0}  /* edgeId: 4 */, {4, 1}  /* edgeId: 5 */};
        short[] edgeTypes = {3, 1, 3, 2, 0, 9};
        short[][] vertexTypes = {{0, 3}, {0, 1}, {1, 3}, {1, 2}, {4, 0}, {4, 1}};

        Graph graph = TestUtils.initializeGraphPermanently(edges, edgeTypes, vertexTypes);
        // Delete a list of edges.
        int[][] deleteEdges = {{0, 1}, {4, 1}}; /* Edges exist in the graph.*/
        short[] deleteEdgeTypes = {1, 9};
        for (int i = 0; i < deleteEdges.length; i++) {
            graph.deleteEdgeTemporarily(deleteEdges[i][0], deleteEdges[i][1], deleteEdgeTypes[i]);
        }
        graph.finalizeChanges();

        // Test the vertex count.
        Assert.assertEquals(5, graph.getVertexCount());
        // Test the forward adjacency lists.
        int[][] expectedOutgoingAdjLists = {{3}, {2, 3}, {}, {}, {0}};
        short[][] expectedOutgoingEdgeTypes = {{3}, {2, 3}, {}, {}, {0}};
        long[][] expectedOutgoingEdgeIds = {{0}, {3, 2}, {}, {}, {4}};
        for (int i = 0; i < expectedOutgoingAdjLists.length; i++) {
            SortedAdjacencyList expected = new SortedAdjacencyList();
            for (int j = 0; j < expectedOutgoingAdjLists[i].length; j++) {
                expected.add(expectedOutgoingAdjLists[i][j], expectedOutgoingEdgeTypes[i][j],
                    expectedOutgoingEdgeIds[i][j]);
            }
            Assert.assertTrue("Testing FORWARD vertex id: " + i, SortedAdjacencyList.isSameAs(graph.
                getSortedAdjacencyList(i, Direction.FORWARD, GraphVersion.PERMANENT), expected));
        }
        // Test the backward adjacency lists.
        int[][] expectedIncomingAdjLists = {{4}, {}, {1}, {0, 1}, {}};
        short[][] expectedIncomingEdgeTypes = {{0}, {}, {2}, {3, 3}, {}};
        long[][] expectedIncomingEdgeIds = {{4}, {}, {3}, {0, 2}, {}};
        for (int i = 0; i < expectedIncomingAdjLists.length; i++) {
            SortedAdjacencyList expected = new SortedAdjacencyList();
            for (int j = 0; j < expectedIncomingAdjLists[i].length; j++) {
                expected.add(expectedIncomingAdjLists[i][j], expectedIncomingEdgeTypes[i][j],
                    expectedIncomingEdgeIds[i][j]);
            }
            Assert.assertTrue("Testing BACKWARD vertex id: " + i, SortedAdjacencyList.isSameAs(
                graph.getSortedAdjacencyList(i, Direction.BACKWARD, GraphVersion.PERMANENT),
                expected));
        }
    }

    @Test
    public void testDeletionOfNonExistingEdges() throws Exception {
        // Create a graph.
        int[][] edges = {{0, 3}, {0, 1}, {1, 3}, {1, 2}, {4, 0}, {4, 1}};
        short[] edgeTypes = {3, 1, 3, 2, 0, 9};
        short[][] vertexTypes = {{0, 3}, {0, 1}, {1, 3}, {1, 2}, {4, 0}, {4, 1}};
        Graph graph = TestUtils.initializeGraphPermanently(edges, edgeTypes, vertexTypes);
        // Delete a list of edges.
        int[][] deleteEdges = {{0, 1}, {4, 1}}; /* Edges exist in the graph.*/
        short[] deleteEdgeTypes = {1, 9};
        for (int i = 0; i < deleteEdges.length; i++) {
            graph.deleteEdgeTemporarily(deleteEdges[i][0], deleteEdges[i][1], deleteEdgeTypes[i]);
        }
        graph.finalizeChanges();

        int[][] deleteNonExistingEdges = {{120, 34}, {1, 42}};
        short[] deleteNonExistingEdgeTypes = {14, 56};
        for (int i = 0; i < deleteNonExistingEdges.length; i++) {
            try {
                graph.deleteEdgeTemporarily(deleteNonExistingEdges[i][0],
                    deleteNonExistingEdges[i][1], deleteNonExistingEdgeTypes[i]);
                Assert.fail("NoSuchElementException should have been thrown.");
            } catch (NoSuchElementException e) {
                // Expected exception caught.
            }
        }
    }

    private Graph getGraphWithTemporaryChanges() {
        // Create a graph with some initial edges.
        Graph graph = Graph.getInstance();
        graph.addEdgeTemporarily(0, 1, (short) 2, (short) 1, null /* no fromVertex properties */,
            null /* no toVertex properties */, (short) 1, null /* no edge properties */);
        graph.addEdgeTemporarily(11, 9, (short) 2, (short) 1, null /* no fromVertex properties */,
            null /* no toVertex properties */, (short) 2, null /* no edge properties */);
        graph.finalizeChanges();

        // Add and delete some edges temporarily.
        int[][] edges = {{0, 6}, {2, 6}, {5, 6}, {4, 6}, {2, 5}};
        short[] edgeTypes = {1, 1, 1, 1, -1};
        short[] fromVertexTypes = {2, 2, 2, 4, 2};
        short[] toVertexTypes = {6, 6, 6, 6, 2};
        for (int i = 0; i < edges.length; i++) {
            graph.addEdgeTemporarily(edges[i][0], edges[i][1], fromVertexTypes[i],
                toVertexTypes[i], null /* no fromVertex properties */, null /* no toVertex
                properties */, edgeTypes[i], null /* no edge properties */);
        }
        graph.deleteEdgeTemporarily(2, 6, (short) 1);
        graph.deleteEdgeTemporarily(0, 1, (short) 1);
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
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.FORWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */),
            new int[][]{{0, 1}, {11, 9}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.BACKWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */),
            new int[][]{{1, 0}, {9, 11}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the permanent graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.FORWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{{0, 6},
            {2, 5}, {4, 6}, {5, 6}, {11, 9}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.BACKWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{{5, 2},
            {6, 0}, {6, 4}, {6, 5}, {9, 11}});
    }

    @Test
    public void testGetEdgesIteratorWithTypesForPermanentGraph() {
        Graph graph = getGraphWithTemporaryChanges();
        short edgeType = 1;
        // Test the list of edges of the permanent graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.FORWARD,
            edgeType, null /* no edge properties */), new int[][]{{0, 1}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.BACKWARD,
            edgeType, null /* no edge properties */), new int[][]{{1, 0}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();
        // Test the list of edges of the permanent graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.FORWARD,
            edgeType, null /* no edge properties */), new int[][]{{0, 6}, {4, 6}, {5, 6}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.PERMANENT, Direction.BACKWARD,
            edgeType, null /* no edge properties */), new int[][]{{6, 0}, {6, 4}, {6, 5}});
    }

    /**
     * Tests that the iterator returned by {@code getEdgesIterator()} correctly returns the
     * list of edges for the {@link GraphVersion#MERGED} graph.
     */
    @Test
    public void testGetEdgesIteratorForMergedGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();

        // Test the list of edges of the merged graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.FORWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{{0, 6},
            {2, 5}, {4, 6}, {5, 6}, {11, 9}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.BACKWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{{5, 2},
            {6, 0}, {6, 4}, {6, 5}, {9, 11}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the merged graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.FORWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{{0, 6},
            {2, 5}, {4, 6}, {5, 6}, {11, 9}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.BACKWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{{5, 2},
            {6, 0}, {6, 4}, {6, 5}, {9, 11}});
    }

    @Test
    public void testGetEdgesIteratorWithTypesForMergedGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();
        short edgeType = 1;
        // Test the list of edges of the merged graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.FORWARD,
            edgeType, null /* no edge properties */), new int[][]{{0, 6}, {4, 6}, {5, 6}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.BACKWARD,
            edgeType, null /* no edge properties */), new int[][]{{6, 0}, {6, 4}, {6, 5}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the merged graph after finalizing the changes. The results
        // should be the same because iterating the merged graph defaults to iterating the
        // permanent graph when the merged graph is empty.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.FORWARD,
            edgeType, null /* no edge properties */), new int[][]{{0, 6}, {4, 6}, {5, 6}});
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.MERGED, Direction.BACKWARD,
            edgeType, null /* no edge properties */), new int[][]{{6, 0}, {6, 4}, {6, 5}});
    }

    /**
     * Tests that the iterator returned by {@code getEdgesIterator()} correctly returns the
     * list of edges for the {@link GraphVersion#DIFF_PLUS} graph.
     */
    @Test
    public void testGetEdgesIteratorForDiffPlusGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();

        // Test the list of edges of the diff plus graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_PLUS, Direction.FORWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{{0, 6},
            {2, 6}, {5, 6}, {4, 6}, {2, 5}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the diff plus graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_PLUS, Direction.FORWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{});
    }

    @Test
    public void testGetEdgesIteratorWithTypesForDiffPlusGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();
        short edgeType = 1;
        // Test the list of edges of the diff plus graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_PLUS, Direction.FORWARD,
            edgeType, null /* no edge properties */), new int[][]{{0, 6}, {2, 6}, {5, 6}, {4, 6}});

        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the diff plus graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_PLUS, Direction.FORWARD,
            edgeType, null /* no edge properties */), new int[][]{});
    }

    /**
     * Tests that the iterator returned by {@code getEdgesIterator()} correctly returns the
     * list of edges for the {@link GraphVersion#DIFF_MINUS} graph.
     */
    @Test
    public void testGetEdgesIteratorForDiffMinusGraph() throws Exception {
        Graph graph = getGraphWithTemporaryChanges();

        // Test the list of edges of the diff minus graph before finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_MINUS, Direction.FORWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{{2, 6},
            {0, 1}});
        // Make the temporary changes permanent.
        graph.finalizeChanges();

        // Test the list of edges of the diff minus graph after finalizing the changes.
        assertEdgesIterator(graph.getEdgesIterator(GraphVersion.DIFF_MINUS, Direction.FORWARD,
            TypeAndPropertyKeyStore.ANY, null /* no edge properties */), new int[][]{});
    }

    private void assertEdgesIterator(Iterator<int[]> iterator, int[][] expectedEdges) {
        List<int[]> edgesList = new ArrayList<>();
        while (iterator.hasNext()) {
            edgesList.add(iterator.next());
        }
        Assert.assertArrayEquals(expectedEdges, edgesList.toArray());
    }
}

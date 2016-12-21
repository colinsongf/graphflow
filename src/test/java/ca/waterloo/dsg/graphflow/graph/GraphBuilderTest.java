package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Tests {@link GraphBuilder}.
 */
public class GraphBuilderTest {

    /**
     * Test the initialization of a {@code graph} instance from a JSON file.
     */
    @Test
    public void createInstance() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("graph.json").getPath());
        Graph graph = GraphBuilder.createInstance(file);

        // Test vertex count.
        Assert.assertEquals(6, graph.getVertexCount());
        // Test outgoing adjacency lists.
        int[][] expectedOutgoingAdjLists = {{1}, {2}, {0, 3, 4}, {0}, {5}, {}};
        short[][] expectedOutgoingAdjListEdgeTypes = {{1}, {4}, {1, 6, 10}, {6}, {14}, {}};
        for (int i = 0; i < expectedOutgoingAdjLists.length; i++) {
            SortedAdjacencyList expectedResult = new SortedAdjacencyList();
            for (int j = 0; j < expectedOutgoingAdjLists[i].length; j++) {
                expectedResult.add(expectedOutgoingAdjLists[i][j],
                    expectedOutgoingAdjListEdgeTypes[i][j]);
            }
            Assert.assertTrue("Testing FORWARD vertex id: " + i, SortedAdjacencyList.
                isSameAs(graph.getSortedAdjacencyList(i, Direction.FORWARD, GraphVersion.PERMANENT),
                    expectedResult));
        }
        // Test incoming adjacency lists.
        int[][] expectedIncomingAdjLists = {{2, 3}, {0}, {1}, {2}, {2}, {4}};
        short[][] expectedIncomingAdjListEdgeTypes = {{1, 6}, {1}, {4}, {6}, {10}, {14}};
        for (int i = 0; i < expectedIncomingAdjLists.length; i++) {
            SortedAdjacencyList expectedResult = new SortedAdjacencyList();
            for (int j = 0; j < expectedIncomingAdjLists[i].length; j++) {
                expectedResult.add(expectedIncomingAdjLists[i][j],
                    expectedIncomingAdjListEdgeTypes[i][j]);
            }
            Assert.assertTrue("Testing BACKWARD vertex id: " + i, SortedAdjacencyList.
                isSameAs(graph.getSortedAdjacencyList(i, Direction.BACKWARD,
                    GraphVersion.PERMANENT), expectedResult));
        }
    }
}

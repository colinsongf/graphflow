package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

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
        for (int i = 0; i < expectedOutgoingAdjLists.length; i++) {
            Assert.assertTrue("Testing FORWARD vertex id: " + i, graph.getAdjacencyList(i,
                Direction.FORWARD, GraphVersion.PERMANENT).isSameAs
                (expectedOutgoingAdjLists[i]));
        }
        // Test incoming adjacency lists.
        int[][] expectedIncomingAdjLists = {{2, 3}, {0}, {1}, {2}, {2}, {4}};
        for (int i = 0; i < expectedIncomingAdjLists.length; i++) {
            Assert.assertTrue("Testing BACKWARD vertex id: " + i, graph.getAdjacencyList(i,
                Direction.BACKWARD, GraphVersion.PERMANENT).isSameAs
                (expectedIncomingAdjLists[i]));
        }
    }
}

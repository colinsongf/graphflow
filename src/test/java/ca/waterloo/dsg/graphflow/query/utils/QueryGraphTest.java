package ca.waterloo.dsg.graphflow.query.utils;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import org.junit.Assert;
import org.junit.Test;

public class QueryGraphTest {

    @Test
    public void testNumberOfAdjacentRelations() throws Exception {
        // Create the {@code QueryGraph}.
        QueryGraph queryGraph = new QueryGraph();
        // Three relations between "a" and "b" with defined edge types.
        queryGraph.addEdge(new QueryEdge(new QueryVariable("a"), new QueryVariable("b"),
            Direction.FORWARD, "FOLLOWS"));
        queryGraph.addEdge(new QueryEdge(new QueryVariable("a"), new QueryVariable("b"),
            Direction.FORWARD, "LIKES"));
        queryGraph.addEdge(new QueryEdge(new QueryVariable("b"), new QueryVariable("a"),
            Direction.FORWARD, "LIKES"));
        // Two relations between "b" and "c" with undefined edge types.
        queryGraph.addEdge(new QueryEdge(new QueryVariable("b"), new QueryVariable("c")));
        queryGraph.addEdge(new QueryEdge(new QueryVariable("c"), new QueryVariable("b")));
        // One relation between "a" and "c" with a defined edge type.
        queryGraph.addEdge(new QueryEdge(new QueryVariable("c"), new QueryVariable("a"),
            Direction.FORWARD, "LIKES"));

        // Test the number of adjacent relations for each variable.
        Assert.assertEquals(4, queryGraph.getNumberOfAdjacentRelations("a"));
        Assert.assertEquals(5, queryGraph.getNumberOfAdjacentRelations("b"));
        Assert.assertEquals(3, queryGraph.getNumberOfAdjacentRelations("c"));
    }
}

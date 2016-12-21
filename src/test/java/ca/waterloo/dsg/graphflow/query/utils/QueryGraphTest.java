package ca.waterloo.dsg.graphflow.query.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link QueryGraph}.
 */
public class QueryGraphTest {

    /**
     * Tests that the {@link QueryGraph} stores the correct number of adjacent relations when
     * multiple relations are present between two variables.
     */
    @Test
    public void testNumberOfAdjacentRelations() throws Exception {
        // Create the {@code QueryGraph}.
        QueryGraph queryGraph = new QueryGraph();
        // Three relations between "a" and "b" with defined edge types.
        queryGraph.addEdge(new QueryEdge(new QueryVariable("a"), new QueryVariable("b"),
            "FOLLOWS"));
        queryGraph.addEdge(new QueryEdge(new QueryVariable("a"), new QueryVariable("b"), "LIKES"));
        queryGraph.addEdge(new QueryEdge(new QueryVariable("b"), new QueryVariable("a"), "LIKES"));
        // Two relations between "b" and "c" with undefined edge types.
        queryGraph.addEdge(new QueryEdge(new QueryVariable("b"), new QueryVariable("c")));
        queryGraph.addEdge(new QueryEdge(new QueryVariable("c"), new QueryVariable("b")));
        // One relation between "a" and "c" with a defined edge type.
        queryGraph.addEdge(new QueryEdge(new QueryVariable("c"), new QueryVariable("a"), "LIKES"));

        // Test the number of adjacent relations for each variable.
        Assert.assertEquals(4, queryGraph.getNumberOfAdjacentRelations("a"));
        Assert.assertEquals(5, queryGraph.getNumberOfAdjacentRelations("b"));
        Assert.assertEquals(3, queryGraph.getNumberOfAdjacentRelations("c"));
    }
}

package ca.waterloo.dsg.graphflow.query.structuredquery;

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
        queryGraph.addRelation(new QueryRelation(new QueryVariable("a"), new QueryVariable("b"),
            "FOLLOWS"));
        queryGraph.addRelation(new QueryRelation(new QueryVariable("a"), new QueryVariable("b"),
            "LIKES"));
        queryGraph.addRelation(new QueryRelation(new QueryVariable("b"), new QueryVariable("a"),
            "LIKES"));
        // Two relations between "b" and "c" with undefined edge types.
        queryGraph.addRelation(new QueryRelation(new QueryVariable("b"), new QueryVariable("c")));
        queryGraph.addRelation(new QueryRelation(new QueryVariable("c"), new QueryVariable("b")));
        // One relation between "a" and "c" with a defined edge type.
        queryGraph.addRelation(new QueryRelation(new QueryVariable("c"), new QueryVariable("a"),
            "LIKES"));

        // Test the number of adjacent relations for each variable.
        Assert.assertEquals(4, queryGraph.getNumberOfAdjacentRelations("a"));
        Assert.assertEquals(5, queryGraph.getNumberOfAdjacentRelations("b"));
        Assert.assertEquals(3, queryGraph.getNumberOfAdjacentRelations("c"));
    }
}

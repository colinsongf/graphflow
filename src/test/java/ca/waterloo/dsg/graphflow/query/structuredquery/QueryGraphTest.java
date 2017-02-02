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
        QueryVariable queryVariable1 = new QueryVariable("a");
        QueryVariable queryVariable2 = new QueryVariable("b");
        QueryVariable queryVariable3 = new QueryVariable("c");

        QueryRelation queryRelation = new QueryRelation(queryVariable1, queryVariable2);
        queryRelation.setRelationType("FOLLOWS");
        queryGraph.addRelation(queryRelation);
        queryRelation.setRelationType("LIKES");
        queryGraph.addRelation(queryRelation);
        queryRelation = new QueryRelation(queryVariable2, queryVariable1);
        queryGraph.addRelation(queryRelation);
        // Two relations between "b" and "c" with undefined edge types.
        queryRelation = new QueryRelation(queryVariable2, queryVariable3);
        queryGraph.addRelation(queryRelation);
        queryRelation = new QueryRelation(queryVariable3, queryVariable2);
        queryGraph.addRelation(queryRelation);
        // One relation between "a" and "c" with a defined edge type.
        queryRelation = new QueryRelation(queryVariable3, queryVariable1);
        queryRelation.setRelationType("LIKES");
        queryGraph.addRelation(queryRelation);

        // Test the number of adjacent relations for each variable.
        Assert.assertEquals(4, queryGraph.getNumberOfAdjacentRelations("a"));
        Assert.assertEquals(5, queryGraph.getNumberOfAdjacentRelations("b"));
        Assert.assertEquals(3, queryGraph.getNumberOfAdjacentRelations("c"));
    }
}

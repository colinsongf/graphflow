package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link StructuredQueryParser}.
 */
public class StructuredQueryParserTest {

    /**
     * Tests the parsing of a triangle MATCH query with no types.
     */
    @Test
    public void testParseTriangleMatchQuery() throws Exception {
        StructuredQuery actualStructuredQuery;
        String query = "MATCH (a)->(b),(b)->(c),(c)->(a);";
        try {
            actualStructuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        StructuredQuery expectedStructuredQuery = new StructuredQuery();
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("a"),
            new QueryVariable("b")));
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("b"),
            new QueryVariable("c")));
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("c"),
            new QueryVariable("a")));
        expectedStructuredQuery.setQueryOperation(StructuredQuery.QueryOperation.MATCH);

        Assert.assertTrue(StructuredQuery.isSameAs(actualStructuredQuery, expectedStructuredQuery));
    }

    /**
     * Tests the parsing of a CREATE query.
     */
    @Test
    public void testParseCreateQuery() throws Exception {
        StructuredQuery actualStructuredQuery;
        String query = "CREATE (1:Person { name: Olivier })-[:FOLLOWS { really:   2yes }]->" +
            "(2:Person { name: Mohannad }), (2:Person { name: Mohannad } )-[:FOLLOWS]->(3:Person { name: Sid })," +
            "(1:Person {name: Olivier})-[:FOLLOWS]->(3:Person { name: Sid });";
        try {
            actualStructuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        StructuredQuery expectedStructuredQuery = new StructuredQuery();
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("1", "Person"),
            new QueryVariable("2", "Person"), "FOLLOWS"));
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("2", "Person"),
            new QueryVariable("3", "Person"), "FOLLOWS"));
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("1", "Person"),
            new QueryVariable("3", "Person"), "FOLLOWS"));
        expectedStructuredQuery.setQueryOperation(StructuredQuery.QueryOperation.CREATE);

        Assert.assertTrue(StructuredQuery.isSameAs(actualStructuredQuery, expectedStructuredQuery));
    }

    /**
     * Tests the parsing of a DELETE query.
     */
    @Test
    public void testParseDeleteQuery() throws Exception {
        StructuredQuery actualStructuredQuery;
        String query = "DELETE (1)->(2),(2)->(3);";
        try {
            actualStructuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        StructuredQuery expectedStructuredQuery = new StructuredQuery();
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("1"),
            new QueryVariable("2")));
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("2"),
            new QueryVariable("3")));
        expectedStructuredQuery.setQueryOperation(StructuredQuery.QueryOperation.DELETE);

        Assert.assertTrue(StructuredQuery.isSameAs(actualStructuredQuery, expectedStructuredQuery));
    }

    /**
     * Tests the parsing of a SHORTEST PATH query.
     */
    @Test
    public void parseSimpleShortestPathQuery() throws Exception {
        StructuredQuery actualStructuredQuery;
        String query = "SHORTEST PATH (0, 9)";
        try {
            actualStructuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        StructuredQuery expectedStructuredQuery = new StructuredQuery();
        expectedStructuredQuery.addRelation(new QueryRelation(new QueryVariable("0"),
            new QueryVariable("9")));
        expectedStructuredQuery.setQueryOperation(StructuredQuery.QueryOperation.SHORTEST_PATH);

        Assert.assertTrue(StructuredQuery.isSameAs(actualStructuredQuery, expectedStructuredQuery));
    }
}

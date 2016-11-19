package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Assert;
import org.junit.Test;

public class StructuredQueryParserTest {
    @Test
    public void testParseTriangleMatchQuery() throws Exception {
        StructuredQuery structuredQueryActual;
        String query = "MATCH (a)->(b),(b)->(c),(c)->(a);";
        try {
            structuredQueryActual = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        StructuredQuery structuredQueryExpected = new StructuredQuery();
        structuredQueryExpected.addEdge(new StructuredQueryEdge("a", "b"));
        structuredQueryExpected.addEdge(new StructuredQueryEdge("b", "c"));
        structuredQueryExpected.addEdge(new StructuredQueryEdge("c", "a"));
        structuredQueryExpected.setQueryOperation(StructuredQuery.QueryOperation.MATCH);

        Assert.assertTrue(structuredQueryActual.isSameAs(structuredQueryExpected));
    }

    @Test
    public void testParseCreateQuery() throws Exception {
        StructuredQuery structuredQueryActual;
        String query = "CREATE (1)->(2),(2)->(3),(1)->(3);";
        try {
            structuredQueryActual = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        StructuredQuery structuredQueryExpected = new StructuredQuery();
        structuredQueryExpected.addEdge(new StructuredQueryEdge("1", "2"));
        structuredQueryExpected.addEdge(new StructuredQueryEdge("2", "3"));
        structuredQueryExpected.addEdge(new StructuredQueryEdge("1", "3"));
        structuredQueryExpected.setQueryOperation(StructuredQuery.QueryOperation.CREATE);

        Assert.assertTrue(structuredQueryActual.isSameAs(structuredQueryExpected));
    }

    @Test
    public void testParseDeleteQuery() throws Exception {
        StructuredQuery structuredQueryActual;
        String query = "DELETE (1)->(2),(2)->(3);";
        try {
            structuredQueryActual = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        StructuredQuery structuredQueryExpected = new StructuredQuery();
        structuredQueryExpected.addEdge(new StructuredQueryEdge("1", "2"));
        structuredQueryExpected.addEdge(new StructuredQueryEdge("2", "3"));
        structuredQueryExpected.setQueryOperation(StructuredQuery.QueryOperation.DELETE);

        Assert.assertTrue(structuredQueryActual.isSameAs(structuredQueryExpected));
    }

    @Test
    public void parseSimpleShortestPathQuery() throws Exception {
        StructuredQuery result;
        String query = "SHORTEST_PATH (0, 9)";
        try {
            result = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }
        StructuredQuery expected = new StructuredQuery();
        expected.addEdge(new StructuredQueryEdge("0", "9"));
        expected.setOperation(StructuredQuery.Operation.SHORTEST_PATH);
        Assert.assertTrue(result.equalsTo(expected));
    }
}

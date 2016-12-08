package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariable;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class StructuredQueryParserTest {

    @Rule
    public ExpectedException parseException = ExpectedException.none();

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
        structuredQueryExpected.addEdge(new QueryEdge(new QueryVariable("a"),
            new QueryVariable("b")));
        structuredQueryExpected.addEdge(new QueryEdge(new QueryVariable("b"),
            new QueryVariable("c")));
        structuredQueryExpected.addEdge(new QueryEdge(new QueryVariable("c"),
            new QueryVariable("a")));
        structuredQueryExpected.setQueryOperation(StructuredQuery.QueryOperation.MATCH);

        Assert.assertTrue(StructuredQuery.isSameAs(structuredQueryActual, structuredQueryExpected));
    }

    @Test
    public void testParseCreateQuery() throws Exception {
        StructuredQuery structuredQueryActual;
        String query = "CREATE (1:Person)-[:FOLLOWS]->(2:Person),(2:Person)-[:FOLLOWS]->" +
            "(3:Person),(1:Person)-[:FOLLOWS]->(3:Person);";
        try {
            structuredQueryActual = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        StructuredQuery structuredQueryExpected = new StructuredQuery();
        structuredQueryExpected.addEdge(new QueryEdge(new QueryVariable("1", "Person"),
            new QueryVariable("2", "Person"), Direction.FORWARD, "FOLLOWS"));
        structuredQueryExpected.addEdge(new QueryEdge(new QueryVariable("2", "Person"),
            new QueryVariable("3", "Person"), Direction.FORWARD, "FOLLOWS"));
        structuredQueryExpected.addEdge(new QueryEdge(new QueryVariable("1", "Person"),
            new QueryVariable("3", "Person"), Direction.FORWARD, "FOLLOWS"));
        structuredQueryExpected.setQueryOperation(StructuredQuery.QueryOperation.CREATE);

        Assert.assertTrue(StructuredQuery.isSameAs(structuredQueryActual, structuredQueryExpected));
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
        structuredQueryExpected.addEdge(new QueryEdge(new QueryVariable("1"),
            new QueryVariable("2")));
        structuredQueryExpected.addEdge(new QueryEdge(new QueryVariable("2"),
            new QueryVariable("3")));
        structuredQueryExpected.setQueryOperation(StructuredQuery.QueryOperation.DELETE);

        Assert.assertTrue(StructuredQuery.isSameAs(structuredQueryActual, structuredQueryExpected));
    }

    @Test
    public void parseSimpleShortestPathQuery() throws Exception {
        StructuredQuery result;
        String query = "SHORTEST PATH (0, 9)";
        try {
            result = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }
        StructuredQuery expected = new StructuredQuery();
        expected.addEdge(new QueryEdge(new QueryVariable("0"), new QueryVariable("9")));
        expected.setQueryOperation(StructuredQuery.QueryOperation.SHORTEST_PATH);
        Assert.assertTrue(StructuredQuery.isSameAs(result, expected));
    }
}

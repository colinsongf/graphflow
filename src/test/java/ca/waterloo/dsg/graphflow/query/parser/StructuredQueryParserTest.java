package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Assert;
import org.junit.Test;

public class StructuredQueryParserTest {
    @Test
    public void parseTriangleMatchQuery() throws Exception {
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
        structuredQueryExpected.setOperation(StructuredQuery.Operation.MATCH);

        Assert.assertTrue(structuredQueryActual.equalsTo(structuredQueryExpected));
    }
}

package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
     * Tests the parsing of a CREATE edges query.
     */
    @Test
    public void testParseCreateEdgesQuery() throws Exception {
        StructuredQuery actualStructuredQuery;
        String query = "CREATE (1:Person { name:String='Olivier' })-[:FOLLOWS]->(2:Person " +
            "{ name:String='Mohannad' }), (2:Person { name:String='Mohannad' } )-[:FOLLOWS]->" +
            "(3:Person { name:String='Sid' }), (1:Person { name:String='Olivier'})-[:FOLLOWS]->" +
            "(3:Person { name:String='Sid' });";
        try {
            actualStructuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        QueryVariable queryVariable1 = new QueryVariable("1");
        queryVariable1.setVariableType("Person");
        Map<String, Pair<String, String>> properties1 = new HashMap<>();
        properties1.put("name", new Pair<>("string", "Olivier"));
        queryVariable1.setVariableProperties(properties1);

        QueryVariable queryVariable2 = new QueryVariable("2");
        queryVariable2.setVariableType("Person");
        Map<String, Pair<String, String>> properties2 = new HashMap<>();
        properties2.put("name", new Pair<>("string", "Mohannad"));
        queryVariable2.setVariableProperties(properties2);

        QueryVariable queryVariable3 = new QueryVariable("3");
        queryVariable3.setVariableType("Person");
        Map<String, Pair<String, String>> properties3 = new HashMap<>();
        properties3.put("name", new Pair<>("string", "Sid"));
        queryVariable3.setVariableProperties(properties3);

        StructuredQuery expectedStructuredQuery = new StructuredQuery();
        expectedStructuredQuery.setQueryOperation(StructuredQuery.QueryOperation.CREATE);
        QueryRelation queryRelation1 = new QueryRelation(queryVariable1, queryVariable2);
        queryRelation1.setRelationType("FOLLOWS");
        expectedStructuredQuery.addRelation(queryRelation1);
        QueryRelation queryRelation2 = new QueryRelation(queryVariable2, queryVariable3);
        queryRelation2.setRelationType("FOLLOWS");
        expectedStructuredQuery.addRelation(queryRelation2);
        QueryRelation queryRelation3 = new QueryRelation(queryVariable1, queryVariable3);
        queryRelation3.setRelationType("FOLLOWS");
        expectedStructuredQuery.addRelation(queryRelation3);

        Assert.assertTrue(StructuredQuery.isSameAs(actualStructuredQuery, expectedStructuredQuery));
    }

    /**
     * Tests the parsing of a CREATE vertices query.
     */
    @Test
    public void testParseCreateVerticesQuery() throws Exception {
        StructuredQuery actualStructuredQuery;
        String query = "CREATE (1:Person { name: String = 'Olivier' }), (2:Person " +
            "{ name: String = 'Mohannad' }), (3:Person { name: String = 'Sid' })";
        try {
            actualStructuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            throw new Exception("ERROR parsing: " + e.getMessage());
        }

        QueryVariable queryVariable1 = new QueryVariable("1");
        queryVariable1.setVariableType("Person");
        Map<String, Pair<String, String>> properties1 = new HashMap<>();
        properties1.put("name", new Pair<>("string", "Olivier"));
        queryVariable1.setVariableProperties(properties1);

        QueryVariable queryVariable2 = new QueryVariable("2");
        queryVariable2.setVariableType("Person");
        Map<String, Pair<String, String>> properties2 = new HashMap<>();
        properties2.put("name", new Pair<>("string", "Mohannad"));
        queryVariable2.setVariableProperties(properties2);

        QueryVariable queryVariable3 = new QueryVariable("3");
        queryVariable3.setVariableType("Person");
        Map<String, Pair<String, String>> properties3 = new HashMap<>();
        properties3.put("name", new Pair<>("string", "Sid"));
        queryVariable3.setVariableProperties(properties3);

        StructuredQuery expectedStructuredQuery = new StructuredQuery();
        expectedStructuredQuery.setQueryOperation(StructuredQuery.QueryOperation.CREATE);
        expectedStructuredQuery.addVariable(queryVariable1);
        expectedStructuredQuery.addVariable(queryVariable2);
        expectedStructuredQuery.addVariable(queryVariable3);

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

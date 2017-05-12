package ca.waterloo.dsg.graphflow.query.operator.filter;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Predicate;

/**
 * Tests the static functions of the {@link FilterPredicateFactory} class for generating
 * {@link Predicate} lambda expressions.
 */
public class FilterPredicateFactoryTest {

    @Before
    public void setUp() throws Exception {
        GraphDBState.reset();
        String createQuery = "CREATE (0:Person {name: 'name0', age: 20, views: 120 })" +
            "-[:FOLLOWS { isRelated: true, views: 100 }]->(1:Person { name: 'name1', age: 10, " +
            "views: 50 }),(0:Person)-[:LIKES]->(1:Person),(1:Person)-[:LIKES]->" +
            "(0:Person),(1:Person)-[:TAGGED]->(3:Person),(3:Person)-[:LIKES{ rating: 4.1, " +
            "views: 300}]->(1:Person);";
        TestUtils.initializeGraphPermanentlyWithProperties(createQuery);
    }

    @Test
    public void testTwoVertexPropertyPredicate() {
        String propertyKey = "age";
        QueryPropertyPredicate queryPropertyPredicate = TestUtils.createQueryPropertyPredicate(
            new Pair<>("a", propertyKey), new Pair<>("b", propertyKey), null, ComparisonOperator.
                GREATER_THAN);
        int vertex1PropertyResultIndex = 0;
        int vertex2PropertyResultIndex = 2;
        Predicate<String[]> predicate = FilterPredicateFactory.getFilterPredicate(
            queryPropertyPredicate, vertex1PropertyResultIndex, vertex2PropertyResultIndex);
        String[] resolvedProperties = {"15", "20", "10"};
        Assert.assertTrue(predicate.test(resolvedProperties));
    }

    @Test
    public void testEdgeAndLiteralPropertyPredicate() {
        String propertyKey = "views";
        QueryPropertyPredicate queryPropertyPredicate = TestUtils.createQueryPropertyPredicate(
            new Pair<>("a", propertyKey), null, "74", ComparisonOperator.GREATER_THAN_OR_EQUAL);
        int edgePropertyResultIndex = 0;
        Predicate<String[]> predicate = FilterPredicateFactory.getFilterPredicate(
            queryPropertyPredicate, edgePropertyResultIndex, -1/* The index for variable2 goes
            unused.*/);
        String[] resolvedProperties = {"75", "20", "10"};
        Assert.assertTrue(predicate.test(resolvedProperties));
    }
}
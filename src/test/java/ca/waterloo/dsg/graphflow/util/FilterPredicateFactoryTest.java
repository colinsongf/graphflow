package ca.waterloo.dsg.graphflow.util;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.util.FilterPredicateFactory;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate.PredicateType;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class FilterPredicateFactoryTest {

    QueryPropertyPredicate queryPropertyPredicate;
    @Before
    public void setUp() throws Exception {
        Graph.getInstance().reset();
        String createQuery = "CREATE (0:Person{name:string=name0, age:int=20, views:int=120})" +
            "-[:FOLLOWS{isRelated:boolean=true, views:int=100}]->(1:Person{name:string=name1, age:int=10, views:int=50}),(0:Person)" +
            "-[:LIKES]->" +
            "(1:Person)," +
            "(1:Person)-[:LIKES]->(0:Person)," +
            "(1:Person)-[:TAGGED]->(3:Person),(3:Person)-[:LIKES{rating:double=4.1, " +
            "views:int=300}]->" +
            "(1:Person)," +
            "(3:Person)-[:FOLLOWS{isRelated:boolean=true, views:int=500}]->(0:Person),(4:Person)" +
            "-[:FOLLOWS]->(1:Person)," +
            "(4:Person)-[:LIKES]->(1:Person),(1:Person)-[:LIKES]->(4:Person)," +
            "(3:Person)-[:FOLLOWS]->(4:Person), (4:Person)-[:LIKES]->(3:Person);";
        TestUtils.initializeGraphPermanentlyWithProperties(createQuery);
    }

    private void initializeQueryPropertyPredicate(Pair<String, Short>
        variable1, Pair<String, Short> variable2, String constant, ComparisonOperator
        comparisonOperator, PredicateType predicateType) {
        queryPropertyPredicate = new QueryPropertyPredicate();
        queryPropertyPredicate.setVariable1(variable1);
        queryPropertyPredicate.setVariable2(variable2);
        queryPropertyPredicate.setConstant(constant);
        queryPropertyPredicate.setComparisonOperator(comparisonOperator);
        queryPropertyPredicate.setPredicateType(predicateType);
    }

    @Test
    public void testTwoVertexPropertyPredicate() {
        short propertyKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort
            ("age");
        initializeQueryPropertyPredicate(new Pair<>("a", propertyKey), new Pair<>("b",
            propertyKey), null, ComparisonOperator.GREATER_THAN, PredicateType.TWO_VERTEX);
        Map<String, Integer> orderedVertexVariableIndexMap = new HashMap<>();
        orderedVertexVariableIndexMap.put("a", 0);
        orderedVertexVariableIndexMap.put("b", 2);

        Predicate<MatchQueryOutput> predicate = FilterPredicateFactory.getFilterPredicate
            (queryPropertyPredicate, orderedVertexVariableIndexMap, null);
        MatchQueryOutput matchQueryOutput= new MatchQueryOutput();
        int[] outputVertexIds = {0, 2, 1};
        matchQueryOutput.vertexIds = outputVertexIds;
        Assert.assertTrue(predicate.test(matchQueryOutput));
    }

    @Test
    public void testTwoEdgePropertyPredicate() {
        short propertyKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort
            ("views");
        initializeQueryPropertyPredicate(new Pair<>("b", propertyKey), new Pair<>("a",
            propertyKey), null, ComparisonOperator.LESS_THAN_OR_EQUAL, PredicateType.TWO_EDGE);
        Map<String, Integer> orderedEdgeVariableIndexMap = new HashMap<>();
        orderedEdgeVariableIndexMap.put("a", 0);
        orderedEdgeVariableIndexMap.put("b", 2);

        Predicate<MatchQueryOutput> predicate = FilterPredicateFactory.getFilterPredicate
            (queryPropertyPredicate, null, orderedEdgeVariableIndexMap);
        MatchQueryOutput matchQueryOutput= new MatchQueryOutput();
        long[] outputEdgeIds = {4, 2, 0};
        matchQueryOutput.edgeIds = outputEdgeIds;
        Assert.assertTrue(predicate.test(matchQueryOutput));
    }

    @Test
    public void testEdgeAndVertexPropertyPredicate() {
        short propertyKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort
            ("views");
        initializeQueryPropertyPredicate(new Pair<>("a", propertyKey), new Pair<>("b",
            propertyKey), null, ComparisonOperator.GREATER_THAN_EQUAL, PredicateType.EDGE_AND_VERTEX);
        Map<String, Integer> orderedVertexVariableIndexMap = new HashMap<>();
        orderedVertexVariableIndexMap.put("a", 0);
        orderedVertexVariableIndexMap.put("c", 2);
        Map<String, Integer> orderedEdgeVariableIndexMap = new HashMap<>();
        orderedEdgeVariableIndexMap.put("b", 3);
        orderedEdgeVariableIndexMap.put("d", 2);
        Predicate<MatchQueryOutput> predicate = FilterPredicateFactory.getFilterPredicate
            (queryPropertyPredicate, orderedVertexVariableIndexMap, orderedEdgeVariableIndexMap);
        MatchQueryOutput matchQueryOutput= new MatchQueryOutput();
        int[] outputVertexIds = {0, 2, 3};
        long[] outputEdgeIds = {4, 2, 1, 0};
        matchQueryOutput.vertexIds = outputVertexIds;
        matchQueryOutput.edgeIds = outputEdgeIds;
        Assert.assertTrue(predicate.test(matchQueryOutput));
    }

    @Test
    public void testEdgeAndConstantPropertyPredicate() {
        short propertyKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort
            ("views");
        initializeQueryPropertyPredicate(new Pair<>("a", propertyKey), null, "74",
            ComparisonOperator.GREATER_THAN_EQUAL, PredicateType.EDGE_AND_CONSTANT);
        Map<String, Integer> orderedEdgeVariableIndexMap = new HashMap<>();
        orderedEdgeVariableIndexMap.put("a", 3);
        orderedEdgeVariableIndexMap.put("d", 2);
        Predicate<MatchQueryOutput> predicate = FilterPredicateFactory.getFilterPredicate
            (queryPropertyPredicate, null, orderedEdgeVariableIndexMap);
        MatchQueryOutput matchQueryOutput = new MatchQueryOutput();
        long[] outputEdgeIds = {0, 2, 1, 4};
        matchQueryOutput.edgeIds = outputEdgeIds;
        Assert.assertTrue(predicate.test(matchQueryOutput));
    }

    @Test
    public void testVertexAndConstantPropertyPredicate() {
        short propertyKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort
            ("views");
        initializeQueryPropertyPredicate(new Pair<>("a", propertyKey), null, "74",
            ComparisonOperator.GREATER_THAN_EQUAL, PredicateType.VERTEX_AND_CONSTANT);
        Map<String, Integer> orderedVertexVariableIndexMap = new HashMap<>();
        orderedVertexVariableIndexMap.put("a", 3);
        orderedVertexVariableIndexMap.put("d", 2);
        Predicate<MatchQueryOutput> predicate = FilterPredicateFactory.getFilterPredicate
            (queryPropertyPredicate, orderedVertexVariableIndexMap, null);
        MatchQueryOutput matchQueryOutput = new MatchQueryOutput();
        int[] outputEdgeIds = {0, 2, 3, 1};
        matchQueryOutput.vertexIds = outputEdgeIds;
        Assert.assertFalse(predicate.test(matchQueryOutput));
    }
}
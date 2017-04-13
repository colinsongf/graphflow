package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.TestUtils;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.query.operator.EdgeIdResolver;
import ca.waterloo.dsg.graphflow.query.operator.Filter;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.operator.Projection;
import ca.waterloo.dsg.graphflow.query.operator.PropertyResolver;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests {@link OneTimeMatchQueryPlanner}
 */
public class OneTimeMatchQueryPlannerTest {

    @Before
    public void setUp() throws Exception {
        GraphDBState.reset();
        String createQuery = "CREATE (0:Person{name:string='name0', age:int=20, views:int=120})" +
            "-[:FOLLOWS{isRelated:boolean=true, views:int=100}]->(1:Person{name:string='name1', " +
            "age:int=10, views:int=50}),(0:Person)-[:LIKES]->(1:Person),(1:Person)-[:LIKES]->" +
            "(0:Person),(1:Person)-[:TAGGED]->(3:Person),(3:Person)-[:LIKES{rating:double=4.1, " +
            "views:int=300}]->(1:Person);";
        TestUtils.initializeGraphPermanentlyWithProperties(createQuery);
    }

    /**
     * Tests the creation of a {@link OneTimeMatchQueryPlan} for a simple triangle MATCH query with
     * no types.
     */
    @Test
    public void testPlanSimpleTriangleQuery() throws Exception {
        // Create a one time MATCH query plan for a simple triangle query with no types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)->(b),(b)->(c),(c)->(a)");
        OneTimeMatchQueryPlan actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(triangleStructuredQuery, null /* no outputSink */).plan();

        // Create the query plan manually. Ordering of the variables is "abc".
        OneTimeMatchQueryPlan expectedOneTimeMatchQueryPlan = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 0 extends "a" to "b".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 1 extends "ab" to "c".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(actualOneTimeMatchQueryPlan,
            expectedOneTimeMatchQueryPlan));
    }

    /**
     * Tests the creation of a {@link OneTimeMatchQueryPlan} for a triangle MATCH query with
     * specified types on relations.
     */
    @Test
    public void testPlanTriangleQueryWithRelationTypes() throws Exception {
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        short FOLLOWS_TYPE_ID = TypeAndPropertyKeyStore.getInstance().
            mapStringTypeToShortOrInsert("FOLLOWS");
        short LIKES_TYPE_ID = TypeAndPropertyKeyStore.getInstance().
            mapStringTypeToShortOrInsert("LIKES");
        // Create a one time MATCH query plan for a complex triangle query with multiple
        // relations between variable having different edge types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[:FOLLOWS]->(b),(a)-[:LIKES]->(b),(b)-[:LIKES]->(a),(b)->(c),(c)->(b)," +
            "(c)-[:FOLLOWS]->(a)");
        OneTimeMatchQueryPlan actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(triangleStructuredQuery, null /* no outputSink */).plan();

        // Create the query plan manually. Ordering of the variables is "bac".
        OneTimeMatchQueryPlan expectedOneTimeMatchQueryPlan = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 0 extends "b" to "a".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, FOLLOWS_TYPE_ID));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, LIKES_TYPE_ID));
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, LIKES_TYPE_ID));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 1 extends "ba" to "c".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, FOLLOWS_TYPE_ID));
        expectedOneTimeMatchQueryPlan.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(actualOneTimeMatchQueryPlan,
            expectedOneTimeMatchQueryPlan));
    }

    /**
     * Tests the creation of a {@link OneTimeMatchQueryPlan} for a complex MATCH query. The query
     * has been specially constructed to test all the ordering rules which are used to decide the
     * order of the query variables.
     */
    @Test
    public void testPlanComplexQuery() throws Exception {
        // Create a Generic Join query plan for a complex match query.
        StructuredQuery complexStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)->(b),(a)->(e),(c)->(b),(c)->(d),(d)->(b),(e)->(b),(f)->(c)");
        OneTimeMatchQueryPlan actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(complexStructuredQuery, null /* no outputSink */).plan();

        // Create the query plan manually. Ordering of the variables is "bcdaef".
        OneTimeMatchQueryPlan expectedOneTimeMatchQueryPlan = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 0 extends "b" to "c".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 1 extends "bc" to "bcd".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 2 extends "bcd" to "bcda".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 3 extends "bcda" to "bcdae".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(3, Direction.FORWARD, TypeAndPropertyKeyStore.ANY,
            TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 4 extends "bcdae" to "bcdaef".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD, TypeAndPropertyKeyStore.
            ANY, TypeAndPropertyKeyStore.ANY, TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(actualOneTimeMatchQueryPlan,
            expectedOneTimeMatchQueryPlan));
    }

    @Test
    public void testGetIdentityPropertyResolverAsNextOperator() {
        StructuredQuery structuredQueryWithoutReturn = new StructuredQueryParser().parse("MATCH " +
            "(a)-[:FOLLOWS]->(b), (b)-[:FOLLOWS]->(c), (c)-[:FOLLOWS]->(a)");
        OneTimeMatchQueryPlanner oneTimeMatchQueryPlanner = new OneTimeMatchQueryPlanner(
            structuredQueryWithoutReturn, new InMemoryOutputSink());
        String[] orderedVertexVariables = {"a", "b", "c"};
        AbstractDBOperator nextOperator = oneTimeMatchQueryPlanner.getNextOperator(Arrays.asList(
            orderedVertexVariables));
        Assert.assertTrue(nextOperator instanceof PropertyResolver);
        Assert.assertTrue(nextOperator.nextOperator instanceof InMemoryOutputSink);
    }

    @Test
    public void testGetNextOperatorForReturnWithVertices() {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[:FOLLOWS]->(b), (b)-[:FOLLOWS]->(c), (c)-[:FOLLOWS]->(a) RETURN a.name, b" +
            ".views, b.age, c");
        OneTimeMatchQueryPlanner oneTimeMatchQueryPlanner = new OneTimeMatchQueryPlanner(
            structuredQuery, new InMemoryOutputSink());
        String[] orderedVertexVariables = {"a", "b", "c"};
        AbstractDBOperator nextOperator = oneTimeMatchQueryPlanner.getNextOperator(Arrays.asList(
            orderedVertexVariables));
        Assert.assertTrue(nextOperator instanceof Projection);
        Assert.assertTrue(nextOperator.nextOperator instanceof PropertyResolver);
        Assert.assertTrue(nextOperator.nextOperator.nextOperator instanceof InMemoryOutputSink);
    }

    @Test
    public void testGetNextOperatorForReturnWithEdgesAndVertices() {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[d:FOLLOWS]->(b), (b)-[e:FOLLOWS]->(c), (c)-[f:FOLLOWS]->(a) RETURN a.name, b" +
            ".views, d.views, c, e");
        OneTimeMatchQueryPlanner oneTimeMatchQueryPlanner = new OneTimeMatchQueryPlanner(
            structuredQuery, new InMemoryOutputSink());
        String[] orderedVertexVariables = {"a", "b", "c"};
        AbstractDBOperator nextOperator = oneTimeMatchQueryPlanner.getNextOperator(Arrays.asList(
            orderedVertexVariables));
        Assert.assertTrue(nextOperator instanceof EdgeIdResolver);
        Assert.assertTrue(nextOperator.nextOperator instanceof Projection);
        Assert.assertTrue(nextOperator.nextOperator.nextOperator instanceof PropertyResolver);
        Assert.assertTrue(nextOperator.nextOperator.nextOperator.nextOperator instanceof
            InMemoryOutputSink);
    }

    @Test
    public void testGetNextOperatorForFiltersWithEdges() {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[d:FOLLOWS]->(b), (b)-[e:FOLLOWS]->(c), (c)-[f:FOLLOWS]->(a);");
        List<QueryPropertyPredicate> queryPropertyPredicates = new ArrayList<>();
        queryPropertyPredicates.add(TestUtils.createQueryPropertyPredicate(new Pair<>("a", "views"),
            new Pair<>("d", "views"), null /* no literal */, ComparisonOperator.GREATER_THAN));
        queryPropertyPredicates.add(TestUtils.createQueryPropertyPredicate(new Pair<>("d", "views"),
            new Pair<>("e", "views"), null /* no literal */, ComparisonOperator.EQUALS));
        structuredQuery.setQueryPropertyPredicates(queryPropertyPredicates);
        OneTimeMatchQueryPlanner oneTimeMatchQueryPlanner = new OneTimeMatchQueryPlanner(
            structuredQuery, new InMemoryOutputSink());
        String[] orderedVertexVariables = {"a", "b", "c"};
        AbstractDBOperator nextOperator = oneTimeMatchQueryPlanner.getNextOperator(Arrays.asList(
            orderedVertexVariables));
        Assert.assertTrue(nextOperator instanceof EdgeIdResolver);
        Assert.assertTrue(nextOperator.nextOperator instanceof Filter);
        Assert.assertTrue(nextOperator.nextOperator.nextOperator instanceof PropertyResolver);
        Assert.assertTrue(nextOperator.nextOperator.nextOperator.nextOperator instanceof
            InMemoryOutputSink);
    }

    @Test
    public void testGetNextOperationForQueryWithFilterAndReturn() {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[d:FOLLOWS]->(b), (b)-[e:FOLLOWS]->(c), (c)-[f:FOLLOWS]->(a) RETURN a.name, b" +
            ".views, d.views, c, e;");
        List<QueryPropertyPredicate> queryPropertyPredicates = new ArrayList<>();
        queryPropertyPredicates.add(TestUtils.createQueryPropertyPredicate(new Pair<>("a", "views"),
            new Pair<>("d", "views"), null /* no literal */, ComparisonOperator.GREATER_THAN));
        queryPropertyPredicates.add(TestUtils.createQueryPropertyPredicate(new Pair<>("d", "views"),
            new Pair<>("e", "views"), null /* no literal */, ComparisonOperator.EQUALS));
        structuredQuery.setQueryPropertyPredicates(queryPropertyPredicates);
        OneTimeMatchQueryPlanner oneTimeMatchQueryPlanner = new OneTimeMatchQueryPlanner(
            structuredQuery, new InMemoryOutputSink());
        String[] orderedVertexVariables = {"a", "b", "c"};
        AbstractDBOperator nextOperator = oneTimeMatchQueryPlanner.getNextOperator(Arrays.asList(
            orderedVertexVariables));
        Assert.assertTrue(nextOperator instanceof EdgeIdResolver);
        Assert.assertTrue(nextOperator.nextOperator instanceof Filter);
        Assert.assertTrue(nextOperator.nextOperator.nextOperator instanceof Projection);
        Assert.assertTrue(nextOperator.nextOperator.nextOperator.nextOperator instanceof
            PropertyResolver);
        Assert.assertTrue(nextOperator.nextOperator.nextOperator.nextOperator.nextOperator
            instanceof InMemoryOutputSink);
    }
}

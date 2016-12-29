package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests {@link OneTimeMatchQueryPlanner}
 */
public class OneTimeMatchQueryPlannerTest {

    /**
     * Tests the creation of a {@link OneTimeMatchQueryPlan} for a simple triangle MATCH query
     * with no types.
     */
    @Test
    public void testPlanSimpleTriangleQuery() throws Exception {
        // Create a one time MATCH query plan for a simple triangle query with no types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)->(b),(b)->(c),(c)->(a)");
        OneTimeMatchQueryPlan actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(triangleStructuredQuery).plan();

        // Create the query plan manually. Ordering of the variables is "abc".
        OneTimeMatchQueryPlan expectedOneTimeMatchQueryPlan = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 0 extends "a" to "b".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 1 extends "ab" to "c".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD));
        expectedOneTimeMatchQueryPlan.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(actualOneTimeMatchQueryPlan,
            expectedOneTimeMatchQueryPlan));
    }

    /**
     * Tests the creation of a {@link OneTimeMatchQueryPlan} for a triangle MATCH query
     * with specified types on relations.
     */
    @Test
    public void testPlanTriangleQueryWithRelationTypes() throws Exception {
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        short FOLLOWS_TYPE_ID = TypeAndPropertyKeyStore.getInstance().
            getTypeAsShortOrInsertIfDoesNotExist("FOLLOWS");
        short LIKES_TYPE_ID = TypeAndPropertyKeyStore.getInstance().
            getTypeAsShortOrInsertIfDoesNotExist("LIKES");
        // Create a one time MATCH query plan for a complex triangle query with multiple
        // relations between variable having different edge types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[:FOLLOWS]->(b),(a)-[:LIKES]->(b),(b)-[:LIKES]->(a),(b)->(c),(c)->(b)," +
            "(c)-[:FOLLOWS]->(a)");
        OneTimeMatchQueryPlan actualOneTimeMatchQueryPlan = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(triangleStructuredQuery).plan();

        // Create the query plan manually. Ordering of the variables is "bac".
        OneTimeMatchQueryPlan expectedOneTimeMatchQueryPlan = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 0 extends "b" to "a".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, FOLLOWS_TYPE_ID));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, LIKES_TYPE_ID));
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, LIKES_TYPE_ID));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 1 extends "ba" to "c".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD, FOLLOWS_TYPE_ID));
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
            new OneTimeMatchQueryPlanner(complexStructuredQuery).plan();

        // Create the query plan manually. Ordering of the variables is "bcdaef".
        OneTimeMatchQueryPlan expectedOneTimeMatchQueryPlan = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 0 extends "b" to "c".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 1 extends "bc" to "bcd".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 2 extends "bcd" to "bcda".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 3 extends "bcda" to "bcdae".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        stage.add(new GenericJoinIntersectionRule(3, Direction.FORWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);
        // Stage 4 extends "bcdae" to "bcdaef".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD,
                                                        TypeAndPropertyKeyStore.ANY));
        expectedOneTimeMatchQueryPlan.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(actualOneTimeMatchQueryPlan,
            expectedOneTimeMatchQueryPlan));
    }
}

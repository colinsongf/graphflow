package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariable;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OneTimeMatchQueryPlannerTest {

    @Test
    public void testPlanSimpleTriangleQuery() throws Exception {
        // Create a one time MATCH query plan for a simple triangle query with no types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)->(b),(b)->(c),(c)->(a)");
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanActual = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(triangleStructuredQuery).plan();

        // Create the query plan manually.
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanExpected = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD));
        oneTimeMatchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(oneTimeMatchQueryPlanActual,
            oneTimeMatchQueryPlanExpected));
    }

    @Test
    public void testPlanTriangleQuery() throws Exception {
        // Initialize the {@code TypeStore} with types used in the MATCH query.
        short FOLLOWS_TYPE_ID = TypeStore.getInstance().addNewTypeIfDoesNotExist("FOLLOWS");
        short LIKES_TYPE_ID = TypeStore.getInstance().addNewTypeIfDoesNotExist("LIKES");
        // Create a one time MATCH query plan for a complex triangle query with multiple
        // relations between variable having different edge types.
        StructuredQuery triangleStructuredQuery = new StructuredQueryParser().parse("MATCH " +
            "(a)-[:FOLLOWS]->(b),(a)-[:LIKES]->(b),(b)-[:LIKES]->(a),(b)->(c),(c)->(b)," +
            "(c)-[:FOLLOWS]->(a)");
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanActual = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(triangleStructuredQuery).plan();

        // Create the one time MATCH query plan manually. Ordering of the variables is "bac".
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanExpected = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 1 extends "b" to "a".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, FOLLOWS_TYPE_ID));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, LIKES_TYPE_ID));
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, LIKES_TYPE_ID));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 2 extends "ba" to "c".
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD, FOLLOWS_TYPE_ID));
        oneTimeMatchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(oneTimeMatchQueryPlanActual,
            oneTimeMatchQueryPlanExpected));
    }

    @Test
    public void testPlanComplexQuery() throws Exception {
        // Create a Generic Join query plan for a complex match query.
        StructuredQuery complexStructuredQuery = new StructuredQuery();
        complexStructuredQuery.addEdge(new QueryEdge(new QueryVariable("a"),
            new QueryVariable("b")));
        complexStructuredQuery.addEdge(new QueryEdge(new QueryVariable("a"),
            new QueryVariable("e")));
        complexStructuredQuery.addEdge(new QueryEdge(new QueryVariable("c"),
            new QueryVariable("b")));
        complexStructuredQuery.addEdge(new QueryEdge(new QueryVariable("c"),
            new QueryVariable("d")));
        complexStructuredQuery.addEdge(new QueryEdge(new QueryVariable("d"),
            new QueryVariable("b")));
        complexStructuredQuery.addEdge(new QueryEdge(new QueryVariable("e"),
            new QueryVariable("b")));
        complexStructuredQuery.addEdge(new QueryEdge(new QueryVariable("f"),
            new QueryVariable("c")));
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanActual = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(complexStructuredQuery).plan();

        // Create the query plan manually.
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanExpected = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 1
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeStore.ANY_TYPE));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 2
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, TypeStore.ANY_TYPE));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 3
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeStore.ANY_TYPE));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 4
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(3, Direction.FORWARD, TypeStore.ANY_TYPE));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 5
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD, TypeStore.ANY_TYPE));
        oneTimeMatchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(oneTimeMatchQueryPlanActual,
            oneTimeMatchQueryPlanExpected));
    }
}

package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariable;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OneTimeMatchQueryPlannerTest {

    private short defaultId = TypeStore.ANY_TYPE;

    @Test
    public void testPlanTriangleQuery() throws Exception {
        // Create a Generic Join query plan for a triangle query.
        StructuredQuery triangleStructuredQuery = new StructuredQuery();
        triangleStructuredQuery.addEdge(new QueryEdge(new QueryVariable("a"),
            new QueryVariable("b")));
        triangleStructuredQuery.addEdge(new QueryEdge(new QueryVariable("b"),
            new QueryVariable("c")));
        triangleStructuredQuery.addEdge(new QueryEdge(new QueryVariable("c"),
            new QueryVariable("a")));
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanActual = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(triangleStructuredQuery).plan();

        // Create the query plan manually.
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanExpected = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            defaultId));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            defaultId));
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
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            defaultId));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 2
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            defaultId));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 3
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            defaultId));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 4
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(3, Direction.FORWARD,
            defaultId));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        // Stage 5
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD,
            defaultId));
        oneTimeMatchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(OneTimeMatchQueryPlan.isSameAs(oneTimeMatchQueryPlanActual,
            oneTimeMatchQueryPlanExpected));
    }
}

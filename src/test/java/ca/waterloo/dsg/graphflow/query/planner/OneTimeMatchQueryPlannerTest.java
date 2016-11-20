package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OneTimeMatchQueryPlannerTest {

    @Test
    public void testPlanTriangleQuery() throws Exception {
        // Create a Generic Join query plan for a triangle query.
        StructuredQuery triangleStructuredQuery = new StructuredQuery();
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("b", "c"));
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("c", "a"));
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

        Assert.assertTrue(oneTimeMatchQueryPlanActual.isSameAs(oneTimeMatchQueryPlanExpected));
    }

    @Test
    public void testPlanComplexQuery() throws Exception {
        // Create a Generic Join query plan for a complex match query.
        StructuredQuery complexStructuredQuery = new StructuredQuery();
        complexStructuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("a", "e"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("c", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("c", "d"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("d", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("e", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("f", "c"));
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanActual = (OneTimeMatchQueryPlan)
            new OneTimeMatchQueryPlanner(complexStructuredQuery).plan();

        // Create the query plan manually.
        OneTimeMatchQueryPlan oneTimeMatchQueryPlanExpected = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD));
        stage.add(new GenericJoinIntersectionRule(3, Direction.FORWARD));
        oneTimeMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD));
        oneTimeMatchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(oneTimeMatchQueryPlanActual.isSameAs(oneTimeMatchQueryPlanExpected));
    }
}

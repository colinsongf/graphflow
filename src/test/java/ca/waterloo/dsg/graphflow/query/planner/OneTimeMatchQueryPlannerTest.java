package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graphmodel.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.GJMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class OneTimeMatchQueryPlannerTest {

    @Test
    public void testPlanTriangleQuery() throws Exception {
        StructuredQuery triangleStructuredQuery = new StructuredQuery();
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("b", "c"));
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("c", "a"));

        GJMatchQueryPlan gjMatchQueryPlanActual = (GJMatchQueryPlan) new OneTimeMatchQueryPlanner(
            triangleStructuredQuery).plan();

        GJMatchQueryPlan gjMatchQueryPlanExpected = new GJMatchQueryPlan();
        ArrayList<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.FORWARD));
        gjMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        gjMatchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(gjMatchQueryPlanActual.isSameAs(gjMatchQueryPlanExpected));
    }

    @Test
    public void testPlanComplexQuery() throws Exception {
        StructuredQuery complexStructuredQuery = new StructuredQuery();
        complexStructuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("a", "e"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("c", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("c", "d"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("d", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("e", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("f", "c"));

        GJMatchQueryPlan gjMatchQueryPlanActual = (GJMatchQueryPlan) new OneTimeMatchQueryPlanner(
            complexStructuredQuery).plan();

        GJMatchQueryPlan gjMatchQueryPlanExpected = new GJMatchQueryPlan();
        ArrayList<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        gjMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        gjMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        gjMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        stage.add(new GenericJoinIntersectionRule(3, EdgeDirection.FORWARD));
        gjMatchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.REVERSE));
        gjMatchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(gjMatchQueryPlanActual.isSameAs(gjMatchQueryPlanExpected));
    }
}

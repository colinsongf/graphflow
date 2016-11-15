package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.MatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class MatchQueryPlannerTest {

    @Test
    public void testPlanTriangleQuery() throws Exception {
        StructuredQuery triangleStructuredQuery = new StructuredQuery();
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("b", "c"));
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("c", "a"));

        MatchQueryPlan matchQueryPlanActual = (MatchQueryPlan) new MatchQueryPlanner(
            triangleStructuredQuery).plan();

        MatchQueryPlan matchQueryPlanExpected = new MatchQueryPlan();
        ArrayList<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, true));
        matchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        stage.add(new GenericJoinIntersectionRule(1, true));
        matchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(matchQueryPlanActual.isSameAs(matchQueryPlanExpected));
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

        MatchQueryPlan matchQueryPlanActual = (MatchQueryPlan) new MatchQueryPlanner(
            complexStructuredQuery).plan();

        MatchQueryPlan matchQueryPlanExpected = new MatchQueryPlan();
        ArrayList<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        matchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        stage.add(new GenericJoinIntersectionRule(1, true));
        matchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        matchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        stage.add(new GenericJoinIntersectionRule(3, true));
        matchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, false));
        matchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(matchQueryPlanActual.isSameAs(matchQueryPlanExpected));
    }
}

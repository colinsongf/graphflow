package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.MatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class MatchQueryPlannerTest {

    @Test
    public void planTriangleQuery() throws Exception {
        StructuredQuery triangleStructuredQuery = new StructuredQuery();
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("b", "c"));
        triangleStructuredQuery.addEdge(new StructuredQueryEdge("c", "a"));

        QueryPlan matchQueryPlanActual = new MatchQueryPlanner(triangleStructuredQuery).plan();

        MatchQueryPlan matchQueryPlanExpected = new MatchQueryPlan();
        matchQueryPlanExpected.addOrderedVariable("a");
        matchQueryPlanExpected.addOrderedVariable("b");
        matchQueryPlanExpected.addOrderedVariable("c");
        ArrayList<GenericJoinIntersectionRule> stage;
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, true));
        matchQueryPlanExpected.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        stage.add(new GenericJoinIntersectionRule(1, true));
        matchQueryPlanExpected.addStage(stage);

        Assert.assertTrue(matchQueryPlanActual.equalsTo(matchQueryPlanExpected));
    }

    @Test
    public void planComplexQuery() throws Exception {
        StructuredQuery complexStructuredQuery = new StructuredQuery();
        complexStructuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("a", "e"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("c", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("c", "d"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("d", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("e", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("f", "c"));

        QueryPlan matchQueryPlanActual = new MatchQueryPlanner(complexStructuredQuery).plan();

        MatchQueryPlan matchQueryPlanExpected = new MatchQueryPlan();
        matchQueryPlanExpected.addOrderedVariable("b");
        matchQueryPlanExpected.addOrderedVariable("c");
        matchQueryPlanExpected.addOrderedVariable("d");
        matchQueryPlanExpected.addOrderedVariable("a");
        matchQueryPlanExpected.addOrderedVariable("e");
        matchQueryPlanExpected.addOrderedVariable("f");
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

        Assert.assertTrue(matchQueryPlanActual.equalsTo(matchQueryPlanExpected));
    }
}

package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class MatchQueryPlanTest {

    MatchQueryPlan obj_equal_1 = new MatchQueryPlan();
    MatchQueryPlan obj_equal_2 = new MatchQueryPlan();
    MatchQueryPlan obj_different_1 = new MatchQueryPlan();
    MatchQueryPlan obj_different_2 = new MatchQueryPlan();

    @Before
    public void setup() {
        ArrayList<GenericJoinIntersectionRule> stage;

        obj_equal_1.addOrderedVariable("a");
        obj_equal_1.addOrderedVariable("b");
        obj_equal_1.addOrderedVariable("c");
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        obj_equal_1.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        stage.add(new GenericJoinIntersectionRule(1, true));
        obj_equal_1.addStage(stage);

        obj_equal_2.addOrderedVariable("a");
        obj_equal_2.addOrderedVariable("b");
        obj_equal_2.addOrderedVariable("c");
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        obj_equal_2.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        stage.add(new GenericJoinIntersectionRule(1, true));
        obj_equal_2.addStage(stage);

        obj_different_1.addOrderedVariable("a");
        obj_different_1.addOrderedVariable("b");
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        obj_different_1.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        stage.add(new GenericJoinIntersectionRule(1, true));
        obj_different_1.addStage(stage);

        obj_different_2.addOrderedVariable("a");
        obj_different_2.addOrderedVariable("b");
        obj_different_2.addOrderedVariable("c");
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        obj_different_2.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, false));
        obj_different_2.addStage(stage);
    }

    @Test
    public void equalObjects() throws Exception {
        Assert.assertTrue(obj_equal_1.equalsTo(obj_equal_2));
    }

    @Test
    public void notEqualObjects() throws Exception {
        Assert.assertFalse(obj_equal_1.equalsTo(obj_different_1));
        Assert.assertFalse(obj_equal_2.equalsTo(obj_different_1));
        Assert.assertFalse(obj_equal_2.equalsTo(obj_different_2));
        Assert.assertFalse(obj_equal_2.equalsTo(obj_different_2));
        Assert.assertFalse(obj_different_1.equalsTo(obj_different_2));
    }
}

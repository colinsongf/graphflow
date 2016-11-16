package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class OneTimeMatchQueryPlanTest {

    private OneTimeMatchQueryPlan obj_equal_1 = new OneTimeMatchQueryPlan();
    private OneTimeMatchQueryPlan obj_equal_2 = new OneTimeMatchQueryPlan();
    private OneTimeMatchQueryPlan obj_different_1 = new OneTimeMatchQueryPlan();
    private OneTimeMatchQueryPlan obj_different_2 = new OneTimeMatchQueryPlan();

    @Before
    public void setup() {
        ArrayList<GenericJoinIntersectionRule> stage;

        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.BACKWARD));
        obj_equal_1.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.BACKWARD));
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        obj_equal_1.addStage(stage);

        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.BACKWARD));
        obj_equal_2.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.BACKWARD));
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        obj_equal_2.addStage(stage);

        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.BACKWARD));
        obj_different_1.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.BACKWARD));
        stage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        obj_different_1.addStage(stage);

        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.BACKWARD));
        obj_different_2.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.BACKWARD));
        obj_different_2.addStage(stage);
    }

    @Test
    public void testEqualObjects() throws Exception {
        Assert.assertTrue(obj_equal_1.isSameAs(obj_equal_2));
    }

    @Test
    public void testNotEqualObjects() throws Exception {
        Assert.assertFalse(obj_equal_1.isSameAs(obj_different_1));
        Assert.assertFalse(obj_equal_2.isSameAs(obj_different_1));
        Assert.assertFalse(obj_equal_2.isSameAs(obj_different_2));
        Assert.assertFalse(obj_equal_2.isSameAs(obj_different_2));
        Assert.assertFalse(obj_different_1.isSameAs(obj_different_2));
    }
}

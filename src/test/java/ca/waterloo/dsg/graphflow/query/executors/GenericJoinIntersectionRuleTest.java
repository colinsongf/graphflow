package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import org.junit.Assert;
import org.junit.Test;

public class GenericJoinIntersectionRuleTest {

    private GenericJoinIntersectionRule genericJoinIntersectionRule1 = new
        GenericJoinIntersectionRule(0, Direction.BACKWARD);
    private GenericJoinIntersectionRule genericJoinIntersectionRule2 = new
        GenericJoinIntersectionRule(0, Direction.BACKWARD);
    private GenericJoinIntersectionRule genericJoinIntersectionRule3 = new
        GenericJoinIntersectionRule(1, Direction.FORWARD);

    @Test
    public void testIsSameAsTrue() throws Exception {
        Assert.assertTrue(genericJoinIntersectionRule1.isSameAs(genericJoinIntersectionRule2));
    }

    @Test
    public void testIsSameAsFalse() throws Exception {
        Assert.assertFalse(genericJoinIntersectionRule1.isSameAs(genericJoinIntersectionRule3));
        Assert.assertFalse(genericJoinIntersectionRule2.isSameAs(genericJoinIntersectionRule3));
    }
}

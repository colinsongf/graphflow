package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;

/**
 * Represents a generic join rule consisting of a prefix index and a direction. The rule will be
 * part of a Generic Join stage and will be used to find possible extensions (new outgoing or
 * incoming edge) to a given element ({@code prefixIndex}) of the intermediate result tuple prefix).
 */
public class GenericJoinIntersectionRule {
    private int prefixIndex;
    private Direction direction;
    private GraphVersion graphVersion;

    public GenericJoinIntersectionRule(int prefixIndex, Direction direction) {
        this(prefixIndex, direction, GraphVersion.PERMANENT);
    }

    public GenericJoinIntersectionRule(int prefixIndex, Direction direction,
        GraphVersion graphVersion) {
        this.prefixIndex = prefixIndex;
        this.direction = direction;
        this.graphVersion = graphVersion;
    }

    public int getPrefixIndex() {
        return prefixIndex;
    }

    public Direction getDirection() {
        return direction;
    }

    public GraphVersion getGraphVersion() {
        return graphVersion;
    }

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The expected object.
     *
     * @return {@code true} if the current object values match perfectly with the expected object
     * values, {@code false} otherwise.
     */
    public boolean isSameAs(GenericJoinIntersectionRule that) {
        if (null == that) {
            return false;
        }
        if (this == that) {
            return true;
        }
        return (this.prefixIndex == that.prefixIndex && this.direction == that.direction
            && this.graphVersion == that.graphVersion);
    }
}

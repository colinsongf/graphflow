package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;

/**
 * Represents a generic join rule consisting of a prefix index and a direction. The rule will be
 * part of a Generic Join stage and will be used to find possible extensions (new outgoing or
 * incoming edge) to a given element ({@code prefixIndex}) of the intermediate result tuple prefix).
 */
public class GenericJoinIntersectionRule {
    private int prefixIndex;
    private EdgeDirection edgeDirection;
    private GraphVersion graphVersion;

    public GenericJoinIntersectionRule(int prefixIndex, EdgeDirection edgeDirection) {
        this(prefixIndex, edgeDirection, GraphVersion.CURRENT);
    }

    public GenericJoinIntersectionRule(int prefixIndex, EdgeDirection edgeDirection,
        GraphVersion graphVersion) {
        this.prefixIndex = prefixIndex;
        this.edgeDirection = edgeDirection;
        this.graphVersion = graphVersion;
    }

    public int getPrefixIndex() {
        return prefixIndex;
    }

    public EdgeDirection getEdgeDirection() {
        return edgeDirection;
    }

    public GraphVersion getGraphVersion() {
        return graphVersion;
    }

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The expected object.
     * @return {@code true} if the current object values match perfectly with the expected object
     * values, {@code false} otherwise.
     */
    public boolean isSameAs(GenericJoinIntersectionRule that) {
        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }
        return (this.prefixIndex == that.prefixIndex && this.edgeDirection == that.edgeDirection &&
            this.graphVersion == that.graphVersion);
    }
}

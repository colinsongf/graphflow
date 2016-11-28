package ca.waterloo.dsg.graphflow.query.utils;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

import java.util.Objects;

/**
 * Represents a query edge used by {@link StructuredQuery}.
 */
public class QueryEdge implements AbstractStructuredQuery {

    private QueryVariable fromQueryVariable;
    private QueryVariable toQueryVariable;
    private Direction direction;
    private String edgeType;

    /**
     * Constructs a {@code QueryEdge} with the direction set to {@link Direction#FORWARD}.
     *
     * @see QueryEdge#QueryEdge(QueryVariable, QueryVariable, Direction)
     */
    public QueryEdge(QueryVariable fromQueryVariable, QueryVariable toQueryVariable) {
        this(fromQueryVariable, toQueryVariable, Direction.FORWARD);
    }

    /**
     * @param fromQueryVariable The source variable of the edge.
     * @param toQueryVariable The destination variable of the edge.
     * @param direction the direction of the edge.
     */
    public QueryEdge(QueryVariable fromQueryVariable, QueryVariable toQueryVariable,
        Direction direction) {
        this.fromQueryVariable = fromQueryVariable;
        this.toQueryVariable = toQueryVariable;
        this.direction = direction;
    }

    public QueryVariable getFromQueryVariable() {
        return fromQueryVariable;
    }

    public QueryVariable getToQueryVariable() {
        return toQueryVariable;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a The actual object.
     * @param b The expected object.
     * @return {@code true} if the {@code a} object values are the same as the
     * {@code expected} object values, {@code false} otherwise.
     */
    @ExistsForTesting
    public static boolean isSameAs(QueryEdge a, QueryEdge b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return QueryVariable.isSameAs(a.fromQueryVariable, b.fromQueryVariable) &&
            QueryVariable.isSameAs(a.toQueryVariable, b.toQueryVariable) &&
            a.direction == b.direction &&
            Objects.equals(a.edgeType, b.edgeType);
    }
}

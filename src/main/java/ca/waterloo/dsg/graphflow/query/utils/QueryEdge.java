package ca.waterloo.dsg.graphflow.query.utils;

import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

import java.util.Objects;

/**
 * Represents a query edge used by {@link StructuredQuery}.
 */
public class QueryEdge implements AbstractStructuredQuery {

    private QueryVariable fromQueryVariable;
    private QueryVariable toQueryVariable;
    private String edgeType;

    /**
     * Constructs a {@code QueryEdge} with the edge type set to {@code null}.
     *
     * @see QueryEdge#QueryEdge(QueryVariable, QueryVariable, String)
     */
    public QueryEdge(QueryVariable fromQueryVariable, QueryVariable toQueryVariable) {
        this(fromQueryVariable, toQueryVariable, null);
    }

    /**
     * Constructs a {@link QueryEdge}. The direction of the {@link QueryEdge} is implicit from
     * {@code fromQueryVariable} to {@code toQueryVariable}.
     *
     * @param fromQueryVariable The from variable of the edge.
     * @param toQueryVariable The to variable of the edge.
     * @param edgeType the {@code String} edge type.
     */
    public QueryEdge(QueryVariable fromQueryVariable, QueryVariable toQueryVariable,
        String edgeType) {
        this.fromQueryVariable = fromQueryVariable;
        this.toQueryVariable = toQueryVariable;
        this.edgeType = edgeType;
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

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     * @return {@code true} if the {@code a} object values are the same as the
     * {@code b} object values, {@code false} otherwise.
     */
    @ExistsForTesting
    public static boolean isSameAs(QueryEdge a, QueryEdge b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        return QueryVariable.isSameAs(a.fromQueryVariable, b.fromQueryVariable) &&
            QueryVariable.isSameAs(a.toQueryVariable, b.toQueryVariable) &&
            Objects.equals(a.edgeType, b.edgeType);
    }
}

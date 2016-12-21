package ca.waterloo.dsg.graphflow.query.utils;

import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

import java.util.Objects;

/**
 * Represents a query relation used by {@link StructuredQuery}.
 */
public class QueryRelation implements AbstractStructuredQuery {

    private QueryVariable fromQueryVariable;
    private QueryVariable toQueryVariable;
    private String relationType;

    /**
     * Constructs a {@code QueryRelation} with the relation type set to {@code null}.
     *
     * @see QueryRelation#QueryRelation(QueryVariable, QueryVariable, String)
     */
    public QueryRelation(QueryVariable fromQueryVariable, QueryVariable toQueryVariable) {
        this(fromQueryVariable, toQueryVariable, null);
    }

    /**
     * Constructs a {@link QueryRelation}. The direction of the {@link QueryRelation} is implicit
     * from {@code fromQueryVariable} to {@code toQueryVariable}.
     *
     * @param fromQueryVariable The from variable of the relation.
     * @param toQueryVariable The to variable of the relation.
     * @param relationType the {@code String} relation type.
     */
    public QueryRelation(QueryVariable fromQueryVariable, QueryVariable toQueryVariable,
        String relationType) {
        this.fromQueryVariable = fromQueryVariable;
        this.toQueryVariable = toQueryVariable;
        this.relationType = relationType;
    }

    public QueryVariable getFromQueryVariable() {
        return fromQueryVariable;
    }

    public QueryVariable getToQueryVariable() {
        return toQueryVariable;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
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
    public static boolean isSameAs(QueryRelation a, QueryRelation b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        return QueryVariable.isSameAs(a.fromQueryVariable, b.fromQueryVariable) &&
            QueryVariable.isSameAs(a.toQueryVariable, b.toQueryVariable) &&
            Objects.equals(a.relationType, b.relationType);
    }
}

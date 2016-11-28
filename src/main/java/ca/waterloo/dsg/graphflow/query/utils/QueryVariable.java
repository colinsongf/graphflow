package ca.waterloo.dsg.graphflow.query.utils;

import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

import java.util.Objects;

/**
 * Represents a query variable used by {@link QueryEdge}.
 */
public class QueryVariable implements AbstractStructuredQuery {

    private String variableId;
    private String variableType;

    /**
     * Constructs a {@code QueryVariable}.
     *
     * @param variableId The {@code String} vertex variable.
     */
    public QueryVariable(String variableId) {
        this.variableId = variableId;
    }

    public String getVariableId() {
        return variableId;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a The actual object.
     * @param b The expected object.
     * @return {@code true} if the {@code actual} object values are the same as the
     * {@code expected} object values, {@code false} otherwise.
     */
    @ExistsForTesting
    public static boolean isSameAs(QueryVariable a, QueryVariable b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.variableId, b.variableId) &&
            Objects.equals(a.variableType, b.variableType);
    }
}

package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a query variable used by {@link QueryRelation}.
 */
public class QueryVariable implements AbstractStructuredQuery {

    private String variableId;
    private String variableType;
    private HashMap<String, String[]> variableProperties;

    /**
     * Constructs a {@code QueryVariable} with the variable type set to {@code null}.
     *
     * @param variableId The {@code String} vertex variable.
     */
    public QueryVariable(String variableId) {
        this.variableId = variableId;
    }

    /**
     * Constructs a {@code QueryVariable}.
     *
     * @param variableId The {@code String} vertex variable.
     * @param variableType The {@code String} type of the variable.
     */
    public QueryVariable(String variableId, String variableType) {
        this.variableId = variableId;
        this.variableType = variableType;
    }

    public String getVariableId() {
        return variableId;
    }

    public String getVariableType() {
        return variableType;
    }

    public HashMap<String, String[]> getVariableProperties() {
        return variableProperties;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public void setVariableProperties(HashMap<String, String[]> variableProperties) {
        this.variableProperties = variableProperties;
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
    public static boolean isSameAs(QueryVariable a, QueryVariable b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        return Objects.equals(a.variableId, b.variableId) &&
            Objects.equals(a.variableType, b.variableType);
    }
}

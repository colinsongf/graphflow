package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a query variable used by {@link QueryRelation}.
 */
public class QueryVariable implements AbstractStructuredQuery {

    private String variableId;
    private String variableType;
    // The Strings below refer to: Map<key, Pair<dataType, value>>
    private Map<String, Pair<String, String>> variableProperties;

    /**
     * Constructs a {@code QueryVariable} with the variable type and properties set to {@code null}.
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

    public Map<String, Pair<String, String>> getVariableProperties() {
        return variableProperties;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public void setVariableProperties(Map<String, Pair<String, String>> variableProperties) {
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
    @UsedOnlyByTests
    public static boolean isSameAs(QueryVariable a, QueryVariable b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (!Objects.equals(a.variableId, b.variableId) ||
            !Objects.equals(a.variableType, b.variableType)) {
            return false;
        }

        if (null == a.variableProperties && null == b.variableProperties) {
            return true;
        } else if ((null != a.variableProperties && null == b.variableProperties) ||
            (null == a.variableProperties) || (a.variableProperties.size() != b.variableProperties.
            size())) {
            return false;
        }
        for (String key : a.variableProperties.keySet()) {
            if (!a.variableProperties.get(key).b.equals((b.variableProperties.get(key).b)) ||
                !a.variableProperties.get(key).a.toUpperCase().equals((b.variableProperties.get(
                    key).a.toUpperCase()))) {
                return false;
            }
        }

        return true;
    }
}

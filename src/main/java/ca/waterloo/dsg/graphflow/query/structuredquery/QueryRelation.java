package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a query relation used by {@link StructuredQuery}.
 */
public class QueryRelation implements AbstractStructuredQuery {

    private QueryVariable fromQueryVariable;
    private QueryVariable toQueryVariable;
    // Variable name, e.g., e1, e2, given to edges in the query. For example:
    // MATCH (a)-[e1:LIKES]->(b);
    private String relationName;
    private String relationType;
    // The Strings below refer to: Map<key, Pair<dataType, value>>
    private Map<String, Pair<String, String>> relationProperties;

    /**
     * Constructs a {@code QueryRelation} with the relation type and properties set to {@code null}.
     * The direction of the {@link QueryRelation} is from {@code fromQueryVariable} to {@code
     * toQueryVariable}.
     *
     * @param fromQueryVariable The from variable of the relation.
     * @param toQueryVariable The to variable of the relation.
     */
    public QueryRelation(QueryVariable fromQueryVariable, QueryVariable toQueryVariable) {
        this.fromQueryVariable = fromQueryVariable;
        this.toQueryVariable = toQueryVariable;
    }

    public QueryVariable getFromQueryVariable() {
        return fromQueryVariable;
    }

    public QueryVariable getToQueryVariable() {
        return toQueryVariable;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public Map<String, Pair<String, String>> getRelationProperties() {
        return relationProperties;
    }

    public void setRelationProperties(Map<String, Pair<String, String>> properties) {
        this.relationProperties = properties;
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     *
     * @return {@code true} if the {@code a} object values are the same as the {@code b} object
     * values, {@code false} otherwise.
     */
    @UsedOnlyByTests
    public static boolean isSameAs(QueryRelation a, QueryRelation b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (!QueryVariable.isSameAs(a.fromQueryVariable, b.fromQueryVariable) ||
            !QueryVariable.isSameAs(a.toQueryVariable, b.toQueryVariable) ||
            !Objects.equals(a.relationType, b.relationType)) {
            return false;
        }

        if (null == a.relationProperties && null == b.relationProperties) {
            return true;
        } else if ((null != a.relationProperties && null == b.relationProperties) ||
            (null == a.relationProperties) || (a.relationProperties.size() != b.relationProperties.
            size())) {
            return false;
        }
        for (String key : a.relationProperties.keySet()) {
            if (!a.relationProperties.get(key).b.equals((b.relationProperties.get(key).b)) ||
                !a.relationProperties.get(key).a.toUpperCase().equals((b.relationProperties.get(
                    key).a.toUpperCase()))) {
                return false;
            }
        }

        return true;
    }
}

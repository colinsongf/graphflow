package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.util.PropertyComparator.ComparisonOperator;
import org.antlr.v4.runtime.misc.Pair;

/**
 * Represents a property filter.
 */
public class QueryPropertyPredicate {

    public enum PredicateType {
        TWO_VERTEX,
        TWO_EDGE,
        EDGE_AND_CONSTANT,
        VERTEX_AND_CONSTANT,
        EDGE_AND_VERTEX
    }

    private Pair<String, Short> variable1;
    private Pair<String, Short> variable2;
    private String constant;
    private ComparisonOperator comparisonOperator;
    private PredicateType predicateType;

    public Pair<String, Short> getVariable1() {
        return variable1;
    }

    public Pair<String, Short> getVariable2() {
        return variable2;
    }

    public String getConstant() {
        return constant;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public PredicateType getPredicateType() {
        return predicateType;
    }

    public Object resolveConstant(String constant) {
        return null;
    }
}

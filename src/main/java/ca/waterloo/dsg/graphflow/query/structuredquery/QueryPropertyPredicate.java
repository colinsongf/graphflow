package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.util.PropertyComparator.ComparisonOperator;

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

    private String variable1;
    private short propertyOfVariable1;
    private String variable2;
    private short propertyOfVariable2;
    private String constant;
    private ComparisonOperator comparisonOperator;
    private PredicateType predicateType;

    public short getPropertyOfVariable1() {
        return propertyOfVariable1;
    }

    public String getVariable1() {
        return variable1;
    }

    public String getVariable2() {
        return variable2;
    }

    public short getPropertyOfVariable2() {
        return propertyOfVariable2;
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

package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
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
        DataType dataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(variable1.b);
        return DataType.parseDataType(dataType, constant);
    }

    public void setVariable1(Pair<String, Short> variable1) {
        this.variable1 = variable1;
    }

    public void setVariable2(Pair<String, Short> variable2) {
        this.variable2 = variable2;
    }

    public void setConstant(String constant) {
        this.constant = constant;
    }

    public void setComparisonOperator(ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    public void setPredicateType(PredicateType predicateType) {
        this.predicateType = predicateType;
    }
}

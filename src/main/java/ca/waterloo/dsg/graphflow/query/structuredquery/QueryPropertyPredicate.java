package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.antlr.v4.runtime.misc.Pair;

import static ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate.OperandType
    .CONSTANT;
import static ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate.OperandType.EDGE;
import static ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate.OperandType.VERTEX;

import java.util.function.Predicate;

/**
 * A class representing a filter on the results of a MATCH query. Consists of two operands (a, b)
 * and a comparison operator(*) where the operands are variables used in specifying the subgraph
 * pattern of the MATCH query. The results of the MATCH query will be forced to satisfy an
 * {@link Predicate} encapsulating the comparison operation (a * b).
 */
public class QueryPropertyPredicate {

    /**
     * An enum whose constants specify types of comparison predicates based on the types
     * of the operand variables of comparison. The types of an operand variable is determined by
     * whether they represent a vertex, or an edge in a MATCH query, or a constant.
     */
    public enum PredicateType {
        TWO_VERTEX,
        TWO_EDGE,
        EDGE_AND_VERTEX,
        VERTEX_AND_EDGE,
        EDGE_AND_CONSTANT,
        VERTEX_AND_CONSTANT
    }

    public enum OperandType {
        EDGE,
        VERTEX,
        CONSTANT
    }

    public QueryPropertyPredicate() { }

    public QueryPropertyPredicate(Pair<String, String> variable1, String constant,
        ComparisonOperator comparisonOperator, PredicateType predicateType) {
        this.variable1 = variable1;
        this.constant = constant;
        this.comparisonOperator = comparisonOperator;
        this.predicateType = predicateType;
    }

    private Pair<String, String> variable1;
    private Pair<String, String> variable2;
    private String constant;
    private ComparisonOperator comparisonOperator;
    private PredicateType predicateType;

    public Pair<String, String> getVariable1() {
        return variable1;
    }

    public Pair<String, String> getVariable2() {
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

    public void setVariable1(Pair<String, String> variable1) {
        this.variable1 = variable1;
    }

    public void setVariable2(Pair<String, String> variable2) {
        this.variable2 = variable2;
    }

    public void setConstant(String constant) {
        this.constant = constant;
    }

    public void setComparisonOperator(ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    public void setPredicateType(OperandType leftOperandType, OperandType rightOperandType) {
        if (leftOperandType == VERTEX && rightOperandType == VERTEX) {
            this.predicateType = PredicateType.TWO_VERTEX;
        } else if (leftOperandType == EDGE && rightOperandType == EDGE) {
            this.predicateType = PredicateType.TWO_EDGE;
        } else if (leftOperandType == VERTEX && rightOperandType == EDGE) {
            this.predicateType = PredicateType.VERTEX_AND_EDGE;
        } else if (leftOperandType == EDGE && rightOperandType == VERTEX) {
            this.predicateType = PredicateType.EDGE_AND_VERTEX;
        } else if (leftOperandType == VERTEX && rightOperandType == CONSTANT) {
            this.predicateType = PredicateType.VERTEX_AND_CONSTANT;
        } else if (leftOperandType == EDGE && rightOperandType == CONSTANT) {
            this.predicateType = PredicateType.EDGE_AND_CONSTANT;
        }
    }
}

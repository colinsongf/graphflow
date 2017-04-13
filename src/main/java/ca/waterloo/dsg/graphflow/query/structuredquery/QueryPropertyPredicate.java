package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.antlr.v4.runtime.misc.Pair;

import java.util.function.Predicate;

/**
 * A class representing a filter on the results of a MATCH query. Consists of two operands (a, b)
 * and a comparison operator(op) where the operands are variables used in specifying the subgraph
 * pattern of the MATCH query. The results of the MATCH query will be forced to satisfy a
 * {@link Predicate} encapsulating the predicate (a op b).
 */
public class QueryPropertyPredicate {

    /**
     * An enum whose constants specify types of predicates based on the types of the operand
     * variables of comparison. The types of an operand variable is determined by whether they
     * represent a vertex, or an edge in a MATCH query, or a constant.
     */
    public enum PredicateType {
        TWO_VARIABLES,
        VARIABLE_AND_LITERAL
    }

    public enum OperandType {
        VARIABLE,
        LITERAL
    }

    private Pair<String, String> variable1;
    private Pair<String, String> variable2;
    private String literal;
    private ComparisonOperator comparisonOperator;
    private PredicateType predicateType;

    public QueryPropertyPredicate() { }

    public Pair<String, String> getVariable1() {
        return variable1;
    }

    public void setVariable1(Pair<String, String> variable1) {
        this.variable1 = variable1;
    }

    public Pair<String, String> getVariable2() {
        return variable2;
    }

    public void setVariable2(Pair<String, String> variable2) {
        this.variable2 = variable2;
    }

    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String constant) {
        this.literal = constant;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }

    public PredicateType getPredicateType() {
        return predicateType;
    }

    public void setPredicateType(PredicateType predicateType) {
        this.predicateType = predicateType;
    }

    public void invertComparisonOperator() {
        switch (comparisonOperator) {
            case GREATER_THAN:
                comparisonOperator = ComparisonOperator.LESS_THAN;
                break;
            case LESS_THAN:
                comparisonOperator = ComparisonOperator.GREATER_THAN;
                break;
            case GREATER_THAN_OR_EQUAL:
                comparisonOperator = ComparisonOperator.LESS_THAN_OR_EQUAL;
                break;
            case LESS_THAN_OR_EQUAL:
                comparisonOperator = ComparisonOperator.GREATER_THAN_OR_EQUAL;
                break;
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder()
            .append("{ " + variable1.a + "." + variable1.b)
            .append(" " + comparisonOperator.name() + " ")
            .append(" ");
        if (variable2 != null) {
            stringBuilder.append(variable2.a + "." + variable2.b + "}");
        } else {
            stringBuilder.append(literal + "}");
        }
        return stringBuilder.toString();
    }
}

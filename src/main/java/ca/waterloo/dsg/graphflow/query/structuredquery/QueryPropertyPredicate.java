package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.query.output.JsonOutputable;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.misc.Pair;

import java.util.function.Predicate;

/**
 * A class representing a filter on the results of a MATCH query. Consists of two operands (a, b)
 * and a comparison operator(op) where the operands are a literal or alternatively a relation or
 * variable name used in the MATCH query followed by a property key. The results of the MATCH
 * query satisfy a {@link Predicate} encapsulating the predicate (a op b).
 */
public class QueryPropertyPredicate implements JsonOutputable {

    /**
     * An enum whose constants specify types of predicates based on the types of the operands.
     * The types of an operand is determined by whether they represent a vertex, or an
     * edge name in a MATCH query, or a literal.
     */
    public enum PredicateType {
        TWO_PROPERTY_KEY_OPERANDS,
        PROPERTY_KEY_AND_LITERAL_OPERANDS
    }

    private Pair<String, String> leftOperand;
    private Pair<String, String> rightOperand;
    private String literal;
    private ComparisonOperator comparisonOperator;
    private PredicateType predicateType;

    public QueryPropertyPredicate() { }

    public Pair<String, String> getLeftOperand() {
        return leftOperand;
    }

    public void setLeftOperand(Pair<String, String> leftOperand) {
        this.leftOperand = leftOperand;
    }

    public Pair<String, String> getRightOperand() {
        return rightOperand;
    }

    public void setRightOperand(Pair<String, String> rightOperand) {
        this.rightOperand = rightOperand;
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
            .append("{ " + leftOperand.a + "." + leftOperand.b)
            .append(" " + comparisonOperator.name() + " ")
            .append(" ");
        if (rightOperand != null) {
            stringBuilder.append(rightOperand.a + "." + rightOperand.b + "}");
        } else {
            stringBuilder.append(literal + "}");
        }
        return stringBuilder.toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonPropertyPredicate = new JsonObject();
        jsonPropertyPredicate.addProperty("Predicate", toString());
        return jsonPropertyPredicate;
    }
}

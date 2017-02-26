package ca.waterloo.dsg.graphflow.util;

import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;

import java.util.function.Predicate;

public class RuntimeTypeBasedComparator {

    public enum ComparisonOperator {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        GREATER_THAN,
        LESS_THAN_OR_EQUAL,
        GREATER_THAN_EQUAL
    }

    public static boolean resolveTypesAndCompare(Object operand1, Object operand2,
        ComparisonOperator comparisonOperator) {

        if (operand1 instanceof Boolean && operand2 instanceof Boolean) {
            return compare((Boolean) operand1, (Boolean) operand2,
                comparisonOperator);
        } else if (operand1 instanceof String && operand2 instanceof String) {
            return compare((String) operand1, (String) operand2,
                comparisonOperator);
        } else if (operand1 instanceof Double && operand2 instanceof Integer) {
            return compare((Double) operand1, new Double(((Integer)
                operand2).intValue()), comparisonOperator);
        } else if (operand1 instanceof Integer && operand2 instanceof Double) {
            return compare(new Double(((Integer) operand1).intValue()),
                (Double) operand2, comparisonOperator);
        } else if (operand1 instanceof Double && operand2 instanceof Double) {
            return compare((Double) operand1, (Double) operand2,
                comparisonOperator);
        } else if (operand1 instanceof Integer && operand2 instanceof Integer) {
            return compare((Integer) operand1, (Integer) operand2,
                comparisonOperator);
        } else {
            throw new IllegalArgumentException("The given objects of type " + operand1.getClass() +
                " and " + operand2.getClass() + " cannot be compared.");
        }
    }

    public static <T extends Comparable> boolean compare(T operand1, T operand2,
        ComparisonOperator
            comparisonOperator) {
        int result = operand1.compareTo(operand2);
        if (result > 0 && (comparisonOperator == ComparisonOperator.GREATER_THAN ||
            comparisonOperator == ComparisonOperator.GREATER_THAN_EQUAL || comparisonOperator ==
            ComparisonOperator.NOT_EQUALS)) {
            return true;
        } else if (result == 0 && (comparisonOperator == ComparisonOperator.EQUALS ||
            comparisonOperator == ComparisonOperator.GREATER_THAN_EQUAL || comparisonOperator ==
            ComparisonOperator.LESS_THAN_OR_EQUAL)) {
            return true;
        } else if (result < 0 && (comparisonOperator == ComparisonOperator.LESS_THAN ||
            comparisonOperator == comparisonOperator.LESS_THAN_OR_EQUAL || comparisonOperator ==
            ComparisonOperator.NOT_EQUALS)) {
            return true;
        }
        return false;
    }
}

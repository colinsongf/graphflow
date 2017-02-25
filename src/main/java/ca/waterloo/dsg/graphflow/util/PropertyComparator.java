package ca.waterloo.dsg.graphflow.util;

import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;

import java.util.function.Predicate;

public class PropertyComparator {

    public enum ComparisonOperator {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        GREATER_THAN,
        LESS_THAN_OR_EQUAL,
        GREATER_THAN_EQUAL
    }

    public static <T extends Comparable> boolean compare(T operand1, T operand2,
        ComparisonOperator
        comparisonOperator) {
        int result = operand1.compareTo(operand2);
        if (result > 0 && (comparisonOperator == ComparisonOperator.GREATER_THAN ||
            comparisonOperator == ComparisonOperator.GREATER_THAN_EQUAL)) {
            return true;
        } else if(result == 0 && (comparisonOperator == ComparisonOperator.EQUALS)) {
            return true;
        } else if(result < 0 && (comparisonOperator == ComparisonOperator.LESS_THAN ||
            comparisonOperator == comparisonOperator.LESS_THAN_OR_EQUAL)) {
            return true;
        }
        return false;
    }

}

package ca.waterloo.dsg.graphflow.util;

public class RuntimeTypeBasedComparator {

    /**
     * An enum representing comparison operators that can be used to determine the operator for a
     * given comparison at runtime. The constants specified in the enum map to the java comparison
     * operators implied by their names.
     */
    public enum ComparisonOperator {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        GREATER_THAN,
        LESS_THAN_OR_EQUAL,
        GREATER_THAN_OR_EQUAL;

        public static ComparisonOperator mapStringToComparisonOperator(String comparisonOperator) {
            if ("=".equals(comparisonOperator)) {
                return ComparisonOperator.EQUALS;
            } else if ("<>".equals(comparisonOperator)) {
                return ComparisonOperator.NOT_EQUALS;
            } else if ("<".equals(comparisonOperator)) {
                return ComparisonOperator.LESS_THAN;
            } else if (">".equals(comparisonOperator)) {
                return ComparisonOperator.GREATER_THAN;
            } else if ("<=".equals(comparisonOperator)) {
                return ComparisonOperator.LESS_THAN_OR_EQUAL;
            } else if (">=".equals(comparisonOperator)) {
                return ComparisonOperator.GREATER_THAN_OR_EQUAL;
            }

            throw new IllegalArgumentException("The comparison operator " + comparisonOperator +
                " is not supported.");
        }
    }

    /**
     * Takes in two {@link Object} operands and a {@link ComparisonOperator}, and performs the
     * comparison on specified by the {@link ComparisonOperator} on the operands. Returns the
     * {@code boolean} result of the comparison if the runtime types of the operands are comparable.
     * If they are not comparable, throws an {@link IllegalArgumentException}.
     * eg 1: Runtime types Integer and Double are comparable.
     * eg 2: Runtime types String and Integer are not comparable.
     *
     * @param operand1 an {@link Object} operand for {@code comparisonOperator} with a runtime
     * type of {@link Integer}, {@link Double}, {@link Boolean} or {@link String}.
     * @param operand2 an {@link Object} operand for {@code comparisonOperator} with a runtime
     * type of {@link Integer}, {@link Double}, {@link Boolean} or {@link String}.
     * @param comparisonOperator an {@link ComparisonOperator} that represents the comparison
     * operator to be performed on {@code operand1} and {@code operand2}.
     * @return the {@code boolean} which results by performing the comparison
     * {@code comparisonOperator} on the operands {@code operand1} and {@code operand2}.
     * @throws IllegalArgumentException if the runtime types of the operands are not comparable.
     */
    public static boolean resolveTypesAndCompare(Object operand1, Object operand2,
        ComparisonOperator comparisonOperator) {
        try {
            if (operand1 instanceof Boolean) {
                return compare((Boolean) operand1, (Boolean) operand2, comparisonOperator);
            } else if (operand1 instanceof String) {
                return compare((String) operand1, (String) operand2, comparisonOperator);
            } else if (operand1 instanceof Double) {
                return compare((Double) operand1, (Double) operand2, comparisonOperator);
            } else { // (operand1 instanceof Integer)
                return compare((Integer) operand1, (Integer) operand2, comparisonOperator);
            }
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("The given objects of type " + operand1.getClass() +
                " and " + operand2.getClass() + " cannot be compared.");
        }
    }

    private static <T extends Comparable<T>> boolean compare(T operand1, T operand2,
        ComparisonOperator comparisonOperator) {
        int result = operand1.compareTo(operand2);
        switch (comparisonOperator) {
            case EQUALS:
                return result == 0;
            case NOT_EQUALS:
                return result != 0;
            case LESS_THAN:
                return result < 0;
            case GREATER_THAN:
                return result > 0;
            case LESS_THAN_OR_EQUAL:
                return result <= 0;
            case GREATER_THAN_OR_EQUAL:
                return result >= 0;
            default:
                throw new IllegalArgumentException("The comparison operator " + comparisonOperator +
                    " is not supported.");
        }
    }
}

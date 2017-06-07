package ca.waterloo.dsg.graphflow.query.operator.filter;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.EdgeOrVertexPropertyDescriptor;
import ca.waterloo.dsg.graphflow.query.operator.Filter;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;

import java.util.function.Predicate;

/**
 * Contains static methods for creating {@link Predicate} objects that are used by {@link Filter}.
 */
public class FilterPredicateFactory {

    /**
     * Returns a {@link Predicate} which performs the comparison specified in the given
     * {@code queryPropertyPredicate}. Uses the {@link QueryPropertyPredicate#comparisonOperator}
     * to determine the operands and the operator of the returned {@link Predicate}.
     *
     * @param queryPropertyPredicate the {@link QueryPropertyPredicate} which contains operand,
     * operator and type information for creating the {@link Predicate} that is returned.
     * @param leftOperandIndexInPropertyResults the index of the left operand property in the
     * property result set created by the {@link Filter} operator. {@link Filter} uses its {@link
     * EdgeOrVertexPropertyDescriptor} list to create the property result set from the {@link
     * MatchQueryOutput}.
     * @param rightOperandIndexInPropertyResults the index of the right operand property in the
     * property result set created by the {@link Filter} operator. {@link Filter} uses its {@link
     * EdgeOrVertexPropertyDescriptor} list to create the property result set from the {@link
     * MatchQueryOutput}.
     *
     * @return a {@link Predicate<String[]>} instance that will perform the comparison specified in
     * the {@code queryPropertyPredicate}.
     */
    public static Predicate<String[]> getFilterPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int leftOperandIndexInPropertyResults,
        int rightOperandIndexInPropertyResults) {
        DataType dataType = getDataTypeToCastOperandsTo(queryPropertyPredicate);
        ComparisonOperator operator = queryPropertyPredicate.getComparisonOperator();
        switch (queryPropertyPredicate.getPredicateType()) {
            case TWO_PROPERTY_KEY_OPERANDS:
                return getTwoKeyValueOperandPredicate(leftOperandIndexInPropertyResults,
                    rightOperandIndexInPropertyResults, dataType, operator);
            case PROPERTY_KEY_AND_LITERAL_OPERANDS:
                return getKeyValueAndLiteralOperandPredicate(leftOperandIndexInPropertyResults,
                    queryPropertyPredicate.getLiteral(), dataType, operator);
            default:
                // Should never execute. Every predicate type introduced should be supported.
                throw new IllegalArgumentException("The predicate type " + queryPropertyPredicate.
                    getPredicateType().name() + " is not supported.");
        }
    }

    private static Predicate<String[]> getTwoKeyValueOperandPredicate(
        int variable1IndexInPropertyResults, int variable2IndexInPropertyResults, DataType dataType,
        ComparisonOperator operator) {
        return predicate -> RuntimeTypeBasedComparator.resolveTypesAndCompare(DataType.
            parseDataType(dataType, predicate[variable1IndexInPropertyResults]), DataType.
            parseDataType(dataType, predicate[variable2IndexInPropertyResults]), operator);
    }

    private static Predicate<String[]> getKeyValueAndLiteralOperandPredicate(
        int variableIndexInPropertyResults, String literal, DataType dataType,
        ComparisonOperator operator) {
        return predicate -> RuntimeTypeBasedComparator.resolveTypesAndCompare(DataType.
            parseDataType(dataType, predicate[variableIndexInPropertyResults]), DataType.
            parseDataType(dataType, literal), operator);
    }

    private static DataType getDataTypeToCastOperandsTo(QueryPropertyPredicate predicate) {
        DataType leftOperandDataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(
            predicate.getLeftOperand().b);
        if (leftOperandDataType == DataType.BOOLEAN || leftOperandDataType == DataType.STRING ||
            leftOperandDataType == DataType.DOUBLE) {
            return leftOperandDataType;
        }

        DataType rightOperandDataType = DataType.INTEGER;
        if (null != predicate.getRightOperand()) {
            rightOperandDataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(
                predicate.getRightOperand().b);
        } else if (predicate.getLiteral().contains(".")) {
            // The numerical literal was written as a floating-point.
            rightOperandDataType = DataType.DOUBLE;
        }
        return rightOperandDataType;
    }
}

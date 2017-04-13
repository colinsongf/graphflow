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
     * @param variable1IndexInPropertyResults the index of variable1's property in the property
     * result set created by the {@link Filter} operator. {@link Filter} uses its
     * {@link EdgeOrVertexPropertyDescriptor} list to create the property result set from the
     * {@link MatchQueryOutput}.
     * @param variable2IndexInPropertyResults the index of variable1's property in the property
     * resultset created by the {@link Filter} operator. {@link Filter} uses its
     * {@link EdgeOrVertexPropertyDescriptor} list to create the property result set from the
     * {@link MatchQueryOutput}.
     * @return a {@link Predicate<String[]>} instance that will perform the comparison
     * specified in the {@code queryPropertyPredicate}.
     */
    public static Predicate<String[]> getFilterPredicate(QueryPropertyPredicate
        queryPropertyPredicate, int variable1IndexInPropertyResults,
        int variable2IndexInPropertyResults) {
        DataType dataType = getDataTypeToCastOperandsTo(queryPropertyPredicate);
        ComparisonOperator operator = queryPropertyPredicate.getComparisonOperator();
        switch (queryPropertyPredicate.getPredicateType()) {
            case TWO_VARIABLES:
                return getTwoVariablesPredicate(variable1IndexInPropertyResults,
                    variable2IndexInPropertyResults, dataType, operator);
            case VARIABLE_AND_LITERAL:
                return getVariableAndLiteralPredicate(variable1IndexInPropertyResults,
                    queryPropertyPredicate.getLiteral(), dataType, operator);
            default:
                // Should never execute. Every predicate type introduced should be supported.
                throw new IllegalArgumentException("The predicate type " + queryPropertyPredicate.
                    getPredicateType().name() + " is not supported.");
        }
    }

    private static Predicate<String[]> getTwoVariablesPredicate(int variable1IndexInPropertyResults,
        int variable2IndexInPropertyResults, DataType dataType, ComparisonOperator operator) {
        return predicate -> RuntimeTypeBasedComparator.resolveTypesAndCompare(DataType.
            parseDataType(dataType, predicate[variable1IndexInPropertyResults]), DataType.
            parseDataType(dataType, predicate[variable2IndexInPropertyResults]), operator);
    }

    private static Predicate<String[]> getVariableAndLiteralPredicate(
        int variableIndexInPropertyResults, String literal, DataType dataType,
        ComparisonOperator operator) {
        return predicate -> RuntimeTypeBasedComparator.resolveTypesAndCompare(DataType.
            parseDataType(dataType, predicate[variableIndexInPropertyResults]), DataType.
            parseDataType(dataType, literal), operator);
    }

    private static DataType getDataTypeToCastOperandsTo(QueryPropertyPredicate predicate) {
        DataType leftOperandDataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(
            predicate.getVariable1().b);
        if (leftOperandDataType == DataType.BOOLEAN || leftOperandDataType == DataType.STRING ||
            leftOperandDataType == DataType.DOUBLE) {
            return leftOperandDataType;
        }

        DataType rightOperandDataType = DataType.INTEGER;
        if (null != predicate.getVariable2()) {
            rightOperandDataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(
                predicate.getVariable2().b);
        } else if (predicate.getLiteral().contains(".")) {
            // The numerical literal was written as a floating-point.
            rightOperandDataType = DataType.DOUBLE;
        }
        return rightOperandDataType;
    }
}

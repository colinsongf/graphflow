package ca.waterloo.dsg.graphflow.query.operator.filter;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.EdgeOrVertexPropertyDescriptor;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator;
import ca.waterloo.dsg.graphflow.query.operator.Filter;
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
     * resultset created by the {@link Filter} operator. {@link Filter} uses its
     * {@link EdgeOrVertexPropertyDescriptor} list to create the property resultset from the
     * {@link MatchQueryOutput}.
     * @param variable2IndexInPropertyResults the index of variable1's property in the property
     * resultset created by the {@link Filter} operator. {@link Filter} uses its
     * {@link EdgeOrVertexPropertyDescriptor} list to create the property resultset from the
     * {@link MatchQueryOutput}.
     * @return a {@link Predicate<String[]>} instance that will perform the comparison
     * specified in the {@code queryPropertyPredicate}.
     */
    public static Predicate<String[]> getFilterPredicate(QueryPropertyPredicate
        queryPropertyPredicate, int variable1IndexInPropertyResults,
        int variable2IndexInPropertyResults) {
        switch (queryPropertyPredicate.getPredicateType()) {
            case TWO_VARIABLES:
                return getTwoVariablesPredicate(queryPropertyPredicate,
                    variable1IndexInPropertyResults, variable2IndexInPropertyResults);
            case VARIABLE_AND_LITERAL:
                return getVariableAndLiteralPredicate(queryPropertyPredicate,
                    variable1IndexInPropertyResults);
            default:
                // Should never execute. Every predicate type introduced should be supported.
                throw new IllegalArgumentException("The predicate type " + queryPropertyPredicate.
                    getPredicateType().name() + " is not supported.");
        }
    }

    private static Predicate<String[]> getTwoVariablesPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int variable1IndexInPropertyResults,
        int variable2IndexInPropertyResults) {
        short variable1PropertyKey = TypeAndPropertyKeyStore.getInstance().
            mapStringPropertyKeyToShort(queryPropertyPredicate.getVariable1().b);
        short variable2PropertyKey = TypeAndPropertyKeyStore.getInstance().
            mapStringPropertyKeyToShort(queryPropertyPredicate.getVariable2().b);
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(resolveConstant(
            p[variable1IndexInPropertyResults], variable1PropertyKey), resolveConstant(
            p[variable2IndexInPropertyResults], variable2PropertyKey), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Predicate<String[]> getVariableAndLiteralPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int variableIndexInPropertyResults) {
        short variablePropertyKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort(
            queryPropertyPredicate.getVariable1().b);

        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(resolveConstant(
            p[variableIndexInPropertyResults], variablePropertyKey), resolveConstant(
            queryPropertyPredicate.getLiteral(), variablePropertyKey), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Object resolveConstant(String constant, short propertyKey) {
        DataType dataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(propertyKey);
        return DataType.parseDataType(dataType, constant);
    }
}

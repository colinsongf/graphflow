package ca.waterloo.dsg.graphflow.query.operator.util;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator;
import ca.waterloo.dsg.graphflow.query.operator.Filter;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import org.antlr.v4.runtime.misc.Pair;
import java.util.Map;
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
     * @param orderedVariableIndexMap a {@code Map<String, Integer>} between
     * {@link QueryVariable#variableName} and its index in the {@link MatchQueryOutput} object
     * that is input to the {@link Predicate}.
     * @param orderedEdgeVariableIndexMap a {@code Map<String, Integer>} between query variable
     * representing an edge and its index in the {@link MatchQueryOutput} object
     * that is input to the {@link Predicate}.
     * @return a {@link Predicate<MatchQueryOutput>} instance that will perform the comparison
     * specified in the {@code queryPropertyPredicate}.
     */
    public static Predicate<MatchQueryOutput> getFilterPredicate(QueryPropertyPredicate
        queryPropertyPredicate, Map<String, Integer> orderedVariableIndexMap, Map<String,
        Integer> orderedEdgeVariableIndexMap) {
        switch (queryPropertyPredicate.getPredicateType()) {
            case TWO_VERTEX:
                return getTwoVertexPropertyPredicate(queryPropertyPredicate,
                    orderedVariableIndexMap);
            case TWO_EDGE:
                return getTwoEdgePropertyPredicate(queryPropertyPredicate,
                    orderedEdgeVariableIndexMap);
            case EDGE_AND_VERTEX:
                return getEdgeAndVertexPropertyPredicate(queryPropertyPredicate,
                    orderedVariableIndexMap, orderedEdgeVariableIndexMap);
            case VERTEX_AND_EDGE:
                return getVertexAndEdgePropertyPredicate(queryPropertyPredicate,
                    orderedVariableIndexMap, orderedEdgeVariableIndexMap);
            case VERTEX_AND_CONSTANT:
                return getVertexAndConstantPropertyPredicate(queryPropertyPredicate,
                    orderedVariableIndexMap);
            case EDGE_AND_CONSTANT:
                return getEdgeAndConstantPropertyPredicate(queryPropertyPredicate,
                    orderedEdgeVariableIndexMap);
        }

        // Should never execute. Every predicate type introduced should be supported.
        throw new IllegalArgumentException("The predicate type " + queryPropertyPredicate.
            getPredicateType().name() + " is not supported.");
    }

    private static Predicate<MatchQueryOutput> getTwoVertexPropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate,
        Map<String, Integer> orderedVertexVariableIndexMap) {
        Pair<Integer, Short> var1IdWithProperty = getVariableIdWithProperty(
            orderedVertexVariableIndexMap, queryPropertyPredicate.getVariable1());
        Pair<Integer, Short> var2IdWithProperty = getVariableIdWithProperty(
            orderedVertexVariableIndexMap, queryPropertyPredicate.getVariable2());

        VertexPropertyStore vertexStore = VertexPropertyStore.getInstance();
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(
            vertexStore.getProperty(p.vertexIds[var1IdWithProperty.a], var1IdWithProperty.b),
            vertexStore.getProperty(p.vertexIds[var2IdWithProperty.a], var2IdWithProperty.b),
            queryPropertyPredicate.getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getTwoEdgePropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate,
        Map<String, Integer> orderedEdgeVariableIndexMap) {
        Pair<Integer, Short> var1IdWithProperty = getVariableIdWithProperty(
            orderedEdgeVariableIndexMap, queryPropertyPredicate.getVariable1());
        Pair<Integer, Short> var2IdWithProperty = getVariableIdWithProperty(
            orderedEdgeVariableIndexMap, queryPropertyPredicate.getVariable2());

        EdgeStore edgeStore = EdgeStore.getInstance();
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(
            edgeStore.getProperty(p.edgeIds[var1IdWithProperty.a], var1IdWithProperty.b),
            edgeStore.getProperty(p.edgeIds[var2IdWithProperty.a], var2IdWithProperty.b),
            queryPropertyPredicate.getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getVertexAndEdgePropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate,
        Map<String, Integer> orderedVertexVariableIndexMap,
        Map<String, Integer> orderedEdgeVariableIndexMap) {
        Pair<Integer, Short> var1IdWithProperty = getVariableIdWithProperty(
            orderedVertexVariableIndexMap, queryPropertyPredicate.getVariable1());
        Pair<Integer, Short> var2IdWithProperty = getVariableIdWithProperty(
            orderedEdgeVariableIndexMap, queryPropertyPredicate.getVariable2());

        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(VertexPropertyStore.
                getInstance().getProperty(p.vertexIds[var1IdWithProperty.a], var1IdWithProperty.b),
            EdgeStore.getInstance().getProperty(p.edgeIds[var2IdWithProperty.a],
                var2IdWithProperty.b), queryPropertyPredicate.getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getEdgeAndVertexPropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate,
        Map<String, Integer> orderedVertexVariableIndexMap,
        Map<String, Integer> orderedEdgeVariableIndexMap) {
        Pair<Integer, Short> var1IdWithProperty = getVariableIdWithProperty(
            orderedEdgeVariableIndexMap, queryPropertyPredicate.getVariable1());
        Pair<Integer, Short> var2IdWithProperty = getVariableIdWithProperty(
            orderedVertexVariableIndexMap, queryPropertyPredicate.getVariable2());

        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(EdgeStore.getInstance().
            getProperty(p.edgeIds[var1IdWithProperty.a], var1IdWithProperty.b), VertexPropertyStore.
                getInstance().getProperty(p.vertexIds[var2IdWithProperty.a], var2IdWithProperty.b),
            queryPropertyPredicate.getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getVertexAndConstantPropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate,
        Map<String, Integer> orderedVertexVariableIndexMap) {
        Pair<Integer, Short> var1IdWithProperty = getVariableIdWithProperty(
            orderedVertexVariableIndexMap, queryPropertyPredicate.getVariable1());

        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(VertexPropertyStore.
                getInstance().getProperty(p.vertexIds[var1IdWithProperty.a], var1IdWithProperty.b),
            resolveConstant(queryPropertyPredicate.getConstant(), var1IdWithProperty.b),
            queryPropertyPredicate.getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getEdgeAndConstantPropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate,
        Map<String, Integer> orderedEdgeVariableIndexMap) {
        Pair<Integer, Short> var1IdWithProperty = getVariableIdWithProperty(
            orderedEdgeVariableIndexMap, queryPropertyPredicate.getVariable1());

        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(EdgeStore.getInstance().
                getProperty(p.edgeIds[var1IdWithProperty.a], var1IdWithProperty.b),
            resolveConstant(queryPropertyPredicate.getConstant(), var1IdWithProperty.b),
            queryPropertyPredicate.getComparisonOperator());
    }

    private static Pair<Integer, Short> getVariableIdWithProperty(
        Map<String, Integer> orderedVariableIndexMap, Pair<String, String> variable) {
        return new Pair<>(orderedVariableIndexMap.get(variable.a), TypeAndPropertyKeyStore.
            getInstance().mapStringPropertyKeyToShort(variable.b));
    }

    private static Object resolveConstant(String constant, short propertyKey) {
        DataType dataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(propertyKey);
        return DataType.parseDataType(dataType, constant);
    }
}

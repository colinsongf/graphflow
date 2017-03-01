package ca.waterloo.dsg.graphflow.query.operator.filter;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;
import ca.waterloo.dsg.graphflow.query.operator.EdgeOrVertexPropertyDescriptor;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator;
import ca.waterloo.dsg.graphflow.query.operator.Filter;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import org.antlr.v4.runtime.misc.Pair;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;


/**
 * Contains static methods for creating {@link Predicate} objects that are used by {@link Filter}.
 */
public class FilterPredicateFactory {

    private static TypeAndPropertyKeyStore typeAndPropertyKeyStore = TypeAndPropertyKeyStore
        .getInstance();
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
            case TWO_VERTEX:
                return getTwoVertexPropertyPredicate(queryPropertyPredicate,
                    variable1IndexInPropertyResults, variable2IndexInPropertyResults);
            case TWO_EDGE:
                return getTwoEdgePropertyPredicate(queryPropertyPredicate,
                    variable1IndexInPropertyResults, variable2IndexInPropertyResults);
            case EDGE_AND_VERTEX:
                return getEdgeAndVertexPropertyPredicate(queryPropertyPredicate,
                    variable1IndexInPropertyResults, variable2IndexInPropertyResults);
            case VERTEX_AND_EDGE:
                return getVertexAndEdgePropertyPredicate(queryPropertyPredicate,
                    variable1IndexInPropertyResults, variable2IndexInPropertyResults);
            case VERTEX_AND_LITERAL:
                return getVertexAndConstantPropertyPredicate(queryPropertyPredicate,
                    variable1IndexInPropertyResults);
            case EDGE_AND_LITERAL:
                return getEdgeAndConstantPropertyPredicate(queryPropertyPredicate,
                    variable1IndexInPropertyResults);
        }

        // Should never execute. Every predicate type introduced should be supported.
        throw new IllegalArgumentException("The predicate type " + queryPropertyPredicate.
            getPredicateType().name() + " is not supported.");
    }

    private static Predicate<String[]> getTwoVertexPropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int vertex1IndexInPropertyResults,
        int vertex2IndexInPropertyResults) {
        short vertex1PropertyKey = typeAndPropertyKeyStore.mapStringPropertyKeyToShort
            (queryPropertyPredicate.getVariable1().b);
        short vertex2PropertyKey = typeAndPropertyKeyStore.mapStringPropertyKeyToShort
            (queryPropertyPredicate.getVariable2().b);
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(resolveConstant(
            p[vertex1IndexInPropertyResults], vertex1PropertyKey), resolveConstant(
            p[vertex2IndexInPropertyResults], vertex2PropertyKey), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Predicate<String[]> getTwoEdgePropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int edge1IndexInPropertyResults,
        int edge2IndexInPropertyResults) {
        short edge1PropertyKey = typeAndPropertyKeyStore.mapStringPropertyKeyToShort
            (queryPropertyPredicate.getVariable1().b);
        short edge2PropertyKey = typeAndPropertyKeyStore.mapStringPropertyKeyToShort
            (queryPropertyPredicate.getVariable2().b);
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(resolveConstant(
            p[edge1IndexInPropertyResults], edge1PropertyKey), resolveConstant(
            p[edge2IndexInPropertyResults], edge2PropertyKey), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Predicate<String[]> getVertexAndEdgePropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int vertexIndexInPropertyResults,
        int edgeIndexInPropertyResults) {
        short vertexPropertyKey = typeAndPropertyKeyStore.mapStringPropertyKeyToShort
            (queryPropertyPredicate.getVariable1().b);
        short edgePropertyKey = typeAndPropertyKeyStore.mapStringPropertyKeyToShort
            (queryPropertyPredicate.getVariable2().b);
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(resolveConstant(
            p[vertexIndexInPropertyResults], vertexPropertyKey), resolveConstant(
            p[edgeIndexInPropertyResults], edgePropertyKey), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Predicate<String[]> getEdgeAndVertexPropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int
        edgeIndexInPropertyResults, int vertexIndexInPropertyResults) {
        short edgePropertyKey = typeAndPropertyKeyStore.mapStringPropertyKeyToShort
            (queryPropertyPredicate.getVariable1().b);
        short vertexPropertyKey = typeAndPropertyKeyStore.mapStringPropertyKeyToShort
            (queryPropertyPredicate.getVariable2().b);
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(resolveConstant(
            p[edgeIndexInPropertyResults], edgePropertyKey), resolveConstant(
            p[vertexIndexInPropertyResults], vertexPropertyKey), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Predicate<String[]> getVertexAndConstantPropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int vertexIndexInPropertyResults) {
        short vertexPropertyKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort(
            queryPropertyPredicate.getVariable1().b);

        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(resolveConstant(
            p[vertexIndexInPropertyResults], vertexPropertyKey), resolveConstant(
            queryPropertyPredicate.getLiteral(), vertexPropertyKey), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Predicate<String[]> getEdgeAndConstantPropertyPredicate(
        QueryPropertyPredicate queryPropertyPredicate, int edgeIndexInPropertyResults) {
        short edgePropertyKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort(
            queryPropertyPredicate.getVariable1().b);

        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(resolveConstant
            (p[edgeIndexInPropertyResults], edgePropertyKey), resolveConstant(
            queryPropertyPredicate.getLiteral(), edgePropertyKey), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Object resolveConstant(String constant, short propertyKey) {
        DataType dataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(propertyKey);
        return DataType.parseDataType(dataType, constant);
    }
}

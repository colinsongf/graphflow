package ca.waterloo.dsg.graphflow.query.operator.util;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator;

import java.util.Map;
import java.util.function.Predicate;

public class FilterPredicateFactory {

    public static Predicate<MatchQueryOutput> getFilterPredicate(QueryPropertyPredicate
        queryPropertyPredicate, Map<String, Integer> orderedVariableIndexMap, Map<String,
        Integer> orderedEdgeVariableIndexMap) {
        switch (queryPropertyPredicate.getPredicateType()) {
            case TWO_VERTEX:
                return getTwoVertexPropertyPredicate(queryPropertyPredicate,
                    orderedVariableIndexMap);
            case TWO_EDGE:
                return getTwoEdgePropertyPredicate(queryPropertyPredicate, orderedEdgeVariableIndexMap);
            case EDGE_AND_VERTEX:
                return getVertexAndEdgePropertyPredicate(queryPropertyPredicate,
                    orderedVariableIndexMap, orderedEdgeVariableIndexMap);
            case VERTEX_AND_CONSTANT:
                return getVertexAndConstantPropertyPredicate(queryPropertyPredicate,
                    orderedVariableIndexMap);
            case EDGE_AND_CONSTANT:
                return getEdgeAndConstantPropertyPredicate(queryPropertyPredicate,
                    orderedEdgeVariableIndexMap);
            default:
                return null;
        }

    }

    private static Predicate<MatchQueryOutput> getTwoVertexPropertyPredicate
        (QueryPropertyPredicate queryPropertyPredicate, Map<String, Integer>
            orderedVariableIndexMap) {

        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(VertexPropertyStore.getInstance()
            .getProperty(p.vertexIds[orderedVariableIndexMap.get(queryPropertyPredicate.getVariable1().a)],
            queryPropertyPredicate.getVariable1().b),
            VertexPropertyStore.getInstance().getProperty(p.vertexIds[orderedVariableIndexMap.get
                (queryPropertyPredicate.getVariable2().a)], queryPropertyPredicate
                .getVariable1().b), queryPropertyPredicate.getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getTwoEdgePropertyPredicate(QueryPropertyPredicate
        queryPropertyPredicate, Map<String, Integer> orderedEdgeVariableIndexMap) {
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(EdgeStore.getInstance().getProperty(p.
            edgeIds[orderedEdgeVariableIndexMap.get(queryPropertyPredicate.getVariable1().a)],
            queryPropertyPredicate.getVariable1().b),
            EdgeStore.getInstance().getProperty(p.edgeIds[orderedEdgeVariableIndexMap.get
                (queryPropertyPredicate.getVariable2().a)], queryPropertyPredicate
                .getVariable2().b), queryPropertyPredicate.getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getVertexAndEdgePropertyPredicate
        (QueryPropertyPredicate
            queryPropertyPredicate, Map<String, Integer> orderedVariableIndexMap, Map<String,
            Integer> orderedEdgeVariableIndexMap) {
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(VertexPropertyStore.getInstance().getProperty(p
                .vertexIds[orderedVariableIndexMap.get(queryPropertyPredicate.getVariable1().a)],
            queryPropertyPredicate.getVariable1().b),
            EdgeStore.getInstance().getProperty(p.edgeIds[orderedEdgeVariableIndexMap.get
                (queryPropertyPredicate.getVariable2().a)], queryPropertyPredicate
                .getVariable2().b), queryPropertyPredicate.getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getVertexAndConstantPropertyPredicate
        (QueryPropertyPredicate
            queryPropertyPredicate, Map<String, Integer> orderedVariableIndexMap) {
        System.out.println(orderedVariableIndexMap.get(queryPropertyPredicate.getVariable1().a));
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(VertexPropertyStore.getInstance().getProperty(p.
                vertexIds[orderedVariableIndexMap.get(queryPropertyPredicate.getVariable1().a)],
            queryPropertyPredicate.getVariable1().b), queryPropertyPredicate.
            resolveConstant(queryPropertyPredicate.getConstant()), queryPropertyPredicate.
            getComparisonOperator());
    }

    private static Predicate<MatchQueryOutput> getEdgeAndConstantPropertyPredicate
        (QueryPropertyPredicate
            queryPropertyPredicate, Map<String, Integer> orderedVariableIndexMap) {
        return p -> RuntimeTypeBasedComparator.resolveTypesAndCompare(EdgeStore.getInstance().getProperty(p.
                edgeIds[orderedVariableIndexMap.get(queryPropertyPredicate.getVariable1().a)],
            queryPropertyPredicate.getVariable1().b), queryPropertyPredicate.
            resolveConstant(queryPropertyPredicate.getConstant()), queryPropertyPredicate.
            getComparisonOperator());
    }


}


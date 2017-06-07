package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.exceptions.IncorrectDataTypeException;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.operator.sinks.OutputSink;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.DataType;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Map;

/**
 * Class representing plan for a CREATE operation.
 */
public class CreateQueryPlan implements QueryPlan {

    private StructuredQuery structuredQuery;

    public CreateQueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    /**
     * Executes the {@link CreateQueryPlan}.
     *
     * @param outputSink the {@link OutputSink} to which the execution output is written.
     *
     * @throws IncorrectDataTypeException if there are two new properties in the query with the same
     * key but different {@link DataType} or if the {@link DataType} of a property key K is not the
     * same as the {@link DataType} that has been stored previously for K.
     */
    public void execute(OutputSink outputSink) {
        if (structuredQuery.getQueryRelations().size() > 0) {
            createEdges(outputSink);
        } else {
            createVertices(outputSink);
        }
    }

    private void createVertices(OutputSink outputSink) {
        TypeAndPropertyKeyStore typeAndPropertyKeyStore = TypeAndPropertyKeyStore.getInstance();
        try {
            for (QueryVariable queryVariable : structuredQuery.getQueryVariables()) {
                Map<String, Pair<String, String>> stringvertexProperties = queryVariable.
                    getVariableProperties();
                typeAndPropertyKeyStore.assertExistingKeyDataTypesMatchPreviousDeclarations(
                    stringvertexProperties);

                int vertexId = Integer.parseInt(queryVariable.getVariableName());
                short vertexType = typeAndPropertyKeyStore.mapStringTypeToShortOrInsert(
                    queryVariable.getVariableType());
                Map<Short, Pair<DataType, String>> vertexProperties = typeAndPropertyKeyStore.
                    mapStringPropertiesToShortAndDataTypeOrInsert(stringvertexProperties);

                Graph.getInstance().addVertex(vertexId, vertexType, vertexProperties);
            }
            // TODO(amine): bug, count the actual number of vertices created to append to sink.
            outputSink.append(structuredQuery.getQueryVariables().size() + " vertices created.");
        } catch (UnsupportedOperationException e) {
            outputSink.append("ERROR: " + e.getMessage());
        }
    }

    private void createEdges(OutputSink outputSink) {
        TypeAndPropertyKeyStore typeAndPropertyKeyStore = TypeAndPropertyKeyStore.getInstance();
        try {
            for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
                Map<String, Pair<String, String>> stringFromVertexProperties = queryRelation.
                    getFromQueryVariable().getVariableProperties();
                Map<String, Pair<String, String>> stringToVertexProperties = queryRelation.
                    getToQueryVariable().getVariableProperties();
                Map<String, Pair<String, String>> stringEdgeProperties = queryRelation.
                    getRelationProperties();

                assertDataTypesAreConsistent(stringFromVertexProperties,
                    stringToVertexProperties);
                assertDataTypesAreConsistent(stringEdgeProperties,
                    stringFromVertexProperties);
                assertDataTypesAreConsistent(stringEdgeProperties,
                    stringToVertexProperties);

                typeAndPropertyKeyStore.assertExistingKeyDataTypesMatchPreviousDeclarations(
                    stringFromVertexProperties);
                typeAndPropertyKeyStore.assertExistingKeyDataTypesMatchPreviousDeclarations(
                    stringToVertexProperties);
                typeAndPropertyKeyStore.assertExistingKeyDataTypesMatchPreviousDeclarations(
                    stringEdgeProperties);

                int fromVertex = Integer.parseInt(queryRelation.getFromQueryVariable().
                    getVariableName());
                int toVertex = Integer.parseInt(queryRelation.getToQueryVariable().
                    getVariableName());

                short fromVertexType = typeAndPropertyKeyStore.mapStringTypeToShortOrInsert(
                    queryRelation.getFromQueryVariable().getVariableType());
                short toVertexType = typeAndPropertyKeyStore.mapStringTypeToShortOrInsert(
                    queryRelation.getToQueryVariable().getVariableType());
                short edgeType = typeAndPropertyKeyStore.mapStringTypeToShortOrInsert(
                    queryRelation.getRelationType());

                Map<Short, Pair<DataType, String>> fromVertexProperties = typeAndPropertyKeyStore.
                    mapStringPropertiesToShortAndDataTypeOrInsert(stringFromVertexProperties);
                Map<Short, Pair<DataType, String>> toVertexProperties = typeAndPropertyKeyStore.
                    mapStringPropertiesToShortAndDataTypeOrInsert(stringToVertexProperties);
                Map<Short, Pair<DataType, String>> edgeProperties = typeAndPropertyKeyStore.
                    mapStringPropertiesToShortAndDataTypeOrInsert(stringEdgeProperties);

                Graph.getInstance().addEdgeTemporarily(fromVertex, toVertex, fromVertexType,
                    toVertexType, fromVertexProperties, toVertexProperties, edgeType,
                    edgeProperties);
            }
            ContinuousMatchQueryExecutor.getInstance().execute();
            Graph.getInstance().finalizeChanges();
            // TODO(amine): bug, count the actual number of edges created to append to sink.
            outputSink.append(structuredQuery.getQueryRelations().size() + " edges created.");
        } catch (UnsupportedOperationException e) {
            outputSink.append("ERROR: " + e.getMessage());
        }
    }

    private void assertDataTypesAreConsistent(
        Map<String, Pair<String, String>> thisPropertiesCollection,
        Map<String, Pair<String, String>> thatPropertiesCollection) {
        if (null == thisPropertiesCollection || null == thatPropertiesCollection) {
            return;
        }
        for (String propertyKey : thisPropertiesCollection.keySet()) {
            String thisDataType = thisPropertiesCollection.get(propertyKey).a.toUpperCase();
            String thatDataType = null;
            if (null != thatPropertiesCollection.get(propertyKey)) {
                thatDataType = thatPropertiesCollection.get(propertyKey).a.toUpperCase();
            }
            if (null != thatDataType && !thisDataType.equals(thatDataType)) {
                throw new IncorrectDataTypeException("Inconsistent DataType usage - property " +
                    "key " + propertyKey + " is used with two different data types: " +
                    thisDataType + " and " + thatDataType + ".");
            }
        }
    }
}

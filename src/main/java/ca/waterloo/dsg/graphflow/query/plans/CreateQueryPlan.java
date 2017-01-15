package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.StringToShortKeyStore;

import java.util.HashMap;
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
     * @param graph the {@link Graph} instance to use during the plan execution.
     * @param outputSink the {@link OutputSink} to which the execution output is written.
     */
    public void execute(Graph graph, OutputSink outputSink) {
        try {
            for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {

                // assert that the types of the properties in the query match within themselves and
                // are also matched with previous property declarations from previous executed
                // queries.
                TypeAndPropertyKeyStore.getInstance().assertEachPropertyTypeCorrectness(
                    queryRelation.getFromQueryVariable().getVariableProperties(), queryRelation.
                        getToQueryVariable().getVariableProperties(), queryRelation.
                        getRelationProperties());

                // get the from and to vertex Ids.
                int fromVertex = Integer.parseInt(queryRelation.getFromQueryVariable().
                    getVariableId());
                int toVertex = Integer.parseInt(queryRelation.getToQueryVariable().getVariableId());

                // Insert the types into the {@code TypeAndPropertyKeyStore} if they do not already
                // exist, and get their {@code short} IDs.
                short fromVertexType = TypeAndPropertyKeyStore.getInstance().
                    getTypeAsShortOrInsertIfDoesNotExist(queryRelation.getFromQueryVariable().
                        getVariableType());
                short toVertexType = TypeAndPropertyKeyStore.getInstance().
                    getTypeAsShortOrInsertIfDoesNotExist(queryRelation.getToQueryVariable().
                        getVariableType());
                short edgeType = TypeAndPropertyKeyStore.getInstance().
                    getTypeAsShortOrInsertIfDoesNotExist(queryRelation.getRelationType());

                // get the properties as short key, string value pairs from vertices and edge.
                HashMap<Short, String> fromVertexProperties = TypeAndPropertyKeyStore
                    .getInstance().getPropertiesAsShortStringKeyValuesOrInsertIfDoesNotExist(
                        queryRelation.getFromQueryVariable().getVariableProperties());
                HashMap<Short, String> toVertexProperties = TypeAndPropertyKeyStore
                    .getInstance().getPropertiesAsShortStringKeyValuesOrInsertIfDoesNotExist(
                        queryRelation.getToQueryVariable().getVariableProperties());
                HashMap<Short, String> edgeProperties = TypeAndPropertyKeyStore
                    .getInstance().getPropertiesAsShortStringKeyValuesOrInsertIfDoesNotExist(
                        queryRelation.getRelationProperties());

                // Add the new edge to the graph.
                graph.addEdgeTemporarily(fromVertex, toVertex, fromVertexType, toVertexType,
                    fromVertexProperties, toVertexProperties, edgeType, edgeProperties);
            }
            ContinuousMatchQueryExecutor.getInstance().execute(graph);
            graph.finalizeChanges();
            outputSink.append(structuredQuery.getQueryRelations().size() + " edges created.");
        } catch (UnsupportedOperationException e) {
            outputSink.append("ERROR: " + e.getMessage());
        }
    }
}

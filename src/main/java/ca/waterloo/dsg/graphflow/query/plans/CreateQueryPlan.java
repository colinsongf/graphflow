package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;

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
                int fromVertex = Integer.parseInt(queryRelation.getFromQueryVariable().
                    getVariableId());
                int toVertex = Integer.parseInt(queryRelation.getToQueryVariable().getVariableId());
                // Insert the types into the {@code TypeStore} if they do not already exist, and
                // get their {@code short} IDs. An exception in the above {@code parseInt()} calls
                // will prevent the insertion of any new type to the {@code TypeStore}.
                short fromVertexTypeId = TypeAndPropertyKeyStore.getInstance().
                    getTypeAsShortOrInsertIfDoesNotExist(queryRelation.getFromQueryVariable().
                        getVariableType());
                short toVertexTypeId = TypeAndPropertyKeyStore.getInstance().
                    getTypeAsShortOrInsertIfDoesNotExist(queryRelation.getToQueryVariable().
                        getVariableType());
                short edgeTypeId = TypeAndPropertyKeyStore.getInstance().
                    getTypeAsShortOrInsertIfDoesNotExist(queryRelation.getRelationType());
                // Add the new edge to the graph.
                graph.addEdgeTemporarily(fromVertex, toVertex, fromVertexTypeId, toVertexTypeId,
                    null /* no fromVertex properties */, null /* no toVertexProperties */,
                    edgeTypeId);
            }
            ContinuousMatchQueryExecutor.getInstance().execute(graph);
            graph.finalizeChanges();
            outputSink.append(structuredQuery.getQueryRelations().size() + " edges created.");
        } catch (UnsupportedOperationException e) {
            outputSink.append("ERROR: " + e.getMessage());
        }
    }
}

package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.exceptions.NoSuchTypeException;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;

/**
 * Class representing plan for a DELETE operation.
 */
public class DeleteQueryPlan implements QueryPlan {

    private StructuredQuery structuredQuery;

    public DeleteQueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    /**
     * Executes the {@link DeleteQueryPlan}.
     *
     * @param graph the {@link Graph} instance to use during the plan execution.
     * @param outputSink the {@link AbstractDBOperator} to which the execution output is written.
     */
    public void execute(Graph graph, AbstractDBOperator outputSink) {
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            try {
                TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortAndAssertTypeExists(
                    queryRelation.getRelationType());
                graph.deleteEdgeTemporarily(Integer.parseInt(queryRelation.getFromQueryVariable()
                    .getVariableName()), Integer.parseInt(queryRelation.getToQueryVariable()
                    .getVariableName()), TypeAndPropertyKeyStore.getInstance()
                    .mapStringTypeToShort(queryRelation.getRelationType()));
            } catch (NoSuchTypeException e) {
                outputSink.append("ERROR: " + e.getMessage());
            }
        }
        ContinuousMatchQueryExecutor.getInstance().execute(graph);
        graph.finalizeChanges();
        // TODO(amine): bug, count the actual num of edges deleted to append to sink.
        outputSink.append(structuredQuery.getQueryRelations().size() + " edges deleted.");
    }
}

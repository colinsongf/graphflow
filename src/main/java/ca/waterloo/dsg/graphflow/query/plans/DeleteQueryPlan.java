package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;

import java.util.NoSuchElementException;

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
     * @param outputSink the {@link OutputSink} to which the execution output is written.
     */
    public void execute(Graph graph, OutputSink outputSink) {
        try {
            for (QueryEdge queryEdge : structuredQuery.getQueryEdges()) {
                graph.deleteEdgeTemporarily(
                    Integer.parseInt(queryEdge.getFromQueryVariable().getVariableId()),
                    Integer.parseInt(queryEdge.getToQueryVariable().getVariableId()),
                    TypeStore.getInstance().getShortIdOrAnyTypeIfNull(queryEdge.getEdgeType()));
            }
            ContinuousMatchQueryExecutor.getInstance().execute(graph);
            graph.finalizeChanges();
            outputSink.append(structuredQuery.getQueryEdges().size() + " edges deleted.");
        } catch (NoSuchElementException e) {
            outputSink.append("ERROR: " + e.getMessage());
        }
    }
}

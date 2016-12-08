package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;

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
            for (StructuredQueryEdge structuredQueryEdge : structuredQuery
                .getStructuredQueryEdges()) {
//                graph.deleteEdgeTemporarily(Integer.parseInt(structuredQueryEdge.getFromVertex()),
//                    Integer.parseInt(structuredQueryEdge.getToVertex()));
            }
            ContinuousMatchQueryExecutor.getInstance().execute(graph);
            graph.finalizeChanges();
            outputSink.append(structuredQuery.getStructuredQueryEdges().size() + " edges deleted.");
        } catch (NoSuchElementException e) {
            outputSink.append("ERROR: " + e.getMessage());
        }
    }
}

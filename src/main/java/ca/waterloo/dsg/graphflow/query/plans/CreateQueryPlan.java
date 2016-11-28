package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;

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
        // TODO: use type IDs.
        //        for (QueryEdge queryEdge : structuredQuery.getQueryEdges()) {
        //            graph.addEdgeTemporarily(Integer.parseInt(queryEdge.getFromQueryVariable()
        //                .getVariableId()), Integer.parseInt(queryEdge.getToQueryVariable()
        //                .getVariableId()));
        //        }
        ContinuousMatchQueryExecutor.getInstance().execute(graph);
        graph.finalizeChanges();
        outputSink.append(structuredQuery.getQueryEdges().size() + " edges created.");
    }
}

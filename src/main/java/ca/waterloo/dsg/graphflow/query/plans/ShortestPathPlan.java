package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.ShortestPathExecutor;

/**
 * Represents the execution plan for a shortest path query.
 */
public class ShortestPathPlan implements QueryPlan {

    private int source = -1;
    private int destination = -1;

    public ShortestPathPlan(int source, int destination) {
        this.source = source;
        this.destination = destination;
    }

    /**
     * Executes the {@link OneTimeMatchQueryPlan}.
     *
     * @param outputSink the {@link OutputSink} to which the execution output is written.
     */
    public void execute(OutputSink outputSink) {
        ShortestPathExecutor.getInstance().execute(source, destination, outputSink);
    }
}

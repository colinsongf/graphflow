package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.query.executors.ShortestPathExecutor;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;

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
     * @param outputSink the {@link AbstractDBOperator} to which the execution output is written.
     */
    public void execute(AbstractDBOperator outputSink) {
        ShortestPathExecutor.getInstance().execute(source, destination, outputSink);
    }
}

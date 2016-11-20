package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.ShortestPathOutputSink;
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

    @Override
    public String execute(Graph graph) {
        ShortestPathOutputSink outputSink = new ShortestPathOutputSink();
        ShortestPathExecutor shortestPathExecutor = ShortestPathExecutor.getInstance();
        shortestPathExecutor.evaluate(source, destination, outputSink);
        return outputSink.getResults().toString();
    }
}

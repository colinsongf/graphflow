package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;

/**
 * Abstract class representing base operations for creating operation plans.
 */
public interface QueryPlan {
    String execute(Graph graph);
}

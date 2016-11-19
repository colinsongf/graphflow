package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;

/**
 * Represents the execution plan for a shortest path query.
 */
public class ShortestPathPlan implements QueryPlan {

    int start = -1;
    int target = -1;

    public ShortestPathPlan(int start, int target) {
        this.start = start;
        this.target = target;
    }

    @Override
    public String execute(Graph graph) {

        return null;
    }

    @Override
    public boolean equalsTo(Object o) {
        return false;
    }
}

package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.ShortestPathPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;

/**
 * Creates the execution plan for a shortest path query.
 */
public class ShortestPathPlanner extends AbstractQueryPlanner {

    public ShortestPathPlanner(StructuredQuery structuredQuery) {
        super(structuredQuery);
        this.structuredQuery = structuredQuery;
    }

    @Override
    QueryPlan plan() {
        QueryEdge shortestPathEdge = structuredQuery.getQueryEdges().get(0);
        return new ShortestPathPlan(Integer.parseInt(shortestPathEdge.getFromQueryVariable()
            .getVariableId()), Integer.parseInt(shortestPathEdge.getToQueryVariable()
            .getVariableId()));
    }
}

package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.ShortestPathPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;

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
        StructuredQueryEdge shortestPathEdge = structuredQuery.getStructuredQueryEdges()
            .get(0);
        return new ShortestPathPlan(Integer.parseInt(shortestPathEdge
            .getFromVertex()), Integer.parseInt(shortestPathEdge.getToVertex()));
    }
}

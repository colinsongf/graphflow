package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.ShortestPathPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;

import java.util.InputMismatchException;

/**
 * Creates the execution plan for a shortest path query.
 */
public class ShortestPathPlanner extends AbstractQueryPlanner{

    public ShortestPathPlanner(StructuredQuery structuredQuery) {
        super(structuredQuery);
        this.structuredQuery = structuredQuery;
    }

    @Override
    QueryPlan plan() throws InputMismatchException{
        if(structuredQuery.getStructuredQueryEdges().isEmpty()) {
            throw new InputMismatchException();
        }
        StructuredQueryEdge shortestPathEdge = structuredQuery.getStructuredQueryEdges()
            .get(0);
        ShortestPathPlan plan = new ShortestPathPlan(Integer.parseInt(shortestPathEdge
            .getFromVertex()), Integer.parseInt(shortestPathEdge.getToVertex()));
        return plan;
    }
}

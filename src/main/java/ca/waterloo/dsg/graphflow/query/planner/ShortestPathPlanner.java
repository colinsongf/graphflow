package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.ShortestPathPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;

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
        QueryRelation shortestPathEdge = structuredQuery.getQueryRelations().get(0);
        return new ShortestPathPlan(Integer.parseInt(shortestPathEdge.getFromQueryVariable()
            .getVariableId()), Integer.parseInt(shortestPathEdge.getToQueryVariable()
            .getVariableId()));
    }
}

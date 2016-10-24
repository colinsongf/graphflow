package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.MatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

/**
 * Create a {@code QueryPlan} for the MATCH operation.
 */
public class MatchQueryPlanner implements IQueryPlanner {

    public QueryPlan plan(StructuredQuery query) {
        return new MatchQueryPlan(query);
    }
}

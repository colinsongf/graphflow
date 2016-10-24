package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.DeleteQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

/**
 * Create a {@code QueryPlan} for the DELETE operation.
 */
public class DeleteQueryPlanner implements IQueryPlanner {

    @Override
    public QueryPlan plan(StructuredQuery query) {
        return new DeleteQueryPlan(query);
    }
}

package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.DeleteQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.IQueryPlan;

/**
 * Create a {@code IQueryPlan} for the DELETE operation.
 */
public class DeleteQueryPlanner implements IQueryPlanner {

    @Override
    public IQueryPlan plan(StructuredQuery query) {
        return new DeleteQueryPlan(query);
    }
}

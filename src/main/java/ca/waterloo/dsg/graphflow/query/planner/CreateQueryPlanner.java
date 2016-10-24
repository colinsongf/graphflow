package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.CreateQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

/**
 * Create a {@code QueryPlan} for the CREATE operation.
 */
public class CreateQueryPlanner implements IQueryPlanner {

    @Override
    public QueryPlan plan(StructuredQuery query) {
        return new CreateQueryPlan(query);
    }
}

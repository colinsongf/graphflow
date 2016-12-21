package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.plans.CreateQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;

/**
 * Create a {@code QueryPlan} for the CREATE operation.
 */
public class CreateQueryPlanner extends AbstractQueryPlanner {

    public CreateQueryPlanner(StructuredQuery structuredQuery) {
        super(structuredQuery);
    }

    @Override
    public QueryPlan plan() {
        return new CreateQueryPlan(structuredQuery);
    }
}

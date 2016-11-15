package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;

/**
 * Class for building the corresponding {@code QueryPlan} of the input operation.
 */
public class QueryPlanBuilder {

    public QueryPlan plan(StructuredQuery query) {
        QueryPlan plan = null;
        switch (query.getQueryOperation()) {
            case CREATE:
                plan = new CreateQueryPlanner(query).plan();
                break;
            case DELETE:
                plan = new DeleteQueryPlanner(query).plan();
                break;
            case MATCH:
                plan = new MatchQueryPlanner(query).plan();
                break;
            default:
                break;
        }
        return plan;
    }
}

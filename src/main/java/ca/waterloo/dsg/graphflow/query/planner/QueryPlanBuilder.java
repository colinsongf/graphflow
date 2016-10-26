package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.IQueryPlan;

/**
 * Class for building the corresponding {@code IQueryPlan} of the input operation.
 */
public class QueryPlanBuilder {

    public IQueryPlan plan(StructuredQuery query) {
        IQueryPlanner planner = null;
        switch (query.getOperation()) {
            case CREATE:
                planner = new CreateQueryPlanner();
                break;
            case DELETE:
                planner = new DeleteQueryPlanner();
                break;
            case MATCH:
                planner = new MatchQueryPlanner();
                break;
            default:
                break;
        }
        return planner.plan(query);
    }
}

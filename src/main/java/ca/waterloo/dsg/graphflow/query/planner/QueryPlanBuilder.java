package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

public class QueryPlanBuilder {

    public QueryPlan plan(StructuredQuery query) {
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

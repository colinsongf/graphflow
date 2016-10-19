package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

public class QueryPlanBuilder {

    IQueryPlanner planner;
    QueryPlan queryPlan;

    public QueryPlan plan(StructuredQuery query) {
        switch (query.getOperation()) {
            case CREATE:
                this.planner = new CreateQueryPlanner();
                break;
            case DELETE:
                this.planner = new DeleteQueryPlanner();
                break;
            case MATCH:
                this.planner = new MatchQueryPlanner();
                break;
            default:
                break;
        }
        this.queryPlan = this.planner.plan(query);
        return this.queryPlan;
    }
}

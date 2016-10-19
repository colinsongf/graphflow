package ca.waterloo.dsg.graphflow.queryplanner;

import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;
import ca.waterloo.dsg.graphflow.queryplanner.operations.create.CreateQueryPlanner;
import ca.waterloo.dsg.graphflow.queryplanner.operations.delete.DeleteQueryPlanner;
import ca.waterloo.dsg.graphflow.queryplanner.operations.match.MatchQueryPlanner;

public class QueryPlanner {

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

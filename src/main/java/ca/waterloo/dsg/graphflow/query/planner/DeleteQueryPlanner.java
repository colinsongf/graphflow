package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.DeleteQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

public class DeleteQueryPlanner implements IQueryPlanner {

    DeleteQueryPlan deleteQueryPlan;

    @Override
    public QueryPlan plan(StructuredQuery query) {
        this.deleteQueryPlan = new DeleteQueryPlan(query);
        return this.deleteQueryPlan;
    }
}

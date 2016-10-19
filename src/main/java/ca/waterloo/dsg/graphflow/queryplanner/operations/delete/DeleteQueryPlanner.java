package ca.waterloo.dsg.graphflow.queryplanner.operations.delete;

import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;
import ca.waterloo.dsg.graphflow.queryplanner.IQueryPlanner;
import ca.waterloo.dsg.graphflow.queryplanner.QueryPlan;

public class DeleteQueryPlanner implements IQueryPlanner {

    DeleteQueryPlan deleteQueryPlan;

    @Override
    public QueryPlan plan(StructuredQuery query) {
        this.deleteQueryPlan = new DeleteQueryPlan(query);
        return this.deleteQueryPlan;
    }
}

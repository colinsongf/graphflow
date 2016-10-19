package ca.waterloo.dsg.graphflow.queryplanner.operations.create;

import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;
import ca.waterloo.dsg.graphflow.queryplanner.IQueryPlanner;
import ca.waterloo.dsg.graphflow.queryplanner.QueryPlan;

public class CreateQueryPlanner implements IQueryPlanner {

    CreateQueryPlan createQueryPlan;

    @Override
    public QueryPlan plan(StructuredQuery query) {
        this.createQueryPlan = new CreateQueryPlan(query);
        return this.createQueryPlan;
    }
}

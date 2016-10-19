package ca.waterloo.dsg.graphflow.queryplanner.operations.match;

import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;
import ca.waterloo.dsg.graphflow.queryplanner.IQueryPlanner;
import ca.waterloo.dsg.graphflow.queryplanner.QueryPlan;

public class MatchQueryPlanner implements IQueryPlanner {

    MatchQueryPlan matchQueryPlan;

    public QueryPlan plan(StructuredQuery query) {
        this.matchQueryPlan = new MatchQueryPlan(query);
        return this.matchQueryPlan;
    }
}

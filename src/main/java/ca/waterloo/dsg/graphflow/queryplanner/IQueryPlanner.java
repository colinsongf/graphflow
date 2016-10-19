package ca.waterloo.dsg.graphflow.queryplanner;

import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;

public interface IQueryPlanner {
    QueryPlan plan(StructuredQuery query);
}

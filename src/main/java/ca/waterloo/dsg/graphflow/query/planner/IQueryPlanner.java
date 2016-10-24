package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

/**
 * Interface definition for creating {@code QueryPlanner}s for different operations.
 */
public interface IQueryPlanner {
    QueryPlan plan(StructuredQuery query);
}

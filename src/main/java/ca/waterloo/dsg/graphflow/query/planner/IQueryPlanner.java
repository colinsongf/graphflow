package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.IQueryPlan;

/**
 * Interface definition for creating {@code QueryPlanner}s for different operations.
 */
public interface IQueryPlanner {
    IQueryPlan plan(StructuredQuery query);
}

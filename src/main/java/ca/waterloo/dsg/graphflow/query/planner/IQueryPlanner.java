package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.plans.IQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;

/**
 * Interface definition for creating {@code QueryPlanner}s for different operations.
 */
public interface IQueryPlanner {
    IQueryPlan plan(StructuredQuery query);
}

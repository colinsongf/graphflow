package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;

/**
 * Interface definition for creating {@code AbstractQueryPlanner}s for different operations.
 */
public abstract class AbstractQueryPlanner {

    StructuredQuery structuredQuery;

    public AbstractQueryPlanner(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    abstract QueryPlan plan();
}

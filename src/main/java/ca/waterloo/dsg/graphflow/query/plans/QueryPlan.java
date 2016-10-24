package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;

/**
 * Abstract class representing base operations for creating operation plans.
 */
public abstract class QueryPlan {

    protected StructuredQuery structuredQuery;

    public QueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    public abstract String execute(Graph graph);
}

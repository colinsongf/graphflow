package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;

public abstract class QueryPlan {

    protected StructuredQuery structuredQuery;

    public QueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    public abstract String execute(Graph graph);
}

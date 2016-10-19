package ca.waterloo.dsg.graphflow.queryplanner;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;

public abstract class QueryPlan {

    protected String message;
    protected StructuredQuery structuredQuery;

    public abstract String execute(Graph graph);

}

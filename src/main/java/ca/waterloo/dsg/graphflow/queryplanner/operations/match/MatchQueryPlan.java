package ca.waterloo.dsg.graphflow.queryplanner.operations.match;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;
import ca.waterloo.dsg.graphflow.queryplanner.QueryPlan;

public class MatchQueryPlan extends QueryPlan {

    public MatchQueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    @Override
    public String execute(Graph g) {
        message = g.getGraphString();
        return message;
    }
}

package ca.waterloo.dsg.graphflow.queryplanner.operations.delete;

import ca.waterloo.dsg.graphflow.queryparser.Edge;
import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;
import ca.waterloo.dsg.graphflow.queryplanner.QueryPlan;

public class DeleteQueryPlan extends QueryPlan {

    public DeleteQueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    @Override
    public String execute(Graph g) {
        for (Edge edge : structuredQuery.getEdges()) {
            g.deleteEdge(edge);
        }
        this.message = structuredQuery.getEdges().size() + " edges deleted.";
        return message;
    }
}

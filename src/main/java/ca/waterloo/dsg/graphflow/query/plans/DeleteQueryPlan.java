package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.Edge;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;

public class DeleteQueryPlan extends QueryPlan {

    public DeleteQueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    @Override
    public String execute(Graph graph) {
        for (Edge edge : structuredQuery.getEdges()) {
            graph.deleteEdge(edge);
        }
        this.message = structuredQuery.getEdges().size() + " edges deleted.";
        return message;
    }
}

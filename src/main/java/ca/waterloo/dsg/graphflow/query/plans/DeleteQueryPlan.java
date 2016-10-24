package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.Edge;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;

/**
 * Class representing plan for a DELETE operation.
 */
public class DeleteQueryPlan extends QueryPlan {

    public DeleteQueryPlan(StructuredQuery structuredQuery) {
        super(structuredQuery);
    }

    @Override
    public String execute(Graph graph) {
        for (Edge edge : structuredQuery.getEdges()) {
            graph.deleteEdge(edge);
        }
        return structuredQuery.getEdges().size() + " edges deleted.";
    }
}

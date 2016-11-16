package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;

/**
 * Class representing plan for a DELETE operation.
 */
public class DeleteQueryPlan implements QueryPlan {

    private StructuredQuery structuredQuery;

    public DeleteQueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    @Override
    public String execute(Graph graph) {
        for (StructuredQueryEdge structuredQueryEdge : structuredQuery.getStructuredQueryEdges()) {
            graph.deleteEdgeTemporarily(Integer.parseInt(structuredQueryEdge.getFromVertex()),
                Integer.parseInt(structuredQueryEdge.getToVertex()));
            // TODO: execute delta generic join
            graph.finalizeChanges();
        }
        return structuredQuery.getStructuredQueryEdges().size() + " edges deleted.";
    }
}

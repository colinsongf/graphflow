package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.StructuredQueryEdge;

/**
 * Class representing plan for a CREATE operation.
 */
public class CreateQueryPlan implements IQueryPlan {

    private StructuredQuery structuredQuery;

    public CreateQueryPlan(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    @Override
    public String execute(Graph graph) {
        for (StructuredQueryEdge structuredQueryEdge : structuredQuery.getStructuredQueryEdges()) {
            graph.addEdge(structuredQueryEdge);
        }
        return structuredQuery.getStructuredQueryEdges().size() + " edges created.";
    }
}

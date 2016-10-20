package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.Edge;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;

public class CreateQueryPlan extends QueryPlan {

    public CreateQueryPlan(StructuredQuery structuredQuery) {
        super(structuredQuery);
    }

    @Override
    public String execute(Graph graph) {
        for (Edge edge : structuredQuery.getEdges()) {
            graph.addEdge(edge);
        }
        return structuredQuery.getEdges().size() + " edges created.";
    }
}

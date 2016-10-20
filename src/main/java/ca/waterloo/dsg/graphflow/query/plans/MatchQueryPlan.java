package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;

public class MatchQueryPlan extends QueryPlan {

    public MatchQueryPlan(StructuredQuery structuredQuery) {
        super(structuredQuery);
    }

    @Override
    public String execute(Graph graph) {
        //TODO: perform actual generic join query
        return graph.getGraphString();
    }
}

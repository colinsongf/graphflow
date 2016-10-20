package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

public class QueryProcessor {

    private Graph graph;

    public QueryProcessor() {
        this.graph = new Graph();
    }

    public String process(String query) {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(query);

        if (structuredQuery.getOperation() == StructuredQuery.Operation.ERROR) {
            return "ERROR parsing: " + structuredQuery.getErrorMessage();
        }

        QueryPlan queryPlan = new QueryPlanBuilder().plan(structuredQuery);
        return queryPlan.execute(graph);
    }
}

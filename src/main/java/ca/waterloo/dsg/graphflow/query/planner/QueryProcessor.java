package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;

public class QueryProcessor {

    Graph graph;
    String result;

    public QueryProcessor() {
        this.graph = new Graph();
    }

    public String process(String query) {
        StructuredQueryParser parser = new StructuredQueryParser();
        StructuredQuery structuredQuery = parser.parse(query);

        if (structuredQuery.getOperation() == StructuredQuery.Operation.ERROR) {
            this.result = "ERROR parsing: " + structuredQuery.getErrorMessage();
            return result;
        }

        QueryPlanBuilder queryPlanner = new QueryPlanBuilder();
        QueryPlan queryPlan = queryPlanner.plan(structuredQuery);

        if (queryPlan == null) {
            this.result = "ERROR executing query: No appropriate operation found";
            return result;
        }

        result = queryPlan.execute(this.graph);

        return result;
    }
}

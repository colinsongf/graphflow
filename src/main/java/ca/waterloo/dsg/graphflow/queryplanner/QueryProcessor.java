package ca.waterloo.dsg.graphflow.queryplanner;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.queryparser.StructuredQuery;
import ca.waterloo.dsg.graphflow.queryparser.StructuredQueryParser;

public class QueryProcessor {

    Graph graph;
    String result;

    public QueryProcessor() {
        this.graph = new Graph();
    }

    public String process(String query) {
        StructuredQueryParser parser = new StructuredQueryParser();
        StructuredQuery structuredQuery = parser.parse(query);

        if(structuredQuery.getOperation() == StructuredQuery.Operation.ERROR) {
            this.result = "ERROR parsing: " + structuredQuery.getErrorMessage();
            return result;
        }

        QueryPlanner queryPlanner = new QueryPlanner();
        QueryPlan queryPlan = queryPlanner.plan(structuredQuery);

        if(queryPlan == null) {
            this.result = "ERROR executing query: No appropriate operation found";
            return result;
        }

        result = queryPlan.execute(this.graph);

        return result;
    }
}

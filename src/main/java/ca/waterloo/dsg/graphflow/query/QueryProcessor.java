package ca.waterloo.dsg.graphflow.query;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.QueryPlanBuilder;
import ca.waterloo.dsg.graphflow.query.plans.IQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * Class to accept incoming queries from the gRPC server, process them and return the results.
 */
public class QueryProcessor {

    private Graph graph;

    public QueryProcessor() {
        this.graph = new Graph();
    }

    public String process(String query) {
        StructuredQuery structuredQuery = null;
        try {
            structuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            return "ERROR parsing: " + e.getMessage();
        }

        IQueryPlan queryPlan = new QueryPlanBuilder().plan(structuredQuery);
        return queryPlan.execute(graph);
    }
}

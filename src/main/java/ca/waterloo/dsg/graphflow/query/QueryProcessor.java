package ca.waterloo.dsg.graphflow.query;

import ca.waterloo.dsg.graphflow.exceptions.IncorrectDataTypeException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchPropertyKeyException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchTypeException;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.FileOutputSink;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.executors.ShortestPathExecutor;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.ContinuousMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.planner.CreateQueryPlanner;
import ca.waterloo.dsg.graphflow.query.planner.DeleteQueryPlanner;
import ca.waterloo.dsg.graphflow.query.planner.OneTimeMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.CreateQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.DeleteQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.server.GraphflowServer;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Class to accept incoming queries from the gRPC server, process them and return the results.
 */
public class QueryProcessor {

    private static final Logger logger = LogManager.getLogger(QueryProcessor.class);

    private static String TMP_DIRECTORY = "/tmp/";
    private Graph graph = new Graph();

    public QueryProcessor() {
        ShortestPathExecutor.getInstance().init(graph);
    }

    /**
     * Executes a string query by converting it into a {@link StructuredQuery}, creating the
     * corresponding {@link QueryPlan}, and executing the plan.
     *
     * @param query The {@code String} input query.
     * @return The result of the query as a {@code String}.
     */
    public String process(String query) {
        StructuredQuery structuredQuery;
        try {
            structuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            return "ERROR parsing: " + e.getMessage();
        }
        switch (structuredQuery.getQueryOperation()) {
            case CREATE:
                return handleCreateQuery(structuredQuery);
            case DELETE:
                return handleDeleteQuery(structuredQuery);
            case MATCH:
                return handleMatchQuery(structuredQuery);
            case CONTINUOUS_MATCH:
                return handleContinuousMatchQuery(structuredQuery);
            default:
                return "ERROR: the operation '" + structuredQuery.getQueryOperation() +
                    "' is not defined.";
        }
    }

    private String handleCreateQuery(StructuredQuery structuredQuery) {
        OutputSink outputSink = new InMemoryOutputSink();
        try {
            ((CreateQueryPlan) new CreateQueryPlanner(structuredQuery).plan()).execute(graph,
                outputSink);
        }
        catch (IncorrectDataTypeException e) {
            logger.debug(e.getMessage());
            outputSink.append("ERROR: " + e.getMessage());
       }
        return outputSink.toString();
    }

    private String handleDeleteQuery(StructuredQuery structuredQuery) {
        OutputSink outputSink = new InMemoryOutputSink();
        ((DeleteQueryPlan) new DeleteQueryPlanner(structuredQuery).plan()).execute(graph,
            outputSink);
        return outputSink.toString();
    }

    private String handleMatchQuery(StructuredQuery structuredQuery) {
        OutputSink outputSink = new InMemoryOutputSink();
        try {
            ((OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(structuredQuery).plan()).execute(
                graph, outputSink);
        } catch (IncorrectDataTypeException | NoSuchPropertyKeyException | NoSuchTypeException e) {
            logger.debug(e.getMessage());
            outputSink.append("ERROR: " + e.getMessage());
        }
        return (0 == outputSink.toString().length()) ? "{}" : outputSink.toString();
    }

    private String handleContinuousMatchQuery(StructuredQuery structuredQuery) {
        String fileName = "continuous_match_query_" + structuredQuery
            .getContinuousMatchOutputLocation();
        OutputSink outputSink;
        try {
            outputSink = new FileOutputSink(new File(TMP_DIRECTORY + fileName));
        } catch (IOException e) {
            return "IO ERROR for file: " + fileName + ".";
        }
        try {
            ContinuousMatchQueryExecutor.getInstance().addContinuousMatchQueryPlan(
                (ContinuousMatchQueryPlan) new ContinuousMatchQueryPlanner(structuredQuery,
                    outputSink).plan());
        } catch (IncorrectDataTypeException | NoSuchPropertyKeyException | NoSuchTypeException e) {
            logger.debug(e.getMessage());
            return "ERROR: The CONTINUOUS MATCH query could not be registered. " + e.getMessage();
        }
        return "The CONTINUOUS MATCH query has been added to the list of continuous queries.";
    }
}

package ca.waterloo.dsg.graphflow.query;

import ca.waterloo.dsg.graphflow.exceptions.IncorrectDataTypeException;
import ca.waterloo.dsg.graphflow.exceptions.IncorrectVertexTypeException;
import ca.waterloo.dsg.graphflow.exceptions.MalformedMatchQueryException;
import ca.waterloo.dsg.graphflow.exceptions.MalformedReturnClauseException;
import ca.waterloo.dsg.graphflow.exceptions.MalformedWhereClauseException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchPropertyKeyException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchTypeException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchVertexIDException;
import ca.waterloo.dsg.graphflow.exceptions.SerializationDeserializationException;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.executors.ShortestPathExecutor;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.query.operator.FileOutputSink;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.ContinuousMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.planner.CreateQueryPlanner;
import ca.waterloo.dsg.graphflow.query.planner.DeleteQueryPlanner;
import ca.waterloo.dsg.graphflow.query.planner.OneTimeMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.query.planner.ShortestPathPlanner;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.CreateQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.DeleteQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.ShortestPathPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.IOUtils;
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

    private static final String TMP_DIRECTORY = "/tmp/";

    /**
     * Executes a string query by converting it into a {@link StructuredQuery}, creating the
     * corresponding {@link QueryPlan}, and executing the plan.
     *
     * @param query The {@code String} input query.
     * @return The result of the query as a {@code String}.
     */
    public String process(String query) {
        long beginTime = System.nanoTime();
        String output;
        StructuredQuery structuredQuery;
        try {
            structuredQuery = new StructuredQueryParser().parse(query);
        } catch (ParseCancellationException e) {
            return "ERROR parsing: " + e.getMessage();
        }
        switch (structuredQuery.getQueryOperation()) {
            case CREATE:
                output = handleCreateQuery(structuredQuery);
                break;
            case DELETE:
                output = handleDeleteQuery(structuredQuery);
                break;
            case MATCH:
                output = handleMatchQuery(structuredQuery);
                break;
            case CONTINUOUS_MATCH:
                output = handleContinuousMatchQuery(structuredQuery);
                break;
            case SHORTEST_PATH:
                output = handleShortestPathQuery(structuredQuery);
                break;
            case LOAD_GRAPH:
                output = handleLoadGraphQuery(structuredQuery);
                break;
            case SAVE_GRAPH:
                output = handleSaveGraphQuery(structuredQuery);
                break;
            default:
                return "ERROR: the operation '" + structuredQuery.getQueryOperation() +
                    "' is not defined.";
        }
        output += String.format("\nQuery executed in %.3f ms.", IOUtils.getElapsedTimeInMillis(
            beginTime));
        return output;
    }

    private String handleSaveGraphQuery(StructuredQuery structuredQuery) {
        try {
            GraphDBState.serialize(structuredQuery.getFilePath());
            return String.format("Graph saved to directory '%s'.", structuredQuery.getFilePath());
        } catch (SerializationDeserializationException e) {
            return String.format("Error saving graph state to '%s'. Please check the Graphflow " +
                "server logs for details.", structuredQuery.getFilePath());
        }
    }

    private String handleLoadGraphQuery(StructuredQuery structuredQuery) {
        try {
            GraphDBState.deserialize(structuredQuery.getFilePath());
            return String.format("Graph loaded from directory '%s'.", structuredQuery.
                getFilePath());
        } catch (SerializationDeserializationException e) {
            return String.format("Error loading graph state from '%s'. Please check the Graphflow" +
                " server logs for details.", structuredQuery.getFilePath());
        }
    }

    private String handleCreateQuery(StructuredQuery structuredQuery) {
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        try {
            ((CreateQueryPlan) new CreateQueryPlanner(structuredQuery).plan()).execute(Graph.
                getInstance(), outputSink);
        } catch (IncorrectDataTypeException e) {
            logger.debug(e.getMessage());
            outputSink.append("ERROR: " + e.getMessage());
        }
        return outputSink.toString();
    }

    private String handleDeleteQuery(StructuredQuery structuredQuery) {
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        ((DeleteQueryPlan) new DeleteQueryPlanner(structuredQuery).plan()).execute(Graph.
            getInstance(), outputSink);
        return outputSink.toString();
    }

    private String handleMatchQuery(StructuredQuery structuredQuery) {
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        try {
            ((OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(structuredQuery, outputSink).
                plan()).execute(Graph.getInstance());
        } catch (IncorrectDataTypeException | IncorrectVertexTypeException |
            NoSuchPropertyKeyException | NoSuchTypeException | MalformedMatchQueryException |
            MalformedReturnClauseException | MalformedWhereClauseException |
            NoSuchVertexIDException e) {
            logger.debug(e.getMessage());
            outputSink.append("ERROR: " + e.getMessage());
        }
        return (outputSink.toString().isEmpty()) ? "{}" : outputSink.toString();
    }

    private String handleContinuousMatchQuery(StructuredQuery structuredQuery) {
        String fileName = "continuous_match_query_" + structuredQuery.
            getFilePath();
        AbstractDBOperator outputSink;
        try {
            outputSink = new FileOutputSink(new File(TMP_DIRECTORY + fileName));
        } catch (IOException e) {
            return "IO ERROR for file: " + fileName + ".";
        }
        try {
            ContinuousMatchQueryExecutor.getInstance().addContinuousMatchQueryPlan(
                (ContinuousMatchQueryPlan) new ContinuousMatchQueryPlanner(structuredQuery,
                    outputSink).plan());
        } catch (IncorrectDataTypeException | IncorrectVertexTypeException |
            NoSuchPropertyKeyException | NoSuchTypeException | MalformedMatchQueryException |
            MalformedWhereClauseException e) {
            logger.debug(e.getMessage());
            return "ERROR: The CONTINUOUS MATCH query could not be registered. " + e.getMessage();
        }
        return "The CONTINUOUS MATCH query has been added to the list of continuous queries.";
    }

    private String handleShortestPathQuery(StructuredQuery structuredQuery) {
        if (!ShortestPathExecutor.getInstance().isInitialized()) {
            ShortestPathExecutor.getInstance().init(Graph.getInstance());
        }
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        try {
            ((ShortestPathPlan) new ShortestPathPlanner(structuredQuery).plan()).execute(
                outputSink);
        } catch (NoSuchVertexIDException e) {
            return "ERROR: " + e.getMessage();
        }
        return outputSink.toString();
    }
}

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
import ca.waterloo.dsg.graphflow.graph.GraphDBState;
import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.operator.sinks.OutputSink;
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
import ca.waterloo.dsg.graphflow.server.ServerQueryString;
import ca.waterloo.dsg.graphflow.server.ServerQueryString.ReturnType;
import ca.waterloo.dsg.graphflow.util.IOUtils;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Class to accept incoming queries from the gRPC server, process them and return the results.
 */
public class QueryProcessor {

    private static final Logger logger = LogManager.getLogger(QueryProcessor.class);

    /**
     * Executes a string query by converting it into a {@link StructuredQuery}, creating the
     * corresponding {@link QueryPlan}, and executing the plan.
     *
     * @param request The {@code ServerQueryString} input request.
     *
     * @return The result of the query as a {@code String}.
     */
    public String process(ServerQueryString request) {
        String query = request.getMessage();
        ReturnType returnType = request.getReturnType();
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
            case EXPLAIN:
                output = handleExplainMatchQuery(structuredQuery, returnType);
                break;
            case CONTINUOUS_EXPLAIN:
                output = handleExplainContinuousMatchQuery(structuredQuery, returnType);
                break;
            default:
                return "ERROR: the operation '" + structuredQuery.getQueryOperation() +
                    "' is not defined.";
        }

        if (ReturnType.TEXT == returnType) {
            output += String.format("\nQuery executed in %.3f ms.", IOUtils.getElapsedTimeInMillis(
                beginTime));
        }
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
        OutputSink inMemoryOutputSink = new InMemoryOutputSink();
        try {
            ((CreateQueryPlan) new CreateQueryPlanner(structuredQuery).plan()).execute(
                inMemoryOutputSink);
        } catch (IncorrectDataTypeException e) {
            logger.debug(e.getMessage());
            inMemoryOutputSink.append("ERROR: " + e.getMessage());
        }
        return inMemoryOutputSink.toString();
    }

    private String handleDeleteQuery(StructuredQuery structuredQuery) {
        OutputSink inMemoryOutputSink = new InMemoryOutputSink();
        ((DeleteQueryPlan) new DeleteQueryPlanner(structuredQuery).plan()).execute(
            inMemoryOutputSink);
        return inMemoryOutputSink.toString();
    }

    private String handleMatchQuery(StructuredQuery structuredQuery) {
        OutputSink inMemoryOutputSink = new InMemoryOutputSink();
        try {
            ((OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(structuredQuery,
                inMemoryOutputSink).plan()).execute();
        } catch (IncorrectDataTypeException | IncorrectVertexTypeException |
            NoSuchPropertyKeyException | NoSuchTypeException | MalformedMatchQueryException |
            MalformedReturnClauseException | MalformedWhereClauseException |
            NoSuchVertexIDException e) {
            logger.debug(e.getMessage());
            inMemoryOutputSink.append("ERROR: " + e.getMessage());
        }
        return (inMemoryOutputSink.toString().isEmpty()) ? "{}" : inMemoryOutputSink.toString();
    }

    private String handleContinuousMatchQuery(StructuredQuery structuredQuery) {
        try {
            ContinuousMatchQueryExecutor.getInstance().addContinuousMatchQueryPlan(
                (ContinuousMatchQueryPlan) new ContinuousMatchQueryPlanner(structuredQuery).plan());
        } catch (IncorrectDataTypeException | IncorrectVertexTypeException |
            NoSuchPropertyKeyException | NoSuchTypeException | MalformedMatchQueryException |
            MalformedWhereClauseException | IOException | ClassNotFoundException |
            ClassCastException | NullPointerException | InstantiationException |
            IllegalAccessException e) {
            logger.debug(e.getMessage());
            return "ERROR: The CONTINUOUS MATCH query could not be registered. " + e.getMessage();
        }
        return "The CONTINUOUS MATCH query has been added to the list of continuous queries.";
    }

    private String handleShortestPathQuery(StructuredQuery structuredQuery) {
        OutputSink inMemoryOutputSink = new InMemoryOutputSink();
        try {
            ((ShortestPathPlan) new ShortestPathPlanner(structuredQuery).plan()).execute(
                inMemoryOutputSink);
        } catch (NoSuchVertexIDException e) {
            return "ERROR: " + e.getMessage();
        }
        return inMemoryOutputSink.toString();
    }

    private String handleExplainMatchQuery(StructuredQuery structuredQuery, ReturnType returnType) {
        OutputSink inMemoryOutputSink = new InMemoryOutputSink();
        try {
            OneTimeMatchQueryPlan oneTimeMatchQueryPlan =
                (OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(structuredQuery,
                    inMemoryOutputSink).plan();
            switch (returnType) {
                case TEXT:
                    return oneTimeMatchQueryPlan.getHumanReadablePlan();
                case JSON:
                    return oneTimeMatchQueryPlan.getJsonPlan().toString();
                default:
                    return "INTERNAL ERROR: unrecognized return type of query result";
            }
        } catch (IncorrectDataTypeException | NoSuchPropertyKeyException | NoSuchTypeException e) {
            logger.debug(e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    private String handleExplainContinuousMatchQuery(StructuredQuery structuredQuery,
        ReturnType returnType) {
        try {
            ContinuousMatchQueryPlan continuousMatchQueryPlan = (ContinuousMatchQueryPlan)
                new ContinuousMatchQueryPlanner(structuredQuery).plan();
            switch (returnType) {
                case TEXT:
                    return continuousMatchQueryPlan.getHumanReadablePlan();
                case JSON:
                    return continuousMatchQueryPlan.getJsonPlan().toString();
                default:
                    return "INTERNAL ERROR: unrecognized return type of query result.";
            }
        } catch (IncorrectDataTypeException | NoSuchPropertyKeyException | NoSuchTypeException |
            IOException | ClassNotFoundException | InstantiationException |
            IllegalAccessException e) {
            logger.debug(e.getMessage());
            return "ERROR: The CONTINUOUS MATCH query could not be planned. " + e.getMessage();
        }
    }
}

package ca.waterloo.dsg.graphflow.query;

import ca.waterloo.dsg.graphflow.exceptions.IncorrectDataTypeException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchPropertyKeyException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchTypeException;
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
import ca.waterloo.dsg.graphflow.query.plans.*;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

/**
 * Class to accept incoming queries from the gRPC server, process them and return the results.
 */
public class QueryProcessor {

    private static final Logger logger = LogManager.getLogger(QueryProcessor.class);

    private static String TMP_DIRECTORY = "/tmp/";
    private Graph graph = Graph.getInstance();

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
            case SHORTEST_PATH:
                return handleShortestPathQuery(structuredQuery);
            case LOAD_GRAPH:
                return handleLoadGraphQuery(structuredQuery);
            case SAVE_GRAPH:
                return handleSaveGraphQuery(structuredQuery);
            default:
                return "ERROR: the operation '" + structuredQuery.getQueryOperation() +
                    "' is not defined.";
        }
    }

    private String handleSaveGraphQuery(StructuredQuery structuredQuery) {
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(
                new FileOutputStream(structuredQuery.getFilePath())));
            GraphDBState.serialize(objectOutputStream);
            objectOutputStream.close();
            return String.format("Graph saved to file '%s'", structuredQuery.getFilePath());
        } catch (IOException e) {
            return String.format("IOError for file '%s': %s", structuredQuery.getFilePath(),
                e.getMessage());
        }
    }

    private String handleLoadGraphQuery(StructuredQuery structuredQuery) {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(
                new FileInputStream(structuredQuery.getFilePath())));
            GraphDBState.deserialize(objectInputStream);
            objectInputStream.close();
            return String.format("Graph loaded from file '%s'", structuredQuery.getFilePath());
        } catch (IOException e) {
            return String.format("IOError for file '%s': %s", structuredQuery.getFilePath(), e);
        } catch (ClassNotFoundException e) {
            return String.format("ERROR: incorrect format of file '%s'", structuredQuery.
                getFilePath());
        }
    }

    private String handleCreateQuery(StructuredQuery structuredQuery) {
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        try {
            ((CreateQueryPlan) new CreateQueryPlanner(structuredQuery).plan()).execute(graph,
                outputSink);
        } catch (IncorrectDataTypeException e) {
            logger.debug(e.getMessage());
            outputSink.append("ERROR: " + e.getMessage());
        }
        return outputSink.toString();
    }

    private String handleDeleteQuery(StructuredQuery structuredQuery) {
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        ((DeleteQueryPlan) new DeleteQueryPlanner(structuredQuery).plan()).execute(graph,
            outputSink);
        return outputSink.toString();
    }

    private String handleMatchQuery(StructuredQuery structuredQuery) {
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        try {
            ((OneTimeMatchQueryPlan) new OneTimeMatchQueryPlanner(structuredQuery, outputSink).
                plan()).execute(graph);
        } catch (IncorrectDataTypeException | NoSuchPropertyKeyException | NoSuchTypeException e) {
            logger.debug(e.getMessage());
            outputSink.append("ERROR: " + e.getMessage());
        }
        return (0 == outputSink.toString().length()) ? "{}" : outputSink.toString();
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
        } catch (IncorrectDataTypeException | NoSuchPropertyKeyException | NoSuchTypeException e) {
            logger.debug(e.getMessage());
            return "ERROR: The CONTINUOUS MATCH query could not be registered. " + e.getMessage();
        }
        return "The CONTINUOUS MATCH query has been added to the list of continuous queries.";
    }

    private String handleShortestPathQuery(StructuredQuery structuredQuery) {
        if (!ShortestPathExecutor.getInstance().isInitialized()) {
            ShortestPathExecutor.getInstance().init(graph);
        }
        AbstractDBOperator outputSink = new InMemoryOutputSink();
        ((ShortestPathPlan) new ShortestPathPlanner(structuredQuery).plan()).execute(outputSink);
        return outputSink.toString();
    }
}

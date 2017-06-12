package ca.waterloo.dsg.graphflow;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.operator.sinks.OutputSink;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput.MatchQueryResultType;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQueryParser;
import ca.waterloo.dsg.graphflow.query.planner.CreateQueryPlanner;
import ca.waterloo.dsg.graphflow.query.plans.CreateQueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate.PredicateType;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Provides utility functions for tests.
 */
public class TestUtils {

    /**
     * Creates and returns a graph initialized with the given {@code edges}, {@code edgeTypes} and
     * {@code vertexTypes}.
     *
     * @param edges The edges {e=(u,v)} of the graph.
     * @param edgeTypes The type of each edge e.
     * @param vertexTypes The types {@code (t1, t2)} where t1 is the type of source vertex u and t2
     * is the type of destination vertex v.
     * @return Graph The initialized graph.
     */
    public static Graph initializeGraphPermanently(int[][] edges, short[] edgeTypes,
        short[][] vertexTypes) {
        Graph graph = initializeGraphTemporarily(edges, edgeTypes, vertexTypes);
        graph.finalizeChanges();
        return graph;
    }

    /**
     * Creates and returns a graph with with the given {@code edges}, {@code edgeTypes} and {@code
     * vertexTypes} added temporarily.
     *
     * @param edges The edges {e=(u,v)} of the graph.
     * @param edgeTypes The type of each edge e.
     * @param vertexTypes The types {@code (t1, t2)} where t1 is the type of source vertex u and t2
     * is the type of destination vertex v.
     * @return Graph The graph initialized with temporary edges.
     */
    public static Graph initializeGraphTemporarily(int[][] edges, short[] edgeTypes,
        short[][] vertexTypes) {
        Graph graph = Graph.getInstance();
        for (int i = 0; i < edges.length; i++) {
            graph.addEdgeTemporarily(edges[i][0], edges[i][1], vertexTypes[i][0],
                vertexTypes[i][1], null /* no fromVertex properties */, null /* no toVertex
                properties */, edgeTypes[i], null /* no edge properties */);
        }
        return graph;
    }

    /**
     * Adds a set of edges to the given {@code graph} by executing the given {@code createQuery}.
     *
     * @param graph The {@link Graph} instance to which the edges should be added.
     * @param createQuery The {@code String} create query to be executed.
     */
    public static void createEdgesPermanently(Graph graph, String createQuery) {
        createEdgesTemporarily(graph, createQuery);
        graph.finalizeChanges();
    }

    /**
     * Adds a set of edges to the given {@code graph} temporarily by executing the given {@code
     * createQuery}.
     *
     * @param graph The {@link Graph} instance to which the edges should be added.
     * @param createQuery The {@code String} create query to be executed.
     */
    public static void createEdgesTemporarily(Graph graph, String createQuery) {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(createQuery);
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            int fromVertex = Integer.parseInt(queryRelation.getFromQueryVariable().
                getVariableName());
            int toVertex = Integer.parseInt(queryRelation.getToQueryVariable().getVariableName());
            // Insert the types into the {@code TypeStore} if they do not already exist, and
            // get their {@code short} IDs. An exception in the above {@code parseInt()} calls
            // will prevent the insertion of any new type to the {@code TypeStore}.
            short fromVertexTypeId = TypeAndPropertyKeyStore.getInstance().
                mapStringTypeToShortOrInsert(queryRelation.getFromQueryVariable().
                    getVariableType());
            short toVertexTypeId = TypeAndPropertyKeyStore.getInstance().
                mapStringTypeToShortOrInsert(queryRelation.getToQueryVariable().getVariableType());
            short edgeTypeId = TypeAndPropertyKeyStore.getInstance().
                mapStringTypeToShortOrInsert(queryRelation.getRelationType());
            // Add the new edge to the graph.
            graph.addEdgeTemporarily(fromVertex, toVertex, fromVertexTypeId, toVertexTypeId, null
                /* no fromVertex properties */, null /* no toVertex properties */, edgeTypeId,
                null /* no edge properties */);
        }
    }

    /**
     * Deletes a set of edges from the given {@code graph} permanently by executing the given {@code
     * deleteQuery}.
     *
     * @param graph The {@link Graph} instance from which the edges should be deleted.
     * @param deleteQuery The {@code String} delete query to be executed.
     */
    public static void deleteEdgesPermanently(Graph graph, String deleteQuery) {
        deleteEdgesTemporarily(graph, deleteQuery);
        graph.finalizeChanges();
    }

    /**
     * Deletes a set of edges from the given {@code graph} temporarily by executing the given {@code
     * deleteQuery}.
     *
     * @param graph The {@link Graph} instance from which the edges should be deleted.
     * @param deleteQuery The {@code String} delete query to be executed.
     */
    public static void deleteEdgesTemporarily(Graph graph, String deleteQuery) {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(deleteQuery);
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            graph.deleteEdgeTemporarily(
                Integer.parseInt(queryRelation.getFromQueryVariable().getVariableName()),
                Integer.parseInt(queryRelation.getToQueryVariable().getVariableName()),
                TypeAndPropertyKeyStore.getInstance().mapStringTypeToShort(
                    queryRelation.getRelationType()));
        }
    }

    /**
     * Initializes the {@link Graph} with the given {@code createQuery}.
     *
     * @param createQuery a {@code String} representing a CREATE query which will be parsed and
     * used to initialize the {@link Graph}.
     */
    public static void initializeGraphPermanentlyWithProperties(String createQuery) {
        StructuredQuery structuredQuery = new StructuredQueryParser().parse(createQuery);
        OutputSink inMemoryOutputSink = new InMemoryOutputSink();
        ((CreateQueryPlan) new CreateQueryPlanner(structuredQuery).plan()).execute(
            inMemoryOutputSink);
    }

    /**
     * Creates a {@link QueryPropertyPredicate} using the given parameters.
     *
     * @param variable1 A {@code Pair<String, Short>} which will be the left operand in the
     * {@link QueryPropertyPredicate} to be created.
     * @param variable2 A {@code Pair<String, Short>} which will be the right operand in the
     * {@link QueryPropertyPredicate} to be created. Mutually exclusive with {@code literal}.
     * @param literal A {@code String} which will be the right operand in the
     * {@link QueryPropertyPredicate} to be created. Mutually exclusive with {@code variable1}.
     * @param comparisonOperator A {@link ComparisonOperator} specifying the comparison operator
     * of the {@link QueryPropertyPredicate} to be created.
     * @return A {@link QueryPropertyPredicate} created using the given parameters.
     */
    public static QueryPropertyPredicate createQueryPropertyPredicate(
        Pair<String, String> variable1, Pair<String, String> variable2, String literal,
        ComparisonOperator comparisonOperator) {
        QueryPropertyPredicate queryPropertyPredicate = new QueryPropertyPredicate();
        queryPropertyPredicate.setLeftOperand(variable1);
        queryPropertyPredicate.setRightOperand(variable2);
        queryPropertyPredicate.setLiteral(literal);
        queryPropertyPredicate.setComparisonOperator(comparisonOperator);
        if (null == queryPropertyPredicate.getLiteral()) {
            queryPropertyPredicate.setPredicateType(PredicateType.TWO_PROPERTY_KEY_OPERANDS);
        } else {
            queryPropertyPredicate.setPredicateType(PredicateType.PROPERTY_KEY_AND_LITERAL_OPERANDS);
        }
        return queryPropertyPredicate;
    }

    /**
     * @see TestUtils#getInMemoryOutputSinkForMotifs(Object[][], MatchQueryResultType[])
     */
    public static InMemoryOutputSink getInMemoryOutputSinkForMotifs(Object[][] results) {
        MatchQueryResultType[] matchQueryResultTypes = new MatchQueryResultType[results.length];
        Arrays.fill(matchQueryResultTypes, MatchQueryResultType.MATCHED);
        return getInMemoryOutputSinkForMotifs(results, matchQueryResultTypes);
    }

    /**
     * Creates an {@link InMemoryOutputSink} containing the results given in {@code results}.
     *
     * @param results a {@code Object[][]} where the outer array is a list of {@code Object[]}
     * result records.
     * @param matchQueryResultTypes an array of {@link MatchQueryResultType}s that will be
     * appended to the end of each record added to the {@link InMemoryOutputSink}.
     * @return an {@link InMemoryOutputSink} containing the given results.
     */
    public static InMemoryOutputSink getInMemoryOutputSinkForMotifs(Object[][] results,
        MatchQueryResultType[] matchQueryResultTypes) {
        InMemoryOutputSink inMemoryOutputSink = new InMemoryOutputSink();
        StringJoiner joiner;
        for (int i = 0; i < results.length; i++) {
            joiner = new StringJoiner(" ");
            for (Object element : results[i]) {
                joiner.add(element.toString());
            }
            inMemoryOutputSink.append(joiner.toString());
        }
        return inMemoryOutputSink;
    }

    public static String getExpectedContentOfOutputFileSink(int[][] results,
        MatchQueryResultType[] matchQueryResultTypes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < results.length; ++i) {
            stringBuilder.append(Arrays.toString(results[i])).append(" ").append(
                matchQueryResultTypes[i].name());
            if (i < results.length - 1) {
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }
}

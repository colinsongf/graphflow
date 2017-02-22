package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.exceptions.MalformedMatchQueryException;
import ca.waterloo.dsg.graphflow.exceptions.MalformedReturnClauseException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchPropertyKeyException;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.query.operator.EdgeIdResolver;
import ca.waterloo.dsg.graphflow.query.operator.EdgeIdResolver.SourceDestinationIndexAndType;
import ca.waterloo.dsg.graphflow.query.operator.Projection;
import ca.waterloo.dsg.graphflow.query.operator.PropertyResolver;
import ca.waterloo.dsg.graphflow.query.operator.PropertyResolver.EdgeOrVertexPropertyIndices;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryGraph;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.antlr.v4.runtime.misc.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Create a {@code QueryPlan} for the MATCH operation.
 */
public class OneTimeMatchQueryPlanner extends AbstractQueryPlanner {

    private static final String UNDEFINED_VARIABLE_ERROR_MESSAGE = "Return statement contains "
        + "variables that are not defined in the MATCH clause.";
    private static final Logger logger = LogManager.getLogger(OneTimeMatchQueryPlanner.class);
    protected QueryGraph queryGraph = new QueryGraph();
    protected AbstractDBOperator outputSink;
    private TypeAndPropertyKeyStore typeAndPropertyKeyStore = TypeAndPropertyKeyStore.getInstance();

    /**
     * @param structuredQuery query to plan.
     * @param outputSink output sink to be added to the final operator in the plan.
     */
    public OneTimeMatchQueryPlanner(StructuredQuery structuredQuery,
        AbstractDBOperator outputSink) {
        super(structuredQuery);
        this.outputSink = outputSink;
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            TypeAndPropertyKeyStore.getInstance().assertAllKeyDataTypesMatchPreviousDeclarations(
                queryRelation.getRelationProperties());
            TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortAndAssertTypeExists(
                queryRelation.getRelationType());
            queryGraph.addRelation(queryRelation);
        }
        checkReturnVariablesAndPropertiesAreWellFormed();
        checkEdgeVariablesAreDistinctFromVertexVariables();
    }

    private void checkEdgeVariablesAreDistinctFromVertexVariables() {
        Set<String> variableNames = queryGraph.getAllVariables();
        for (String relationName : queryGraph.getAllRelationNames()) {
            if (variableNames.contains(relationName)) {
                throw new MalformedMatchQueryException("Edge variable: " + relationName +
                    " has also been defined as a vertex variable in the query.");
            }
        }
    }

    private void checkReturnVariablesAndPropertiesAreWellFormed() {
        for (String variable : structuredQuery.getReturnVariables()) {
            if (!queryGraph.getAllVariables().contains(variable) &&
                !queryGraph.getAllRelationNames().contains(variable)) {
                throw new MalformedReturnClauseException(UNDEFINED_VARIABLE_ERROR_MESSAGE);
            }
        }

        String variable, propertyKey;
        for (Pair<String, String> variablePropertyPair :
            structuredQuery.getReturnVariablePropertyPairs()) {
            variable = variablePropertyPair.a;
            if (!queryGraph.getAllVariables().contains(variable) &&
                !queryGraph.getAllRelationNames().contains(variable)) {
                throw new MalformedReturnClauseException(UNDEFINED_VARIABLE_ERROR_MESSAGE);
            }
            propertyKey = variablePropertyPair.b;
            if (!typeAndPropertyKeyStore.isPropertyDefined(propertyKey)) {
                throw new NoSuchPropertyKeyException(propertyKey);
            }
        }
    }

    /**
     * Selects the order of the rest of the variables, considering the following properties:
     * 1) Select the variable with highest number of connections to the already covered
     * variables.
     * 2) Break tie from (1) by selecting the variable with highest degree.
     * 3) Break tie from (2) by selecting the variable with lowest lexicographical rank.
     */
    protected void orderRemainingVariables(List<String> orderedVariables) {
        int initialSize = orderedVariables.size();
        Set<String> visitedVariables = new HashSet<>();
        visitedVariables.addAll(orderedVariables);
        for (int i = 0; i < queryGraph.getTotalNumberOfVariables() - initialSize; i++) {
            String selectedVariable = "";
            int highestConnectionsCount = -1;
            int highestDegreeCount = -1;
            for (String coveredVariable : orderedVariables) {
                // Loop for all neighboring vertices of the already covered vertices.
                for (String neighborVariable : queryGraph.getAllNeighborVariables(
                    coveredVariable)) {
                    // Skip vertices which have already been covered.
                    if (visitedVariables.contains(neighborVariable)) {
                        continue;
                    }
                    int variableDegree = queryGraph.getNumberOfAdjacentRelations(neighborVariable);
                    // Calculate the number of connections of the new variable to the already
                    // covered vertices.
                    int connectionsCount = 0;
                    for (String alreadyCoveredVariable : orderedVariables) {
                        if (queryGraph.containsRelation(neighborVariable, alreadyCoveredVariable)) {
                            connectionsCount++;
                        }
                    }
                    // See if the new {@code neighbourVariable} should be chosen first.
                    if ((connectionsCount > highestConnectionsCount)) {
                        // Rule (1).
                        selectedVariable = neighborVariable;
                        highestDegreeCount = variableDegree;
                        highestConnectionsCount = connectionsCount;
                    } else if (connectionsCount == highestConnectionsCount) {
                        if (variableDegree > highestDegreeCount) {
                            // Rule (2).
                            selectedVariable = neighborVariable;
                            highestDegreeCount = variableDegree;
                            highestConnectionsCount = connectionsCount;
                        } else if ((variableDegree == highestDegreeCount) &&
                            (neighborVariable.compareTo(selectedVariable) < 0)) {
                            // Rule (3).
                            selectedVariable = neighborVariable;
                            highestDegreeCount = variableDegree;
                            highestConnectionsCount = connectionsCount;
                        }
                    }
                }
            }
            orderedVariables.add(selectedVariable);
            visitedVariables.add(selectedVariable);
        }
    }

    /**
     * Creates a one time {@code MATCH} query plan for the given {@code structuredQuery}.
     *
     * @return A {@link QueryPlan} encapsulating an {@link OneTimeMatchQueryPlan}.
     */
    public QueryPlan plan() {
        OneTimeMatchQueryPlan oneTimeMatchQueryPlan = new OneTimeMatchQueryPlan();
        List<String> orderedVariables = new ArrayList<>();
        /*
          Find the first variable, considering the following properties:
          1) Select the variable with the highest degree.
          2) Break tie from (1) by selecting the variable with the lowest lexicographical rank.
         */
        int highestDegreeCount = -1;
        String variableWithHighestDegree = "";
        for (String variable : queryGraph.getAllVariables()) {
            int variableDegree = queryGraph.getNumberOfAdjacentRelations(variable);
            if (variableDegree > highestDegreeCount) {
                // Rule (1).
                highestDegreeCount = variableDegree;
                variableWithHighestDegree = variable;
            } else if ((variableDegree == highestDegreeCount) &&
                (variable.compareTo(variableWithHighestDegree) < 0)) {
                // Rule (2).
                highestDegreeCount = variableDegree;
                variableWithHighestDegree = variable;
            }
        }
        orderedVariables.add(variableWithHighestDegree);
        // Order the rest of the variables.
        orderRemainingVariables(orderedVariables);
        // Finally, create the plan.
        // Start from the second variable to create the first stage.
        for (int i = 1; i < orderedVariables.size(); i++) {
            String variableForCurrentStage = orderedVariables.get(i);
            ArrayList<GenericJoinIntersectionRule> stage = new ArrayList<>();
            // Loop across all variables covered in the previous stages.
            for (int j = 0; j < i; j++) {
                String variableFromPreviousStage = orderedVariables.get(j);
                if (queryGraph.containsRelation(variableFromPreviousStage,
                    variableForCurrentStage)) {
                    for (QueryRelation queryRelation : queryGraph.getAdjacentRelations(
                        variableFromPreviousStage, variableForCurrentStage)) {
                        Direction direction = queryRelation.getFromQueryVariable().
                            getVariableName().equals(variableFromPreviousStage) ?
                            Direction.FORWARD : Direction.BACKWARD;
                        stage.add(new GenericJoinIntersectionRule(j, direction,
                            TypeAndPropertyKeyStore.getInstance().mapStringTypeToShort(
                                queryRelation.getRelationType()), TypeAndPropertyKeyStore.
                            getInstance().mapStringPropertiesToShortAndDataType(queryRelation.
                            getRelationProperties())));
                    }
                }
            }
            oneTimeMatchQueryPlan.addStage(stage);
        }

        oneTimeMatchQueryPlan.setNextOperator(getNextOperator(orderedVariables));
        logger.info("**********Printing OneTimeMatchQueryPlan**********");
        logger.info("Plan: \n" + oneTimeMatchQueryPlan.getHumanReadablePlan());
        return oneTimeMatchQueryPlan;
    }

    /**
     * Adds to {@link OneTimeMatchQueryPlan} the next plan which can be one of the following. The
     * op1->op2 below indicates that operator op1 appends results to operator op2.
     * <ul>
     * <li> {@link PropertyResolver}->{@link #outputSink}. (when no RETURN statement is specified).
     * <li> {@link Projection}->{@link PropertyResolver}->{@link #outputSink}.
     * <li> {@link EdgeIdResolver}->{@link Projection}->{@link PropertyResolver}->
     * {@link #outputSink}.
     * </ul>
     */
    private AbstractDBOperator getNextOperator(
        List<String> orderedVertexVariablesBeforeProjection) {
        // If there is no RETURN clause specified, we append a PropertyResolver->OutputSink to
        // GJExecutor. The PropertyResolver only returns the ID of each vertex matched.
        if (structuredQuery.getReturnVariables().isEmpty() &&
            structuredQuery.getReturnVariablePropertyPairs().isEmpty()) {
            return getIdentityPropertyResolver(orderedVertexVariablesBeforeProjection);
        } else {
            // Otherwise we first project onto the set of attributes mentioned in the RETURN
            // clause. And then resolve the variables in the RETURN clause with properties
            // (if any).

            // First compute the set of variables that will be in the output after the projection.
            // Note: We do order the variables to return implicitly below. Specifically, below we
            // disregard the order in which the user listed the variables and variableProperties
            // in the RETURN statement and first order the return variables and then
            // variableProperties.
            Pair<List<String>, List<String>> orderedVertexAndEdgeVariables =
                getOrderedVertexVariablesAfterProjectionAndOrderedEdgeVariables();
            List<String> orderedVertexVariablesAfterProjection = orderedVertexAndEdgeVariables.a;
            List<String> orderedEdgeVariables = orderedVertexAndEdgeVariables.b;

            // First construct the PropertyResolver.
            PropertyResolver propertyResolver = constructPropertyResolver(
                orderedVertexVariablesAfterProjection, orderedEdgeVariables);

            // Then construct the Projection.
            Map<String, Integer> orderedVariableIndexMapBeforeProjection =
                getOrderedVariableIndexMap(orderedVertexVariablesBeforeProjection);
            logger.info("Appending Projection->PropertyResolver->OutputSink.");
            List<Integer> vertexIndicesToProject = new ArrayList<>();
            for (String returnVariable : orderedVertexVariablesAfterProjection) {
                vertexIndicesToProject.add(orderedVariableIndexMapBeforeProjection.get(
                    returnVariable));
            }
            Projection projection = new Projection(propertyResolver, vertexIndicesToProject);

            // Finally construct the EdgeIdResolver if needed.
            if (orderedEdgeVariables.isEmpty()) {
                return projection;
            } else {
                return constructEdgeIdResolver(orderedEdgeVariables,
                    orderedVariableIndexMapBeforeProjection, projection);
            }
        }
    }

    private AbstractDBOperator constructEdgeIdResolver(List<String> orderedEdgeVariables,
        Map<String, Integer> orderedVariableIndexMapBeforeProjection, Projection projection) {
        List<SourceDestinationIndexAndType> srcDstVertexIndicesAndTypes = new ArrayList<>();
        for (int i = 0; i < orderedEdgeVariables.size(); ++i) {
            String orderedEdgeVariable = orderedEdgeVariables.get(i);
            QueryRelation queryRelation = queryGraph.getRelationFromRelationName(
                orderedEdgeVariable);
            if (null == queryRelation) {
                logger.warn("QueryRelation with given edgeVariableToResolve is null: "
                    + orderedEdgeVariable + ". This should never happen. Sanity checks"
                    + " should have caught this.");
                continue;
            }
            int sourceIndex = orderedVariableIndexMapBeforeProjection.get(
                queryRelation.getFromQueryVariable().getVariableName());
            int destinationIndex = orderedVariableIndexMapBeforeProjection.get(
                queryRelation.getToQueryVariable().getVariableName());
            srcDstVertexIndicesAndTypes.add(new SourceDestinationIndexAndType(sourceIndex,
                destinationIndex, typeAndPropertyKeyStore.mapStringTypeToShort(queryRelation.
                getRelationType())));
        }
        return new EdgeIdResolver(projection, srcDstVertexIndicesAndTypes);
    }

    private PropertyResolver constructPropertyResolver(
        List<String> orderedVertexVariablesAfterProjection, List<String> orderedEdgeVariables) {
        List<String> returnVariables = structuredQuery.getReturnVariables();
        List<Pair<String, String>> returnVariablePropertyPairs =
            structuredQuery.getReturnVariablePropertyPairs();

        Map<String, Integer> vertexVariableOrderIndexMapAfterProjection =
            getOrderedVariableIndexMap(orderedVertexVariablesAfterProjection);
        Map<String, Integer> edgeVariableOrderIndexMap = getOrderedVariableIndexMap(
            orderedEdgeVariables);
        List<EdgeOrVertexPropertyIndices> edgeOrVertexPropertyIndices = new ArrayList<>();
        for (String returnVariable : returnVariables) {
            if (vertexVariableOrderIndexMapAfterProjection.containsKey(returnVariable)) {
                edgeOrVertexPropertyIndices.add(new EdgeOrVertexPropertyIndices(
                    true /* is vertex or vertex property */,
                    vertexVariableOrderIndexMapAfterProjection.get(returnVariable),
                    (short) -1 /* No type. just return the vertex ID. */));
            } else if (edgeVariableOrderIndexMap.containsKey(returnVariable)) {
                edgeOrVertexPropertyIndices.add(new EdgeOrVertexPropertyIndices(
                    false /* is edge or edge property */,
                    edgeVariableOrderIndexMap.get(returnVariable),
                    (short) -1 /* No type. just return the vertex ID. */));
            } else {
                logger.warn("ERROR: The return variable always has to exist either in"
                    + " vertexVariableOrderIndexMapAfterProjection or "
                    + "edgeOrVertexPropertyIndices.");
            }
        }
        for (Pair<String, String> returnVariablePropertyPair : returnVariablePropertyPairs) {
            String returnVariable = returnVariablePropertyPair.a;
            if (vertexVariableOrderIndexMapAfterProjection.containsKey(returnVariable)) {
                edgeOrVertexPropertyIndices.add(new EdgeOrVertexPropertyIndices(
                    true /* is vertex or vertex property */,
                    vertexVariableOrderIndexMapAfterProjection.get(returnVariable),
                    typeAndPropertyKeyStore.mapStringPropertyKeyToShort(
                        returnVariablePropertyPair.b)));
            } else if (edgeVariableOrderIndexMap.containsKey(returnVariable)) {
                edgeOrVertexPropertyIndices.add(new EdgeOrVertexPropertyIndices(
                    false /* is edge or edge property */,
                    edgeVariableOrderIndexMap.get(returnVariable),
                    typeAndPropertyKeyStore.mapStringPropertyKeyToShort(
                        returnVariablePropertyPair.b)));
            } else {
                logger.warn("ERROR: The return variable in variablePropertyPair always has to "
                    + "exist either in vertexVariableOrderIndexMapAfterProjection or "
                    + "edgeOrVertexPropertyIndices.");
            }
        }
        return new PropertyResolver(outputSink, edgeOrVertexPropertyIndices);
    }

    private Pair<List<String>, List<String>>
    getOrderedVertexVariablesAfterProjectionAndOrderedEdgeVariables() {
        List<String> returnVariables = structuredQuery.getReturnVariables();
        List<Pair<String, String>> returnVariablePropertyPairs =
            structuredQuery.getReturnVariablePropertyPairs();

        List<String> orderedVertexVariablesAfterProjection = new ArrayList<>();
        Set<String> variablesToProjectSet = new HashSet<>();
        List<String> orderedEdgeVariables = new ArrayList<>();
        Set<String> edgeVariablesToResolve = new HashSet<>();
        for (String returnVariable : returnVariables) {
            if (queryGraph.getAllVariables().contains(returnVariable)) {
                if (!variablesToProjectSet.contains(returnVariable)) {
                    variablesToProjectSet.add(returnVariable);
                    orderedVertexVariablesAfterProjection = new ArrayList<>(returnVariables);
                }
            } else {
                if (!edgeVariablesToResolve.contains(returnVariable)) {
                    edgeVariablesToResolve.add(returnVariable);
                    orderedEdgeVariables.add(returnVariable);
                }
            }
        }
        String variableName;
        for (Pair<String, String> returnVariablePropertyPair : returnVariablePropertyPairs) {
            variableName = returnVariablePropertyPair.a;
            if (queryGraph.getAllVariables().contains(variableName)) {
                if (!variablesToProjectSet.contains(variableName)) {
                    variablesToProjectSet.add(variableName);
                    orderedVertexVariablesAfterProjection.add(variableName);
                }
            } else {
                if (!edgeVariablesToResolve.contains(variableName)) {
                    edgeVariablesToResolve.add(variableName);
                    orderedEdgeVariables.add(variableName);
                }
            }
        }
        return new Pair<>(orderedVertexVariablesAfterProjection, orderedEdgeVariables);
    }

    private AbstractDBOperator getIdentityPropertyResolver(
        List<String> orderedVertexVariablesBeforeProjection) {
        List<EdgeOrVertexPropertyIndices> edgeOrVertexPropertyIndices = new ArrayList<>();
        for (int i = 0; i < orderedVertexVariablesBeforeProjection.size(); ++i) {
            // Since the vertex Ids in the prefixes will be ordered we just pass in i as the index
            // below.
            edgeOrVertexPropertyIndices.add(new EdgeOrVertexPropertyIndices(
                true /* is vertex or vertex property */, i,
                (short) -1 /* No type. just return the vertex ID. */));
        }
        logger.info("Appending PropertyResolver->OutputSink.");
        return new PropertyResolver(outputSink, edgeOrVertexPropertyIndices);
    }

    private Map<String, Integer> getOrderedVariableIndexMap(List<String> orderedVariables) {
        Map<String, Integer> variableOrderIndexMapBeforeProjection = new HashMap<>();
        for (int i = 0; i < orderedVariables.size(); ++i) {
            variableOrderIndexMapBeforeProjection.put(orderedVariables.get(i), i);
        }
        return variableOrderIndexMapBeforeProjection;
    }
}

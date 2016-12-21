package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryGraph;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Create a {@code QueryPlan} for the MATCH operation.
 */
public class OneTimeMatchQueryPlanner extends AbstractQueryPlanner {

    private static final Logger logger = LogManager.getLogger(OneTimeMatchQueryPlanner.class);
    protected QueryGraph queryGraph = new QueryGraph();

    public OneTimeMatchQueryPlanner(StructuredQuery structuredQuery) {
        super(structuredQuery);
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            queryGraph.addRelation(queryRelation);
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
     * @throws NoSuchElementException If any edge type {@code String} does not already exist in the
     * {@link TypeStore}, signifying that the one time {@code MATCH} query will return an empty
     * result set.
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
                        stage.add(new GenericJoinIntersectionRule(j,
                            // The {@code Direction} of the rule is {@code FORWARD} if
                            // {@code queryRelation} is an edge from
                            // {@code variableFromPreviousStage} to
                            // {@code variableForCurrentStage}, else {@code BACKWARD}.
                            queryRelation.getFromQueryVariable().getVariableId().equals(
                                variableFromPreviousStage) ? Direction.FORWARD :
                                Direction.BACKWARD,
                            // {@code TypeStore#getShortIdOrAnyTypeIfNull()} will throw a
                            // {@code NoSuchElementException} if the relation type {@code String}
                            // of {@code queryRelation} does not already exist in the
                            // {@code TypeStore}.
                            TypeStore.getInstance().getShortIdOrAnyTypeIfNull(queryRelation.
                                getRelationType())));
                    }
                }
            }
            oneTimeMatchQueryPlan.addStage(stage);
        }
        logger.info("Plan: \n" + oneTimeMatchQueryPlan);
        return oneTimeMatchQueryPlan;
    }
}

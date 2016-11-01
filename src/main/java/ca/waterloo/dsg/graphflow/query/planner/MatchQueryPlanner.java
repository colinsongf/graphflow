package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.MatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryGraph;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariableAdjList;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Create a {@code QueryPlan} for the MATCH operation.
 */
public class MatchQueryPlanner extends AbstractQueryPlanner {

    private static final Logger logger = LogManager.getLogger(MatchQueryPlanner.class);
    private QueryGraph matchQueryGraph = new QueryGraph();

    public MatchQueryPlanner(StructuredQuery structuredQuery) {
        super(structuredQuery);

        // Initialize {@code matchQueryGraph}.
        for (StructuredQueryEdge structuredQueryEdge : structuredQuery.getStructuredQueryEdges()) {
            String toVariable = structuredQueryEdge.getToVertex();
            String fromVariable = structuredQueryEdge.getFromVertex();
            matchQueryGraph.addEdge(fromVariable, toVariable);
        }
        logger.info("Query Graph: \n" + matchQueryGraph);
    }

    public QueryPlan plan() {
        MatchQueryPlan matchQueryPlan = new MatchQueryPlan();
        Set<String> visitedVariables = new HashSet<>();
        int variablesLeftToCoverCount = matchQueryGraph.getTotalCount();

        /**
         * Find the first variable, considering the following properties:
         * 1) Select the variable with the highest degree.
         * 2) Break tie from (1) by selecting the variable with the lowest lexicographical rank.
         */
        int highestDegreeCount = -1;
        String variableWithHighestDegree = "";
        for (String variable : matchQueryGraph.getAllVariables()) {
            int variableDegree = matchQueryGraph.getQueryVariableAdjList(variable).getTotalDegree();
            if (variableDegree > highestDegreeCount) {
                // Rule (1).
                highestDegreeCount = variableDegree;
                variableWithHighestDegree = variable;
            } else if ((variableDegree == highestDegreeCount) && (variable.compareTo(
                variableWithHighestDegree) < 0)) {
                // Rule (2).
                highestDegreeCount = variableDegree;
                variableWithHighestDegree = variable;
            }
        }
        matchQueryPlan.addOrderedVariable(variableWithHighestDegree);
        visitedVariables.add(variableWithHighestDegree);
        variablesLeftToCoverCount--;

        /**
         * Select the order of the rest of the variables, considering the following properties:
         * 1) Select the variable with highest number of connections to the already covered
         * variables.
         * 2) Break tie from (1) by selecting the variable with highest degree.
         * 3) Break tie from (2) by selecting the variable with lowest lexicographical rank.
         */
        while (variablesLeftToCoverCount > 0) {
            String selectedVariable = "";
            int highestConnectionsCount = -1;
            highestDegreeCount = -1;
            for (String coveredVariable : matchQueryPlan.getAllOrderedVariables()) {
                // Loop for all neighboring vertices of the already covered vertices.
                for (String neighborVariable : matchQueryGraph.getQueryVariableAdjList(
                    coveredVariable).getAllNeighborVariables()) {
                    // Skip vertices which have already been covered.
                    if (visitedVariables.contains(neighborVariable)) {
                        continue;
                    }
                    int variableDegree = matchQueryGraph.getQueryVariableAdjList(neighborVariable)
                                                        .getTotalDegree();
                    // Calculate the number of connections of the new variable to the already
                    // covered vertices.
                    int connectionsCount = 0;
                    for (String alreadyCoveredVariable : matchQueryPlan.getAllOrderedVariables()) {
                        if (matchQueryGraph.getQueryVariableAdjList(neighborVariable)
                                           .hasNeighborVariable(alreadyCoveredVariable)) {
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
            matchQueryPlan.addOrderedVariable(selectedVariable);
            visitedVariables.add(selectedVariable);
            variablesLeftToCoverCount--;
        }

        // Finally, create the plan.
        // Start from the second variable to create the first stage.
        for (int i = 1; i < matchQueryPlan.getOrderedVariablesCount(); i++) {
            String variableForCurrentStage = matchQueryPlan.getOrderedVariableAt(i);
            ArrayList<GenericJoinIntersectionRule> stage = new ArrayList<>();
            // Loop across all variables covered in the previous stages.
            for (int j = 0; j < i; j++) {
                String variableFromPreviousStage = matchQueryPlan.getOrderedVariableAt(j);
                if (matchQueryGraph.getQueryVariableAdjList(variableFromPreviousStage)
                                   .hasNeighborVariable(variableForCurrentStage)) {
                    boolean isForward = matchQueryGraph.getQueryVariableAdjList(
                        variableFromPreviousStage).getDirectionTo(variableForCurrentStage) ==
                                        QueryVariableAdjList.Direction.FORWARD;
                    stage.add(new GenericJoinIntersectionRule(j, isForward));
                }
            }
            matchQueryPlan.addStage(stage);
        }
        logger.info("Plan: \n" + matchQueryPlan);
        return matchQueryPlan;
    }
}

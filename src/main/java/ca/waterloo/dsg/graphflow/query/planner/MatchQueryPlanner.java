package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.IQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.MatchQueryPlan;
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
 * Create an {@code IQueryPlan} for the MATCH operation.
 */
public class MatchQueryPlanner implements IQueryPlanner {

    private static final Logger logger = LogManager.getLogger(MatchQueryPlanner.class);
    private QueryGraph matchQueryGraph = new QueryGraph();

    public IQueryPlan plan(StructuredQuery structuredQuery) {
        MatchQueryPlan matchQueryPlan = new MatchQueryPlan();
        Set<String> visitedVariables = new HashSet<>();

        // Initialize {@code matchQueryGraph}.
        for (StructuredQueryEdge structuredQueryEdge : structuredQuery.getStructuredQueryEdges()) {
            String toVariable = structuredQueryEdge.getToVertex();
            String fromVariable = structuredQueryEdge.getFromVertex();
            matchQueryGraph.addEdge(fromVariable, toVariable);
        }
        logger.info("Query Graph: \n" + matchQueryGraph);

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
        matchQueryPlan.getOrderedVariables().add(variableWithHighestDegree);
        visitedVariables.add(variableWithHighestDegree);

        /**
         * Select the order of the rest of the variables, considering the following properties:
         * 1) Select the variable with highest number of connections to the already covered
         * variables.
         * 2) Break tie from (1) by selecting the variable with highest degree.
         * 3) Break tie from (2) by selecting the variable with lowest lexicographical rank.
         */
        for (int i = 0; i < matchQueryGraph.getTotalCount() - 1; i++) {
            String selectedVariable = "";
            int highestConnectionsCount = -1;
            highestDegreeCount = -1;
            for (String coveredVariable : matchQueryPlan.getOrderedVariables()) {
                // Loop for all neighboring vertices of the already covered vertices.
                for (String neighborVariable : matchQueryGraph.getQueryVariableAdjList(
                    coveredVariable).getNeighborVariables().keySet()) {
                    int variableDegree = matchQueryGraph.getQueryVariableAdjList(neighborVariable)
                                                        .getTotalDegree();
                    // Skip vertices which have already been covered.
                    if (visitedVariables.contains(neighborVariable)) {
                        continue;
                    }
                    // Calculate the number of connections of the new variable to the already
                    // covered vertices.
                    int connectionsCount = 0;
                    for (String variable : matchQueryPlan.getOrderedVariables()) {
                        if (matchQueryGraph.getQueryVariableAdjList(neighborVariable)
                                           .getNeighborVariables().containsKey(variable)) {
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
                        } else if ((variableDegree == highestDegreeCount) && (neighborVariable
                            .compareTo(selectedVariable) < 0)) {
                            // Rule (3).
                            selectedVariable = neighborVariable;
                            highestDegreeCount = variableDegree;
                            highestConnectionsCount = connectionsCount;
                        }
                    }
                }
            }
            matchQueryPlan.getOrderedVariables().add(selectedVariable);
            visitedVariables.add(selectedVariable);
        }

        // Finally, create the plan.
        for (int i = 1; i < matchQueryPlan.getOrderedVariables().size(); i++) {
            ArrayList<GenericJoinIntersectionRule> stage = new ArrayList<>();
            String variable = matchQueryPlan.getOrderedVariables().get(i);
            for (int j = 0; j < i; j++) {
                String coveredVariable = matchQueryPlan.getOrderedVariables().get(j);
                if (matchQueryGraph.getQueryVariableAdjList(variable).getNeighborVariables()
                                   .containsKey(coveredVariable)) {
                    boolean isForward = matchQueryGraph.getQueryVariableAdjList(variable)
                                                       .getNeighborVariables().get(
                            coveredVariable) == QueryVariableAdjList.Direction.FORWARD;
                    stage.add(new GenericJoinIntersectionRule(j, isForward));
                }
            }
            matchQueryPlan.addStage(stage);
        }
        logger.info("Plan: \n" + matchQueryPlan);
        return matchQueryPlan;
    }
}

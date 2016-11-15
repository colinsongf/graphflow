package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.GJMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryGraph;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Create a {@code QueryPlan} for the MATCH operation.
 */
public class OneTimeMatchQueryPlanner extends AbstractQueryPlanner {

    private static final Logger logger = LogManager.getLogger(OneTimeMatchQueryPlanner.class);
    protected QueryGraph queryGraph = new QueryGraph();

    public OneTimeMatchQueryPlanner(StructuredQuery structuredQuery) {
        super(structuredQuery);

        // Initialize {@code queryGraph}.
        for (StructuredQueryEdge structuredQueryEdge : structuredQuery.getStructuredQueryEdges()) {
            String toVariable = structuredQueryEdge.getToVertex();
            String fromVariable = structuredQueryEdge.getFromVertex();
            queryGraph.addEdge(fromVariable, toVariable);
        }
        logger.info("Query Graph: \n" + queryGraph);
    }

    /**
     * Selects the order of the rest of the variables, considering the following properties:
     * 1) Select the variable with highest number of connections to the already covered
     * variables.
     * 2) Break tie from (1) by selecting the variable with highest degree.
     * 3) Break tie from (2) by selecting the variable with lowest lexicographical rank.
     */
    public void orderRemainingVariables(List<String> orderedVariables) {
        int initialSize = orderedVariables.size();
        Set<String> visitedVariables = new HashSet<>();
        visitedVariables.addAll(orderedVariables);
        for (int i = 0; i < queryGraph.getQueryVariableCount() - initialSize; i++) {
            String selectedVariable = "";
            int highestConnectionsCount = -1;
            int highestDegreeCount = -1;
            for (String coveredVariable : orderedVariables) {
                // Loop for all neighboring vertices of the already covered vertices.
                for (String neighborVariable : queryGraph.getQueryVariableAdjList(coveredVariable)
                    .getAllNeighborVariables()) {
                    // Skip vertices which have already been covered.
                    if (visitedVariables.contains(neighborVariable)) {
                        continue;
                    }
                    int variableDegree = queryGraph.getQueryVariableAdjList(neighborVariable)
                        .getTotalDegree();
                    // Calculate the number of connections of the new variable to the already
                    // covered vertices.
                    int connectionsCount = 0;
                    for (String alreadyCoveredVariable : orderedVariables) {
                        if (queryGraph.getQueryVariableAdjList(neighborVariable)
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
            orderedVariables.add(selectedVariable);
            visitedVariables.add(selectedVariable);
        }
    }

    public QueryPlan plan() {
        GJMatchQueryPlan gjMatchQueryPlan = new GJMatchQueryPlan();
        Set<String> visitedVariables = new HashSet<>();
        List<String> orderedVariables = new ArrayList<>();
        /*
          Find the first variable, considering the following properties:
          1) Select the variable with the highest degree.
          2) Break tie from (1) by selecting the variable with the lowest lexicographical rank.
         */
        int highestDegreeCount = -1;
        String variableWithHighestDegree = "";
        for (String variable : queryGraph.getAllVariables()) {
            int variableDegree = queryGraph.getQueryVariableAdjList(variable).getTotalDegree();
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
        orderedVariables.add(variableWithHighestDegree);
        visitedVariables.add(variableWithHighestDegree);

        orderRemainingVariables(orderedVariables);
        // Finally, create the plan.
        // Start from the second variable to create the first stage.
        for (int i = 1; i < orderedVariables.size(); i++) {
            String variableForCurrentStage = orderedVariables.get(i);
            ArrayList<GenericJoinIntersectionRule> stage = new ArrayList<>();
            // Loop across all variables covered in the previous stages.
            for (int j = 0; j < i; j++) {
                String variableFromPreviousStage = orderedVariables.get(j);
                if (queryGraph.getQueryVariableAdjList(variableFromPreviousStage)
                    .hasNeighborVariable(variableForCurrentStage)) {
                    EdgeDirection edgeDirection = queryGraph.getQueryVariableAdjList(
                        variableFromPreviousStage).getDirectionTo(variableForCurrentStage);
                    stage.add(new GenericJoinIntersectionRule(j, edgeDirection));
                }
            }
            gjMatchQueryPlan.addStage(stage);
        }
        logger.info("Plan: \n" + gjMatchQueryPlan);
        return gjMatchQueryPlan;
    }
}

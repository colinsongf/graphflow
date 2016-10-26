package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.QueryGraph;
import ca.waterloo.dsg.graphflow.query.QueryVariable;
import ca.waterloo.dsg.graphflow.query.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.StructuredQueryEdge;
import ca.waterloo.dsg.graphflow.query.genericjoin.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.IQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.MatchQueryPlan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * Create a {@code IQueryPlan} for the MATCH operation.
 */
public class MatchQueryPlanner implements IQueryPlanner {

    private static final Logger logger = LogManager.getLogger(MatchQueryPlanner.class);

    QueryGraph matchQueryGraph = new QueryGraph();

    public IQueryPlan plan(StructuredQuery structuredQuery) {
        MatchQueryPlan matchQueryPlan = new MatchQueryPlan();

        // Initialize {@code matchQueryGraph}
        for (StructuredQueryEdge structuredQueryEdge : structuredQuery.getStructuredQueryEdges()) {
            String toVariable = structuredQueryEdge.getToVertex();
            String fromVariable = structuredQueryEdge.getFromVertex();
            matchQueryGraph.addEdge(fromVariable, toVariable);
        }
        logger.info("Query Graph: \n" + matchQueryGraph);

        /**
         * Find the first variable, considering the following properties:
         * 1) select variable with highest degree.
         * 2) break tie from (1) by selecting variable with lowest lexicographical rank.
         */
        int highestDegreeCount = -1;
        String variableWithHighestDegree = "";
        for (String variable : matchQueryGraph.getQueryGraph().keySet()) {
            int variableDegree = matchQueryGraph.getQueryVariableData(variable).getTotalDegree();
            if (variableDegree > highestDegreeCount) {
                // rule (1)
                highestDegreeCount = variableDegree;
                variableWithHighestDegree = variable;
            } else if ((variableDegree == highestDegreeCount) && (variable.compareTo(
                variableWithHighestDegree) < 0)) {
                // rule (2)
                highestDegreeCount = variableDegree;
                variableWithHighestDegree = variable;
            }
        }
        matchQueryPlan.getOrderedVariables().add(variableWithHighestDegree);
        matchQueryGraph.getQueryVariableData(variableWithHighestDegree).setVisited(true);

        /**
         * Select order of the rest of the variables, considering the following properties:
         * 1) select variable with highest number of connections to already covered variables.
         * 2) break tie from (1) by selecting variable with highest degree.
         * 3) break tie from (2) by selecting variable with lowest lexicographical rank.
         */
        for (int i = 0; i < matchQueryGraph.getQueryGraph().size() - 1; i++) {
            String theChosenOne = "";
            int highestConnectionsCount = -1;
            highestDegreeCount = -1;
            for (String coveredVariable : matchQueryPlan.getOrderedVariables()) {
                // loop for all neighboring vertices of the already covered vertices.
                for (String neighborVariable : matchQueryGraph.getQueryVariableData(coveredVariable)
                                                              .getNeighborVariables().keySet()) {
                    // skip vertices which have already been covered.
                    if (matchQueryGraph.getQueryVariableData(neighborVariable).isVisited()) {
                        continue;
                    }

                    int variableDegree = matchQueryGraph.getQueryVariableData(neighborVariable)
                                                        .getTotalDegree();

                    // calculate number of connections of new variable to already covered vertices.
                    int connectionsCount = 0;
                    for (String variable : matchQueryPlan.getOrderedVariables()) {
                        if (matchQueryGraph.getQueryVariableData(neighborVariable)
                                           .getNeighborVariables().containsKey(variable)) {
                            connectionsCount++;
                        }
                    }

                    // see if the new {@code neighbourVariable} should be chosen first
                    if ((connectionsCount > highestConnectionsCount)) {
                        // rule (1)
                        theChosenOne = neighborVariable;
                        highestDegreeCount = variableDegree;
                        highestConnectionsCount = connectionsCount;
                    } else if (connectionsCount == highestConnectionsCount) {
                        if (variableDegree > highestDegreeCount) {
                            // rule (2)
                            theChosenOne = neighborVariable;
                            highestDegreeCount = variableDegree;
                            highestConnectionsCount = connectionsCount;
                        } else if ((variableDegree == highestDegreeCount) && (neighborVariable
                            .compareTo(theChosenOne) < 0)) {
                            // rule (3)
                            theChosenOne = neighborVariable;
                            highestDegreeCount = variableDegree;
                            highestConnectionsCount = connectionsCount;
                        }
                    }
                }
            }
            matchQueryPlan.getOrderedVariables().add(theChosenOne);
            matchQueryGraph.getQueryVariableData(theChosenOne).setVisited(true);
        }

        // finally, create the plan
        for (int i = 1; i < matchQueryPlan.getOrderedVariables().size(); i++) {
            ArrayList<GenericJoinIntersectionRule> stage = new ArrayList<>();
            String variable = matchQueryPlan.getOrderedVariables().get(i);
            for (int j = 0; j < i; j++) {
                String coveredVariable = matchQueryPlan.getOrderedVariables().get(j);
                if (matchQueryGraph.getQueryVariableData(variable).getNeighborVariables()
                                   .containsKey(coveredVariable)) {
                    boolean isForward = matchQueryGraph.getQueryVariableData(variable)
                                                       .getNeighborVariables().get(
                            coveredVariable) == QueryVariable.Direction.FORWARD;
                    stage.add(new GenericJoinIntersectionRule(j, isForward));
                }
            }
            matchQueryPlan.addStage(stage);
        }
        logger.info("Plan: \n" + matchQueryPlan);
        return matchQueryPlan;
    }
}

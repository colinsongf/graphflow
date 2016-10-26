package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.genericjoin.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.parser.Edge;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.MatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.util.QueryVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Create a {@code QueryPlan} for the MATCH operation.
 */
public class MatchQueryPlanner implements IQueryPlanner {

    Map<String, QueryVertex> queryGraph = new HashMap<>();

    public QueryPlan plan(StructuredQuery query) {
        MatchQueryPlan matchQueryPlan = new MatchQueryPlan(query);
        for (Edge edge : query.getEdges()) {
            String toVertex = edge.getToVertex();
            String fromVertex = edge.getFromVertex();
            addVariable(toVertex);
            addVariable(fromVertex);
            queryGraph.get(toVertex).addNeighbourVertexVariable(fromVertex, true);
            queryGraph.get(fromVertex).addNeighbourVertexVariable(toVertex, false);
        }
        System.out.println("Query graph:");
        printQueryGraph();
        int highestDegreeCount = -1;
        String vertexVariableWithHighestDegree = "";
        for (String vertexVariable : queryGraph.keySet()) {
            int vertexDegree = queryGraph.get(vertexVariable).getTotalDegree();
            if (vertexDegree > highestDegreeCount || (vertexDegree == highestDegreeCount &&
                vertexVariable
                .compareTo(vertexVariableWithHighestDegree) < 0)) {
                highestDegreeCount = vertexDegree;
                vertexVariableWithHighestDegree = vertexVariable;
            }
        }
        matchQueryPlan.getOrderedVertexVariables().add(vertexVariableWithHighestDegree);
        queryGraph.get(vertexVariableWithHighestDegree).setVisited(true);
        for (int i = 0; i < queryGraph.size() - 1; i++) {
            String theChosenOne = "";
            int coveredVariablesDegreeCount = -1;
            highestDegreeCount = -1;
            for (String coveredVariable : matchQueryPlan.getOrderedVertexVariables()) {
                for (String neighbourVertex : queryGraph.get(coveredVariable)
                                                        .getNeighbourVertexVariables().keySet()) {
                    if (queryGraph.get(neighbourVertex).isVisited()) {
                        continue;
                    }
                    int vertexDegree = queryGraph.get(neighbourVertex).getTotalDegree();
                    int coveredVariablesDegree = 0;
                    for (String variable : matchQueryPlan.getOrderedVertexVariables()) {
                        if (queryGraph.get(neighbourVertex).getNeighbourVertexVariables()
                                      .containsKey(variable)) {
                            coveredVariablesDegree++;
                        }
                    }
                    if (coveredVariablesDegree > coveredVariablesDegreeCount ||
                        (coveredVariablesDegree == coveredVariablesDegreeCount && ((vertexDegree
                            > highestDegreeCount || (vertexDegree == highestDegreeCount &&
                            neighbourVertex
                        .compareTo(theChosenOne) < 0))))) {
                        theChosenOne = neighbourVertex;
                        highestDegreeCount = vertexDegree;
                        coveredVariablesDegreeCount = coveredVariablesDegree;
                    }
                }
            }
            matchQueryPlan.getOrderedVertexVariables().add(theChosenOne);
            queryGraph.get(theChosenOne).setVisited(true);
        }
        for (int i = 1; i < matchQueryPlan.getOrderedVertexVariables().size(); i++) {
            ArrayList<GenericJoinIntersectionRule> stage = new ArrayList<>();
            String variable = matchQueryPlan.getOrderedVertexVariables().get(i);
            for (int j = 0; j < i; j++) {
                String coveredVariable = matchQueryPlan.getOrderedVertexVariables().get(j);
                if (queryGraph.get(variable).getNeighbourVertexVariables().containsKey(
                    coveredVariable)) {
                    stage.add(new GenericJoinIntersectionRule(j, queryGraph.get(variable)
                                                                           .getNeighbourVertexVariables()
                                                                           .get(coveredVariable)));
                }
            }
            matchQueryPlan.addStage(stage);
        }
        System.out.println("Plan: \n" + matchQueryPlan);
        return matchQueryPlan;
    }

    private void addVariable(String variable) {
        if (!queryGraph.containsKey(variable)) {
            queryGraph.put(variable, new QueryVertex());
        }
    }

    private void printQueryGraph() {
        for (String key : queryGraph.keySet()) {
            QueryVertex v = queryGraph.get(key);
            System.out.println(key + " (degree = " + v.getTotalDegree() + ")");
            v.getNeighbourVertexVariables().forEach((variable, isReverse) -> {
                System.out.println(isReverse ? (variable + "->" + key) : (key + "->" + variable));
            });
        }
    }
}

package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.query.genericjoin.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.parser.Edge;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.plans.MatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.util.QueryVertex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Create a {@code QueryPlan} for the MATCH operation.
 */
public class MatchQueryPlanner implements IQueryPlanner {

    private static final Logger logger = LogManager.getLogger(MatchQueryPlanner.class);

    // Represents the query graph structure
    Map<String, QueryVertex> queryGraph = new HashMap<>();

    public QueryPlan plan(StructuredQuery query) {
        MatchQueryPlan matchQueryPlan = new MatchQueryPlan(query);

        // Initialize {@code queryGraph}
        for (Edge edge : query.getEdges()) {
            String toVertex = edge.getToVertex();
            String fromVertex = edge.getFromVertex();
            addVariable(toVertex);
            addVariable(fromVertex);
            queryGraph.get(toVertex).addNeighbourVertexVariable(fromVertex, true);
            queryGraph.get(fromVertex).addNeighbourVertexVariable(toVertex, false);
        }
        logger.info("Query Graph: \n" + queryGraphToString());

        /**
         * Find the first vertex considering the following properties:
         * 1) select vertex with highest degree.
         * 2) break tie from (1) by selecting vertex with lowest lexicographical rank.
         */
        int highestDegreeCount = -1;
        String vertexVariableWithHighestDegree = "";
        for (String vertexVariable : queryGraph.keySet()) {
            int vertexDegree = queryGraph.get(vertexVariable).getTotalDegree();
            if ((vertexDegree > highestDegreeCount)) {
                // rule (1)
                highestDegreeCount = vertexDegree;
                vertexVariableWithHighestDegree = vertexVariable;
            } else if (((vertexDegree == highestDegreeCount) && (vertexVariable.compareTo(
                vertexVariableWithHighestDegree) < 0))) {
                // rule (2)
                highestDegreeCount = vertexDegree;
                vertexVariableWithHighestDegree = vertexVariable;
            }
        }
        matchQueryPlan.getOrderedVertexVariables().add(vertexVariableWithHighestDegree);
        queryGraph.get(vertexVariableWithHighestDegree).setVisited(true);

        /**
         * Select order of the rest of the vertices, considering the following properties:
         * 1) select vertex with highest number of connections to already covered vertices.
         * 2) break tie from (1) by selecting vertex with highest degree.
         * 3) break tie from (2) by selecting vertex with lowest lexicographical rank.
         */
        for (int i = 0; i < queryGraph.size() - 1; i++) {
            String theChosenOne = "";
            int coveredVariablesDegreeCount = -1;
            highestDegreeCount = -1;
            for (String coveredVariable : matchQueryPlan.getOrderedVertexVariables()) {
                // loop for all vertices connected to the already covered vertices.
                for (String neighbourVertex : queryGraph.get(coveredVariable)
                                                        .getNeighbourVertexVariables().keySet()) {
                    // skip vertices which have already been covered.
                    if (queryGraph.get(neighbourVertex).isVisited()) {
                        continue;
                    }

                    int vertexDegree = queryGraph.get(neighbourVertex).getTotalDegree();

                    // calculate degree of connections of new vertex to already covered vertices.
                    int degreeCountToCoveredVariables = 0;
                    for (String variable : matchQueryPlan.getOrderedVertexVariables()) {
                        if (queryGraph.get(neighbourVertex).getNeighbourVertexVariables()
                                      .containsKey(variable)) {
                            degreeCountToCoveredVariables++;
                        }
                    }

                    // see if the new {@code neighbourVertex} should be chosen first
                    if ((degreeCountToCoveredVariables > coveredVariablesDegreeCount)) {
                        // rule (1)
                        theChosenOne = neighbourVertex;
                        highestDegreeCount = vertexDegree;
                        coveredVariablesDegreeCount = degreeCountToCoveredVariables;
                    } else if (degreeCountToCoveredVariables == coveredVariablesDegreeCount) {
                        if ((vertexDegree > highestDegreeCount)) {
                            // rule (2)
                            theChosenOne = neighbourVertex;
                            highestDegreeCount = vertexDegree;
                            coveredVariablesDegreeCount = degreeCountToCoveredVariables;
                        } else if (((vertexDegree == highestDegreeCount) && (neighbourVertex
                            .compareTo(theChosenOne) < 0))) {
                            // rule (3)
                            theChosenOne = neighbourVertex;
                            highestDegreeCount = vertexDegree;
                            coveredVariablesDegreeCount = degreeCountToCoveredVariables;
                        }
                    }
                }
            }
            matchQueryPlan.getOrderedVertexVariables().add(theChosenOne);
            queryGraph.get(theChosenOne).setVisited(true);
        }

        // finally, create the plan
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
        logger.info("Plan: \n" + matchQueryPlan);
        return matchQueryPlan;
    }

    private void addVariable(String variable) {
        if (!queryGraph.containsKey(variable)) {
            queryGraph.put(variable, new QueryVertex());
        }
    }

    private String queryGraphToString() {
        String graph = "";
        for (String key : queryGraph.keySet()) {
            QueryVertex vertex = queryGraph.get(key);
            graph += key + " (degree = " + vertex.getTotalDegree() + ")\n";
            for (Map.Entry<String, Boolean> entry : vertex.getNeighbourVertexVariables()
                                                          .entrySet()) {
                graph += (entry.getValue() ? (entry.getKey() + "->" + key) : (key + "->" + entry
                    .getKey())) + "\n";
            }
        }
        return graph;
    }
}

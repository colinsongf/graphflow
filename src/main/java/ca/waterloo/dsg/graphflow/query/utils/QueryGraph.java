package ca.waterloo.dsg.graphflow.query.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a user query as a graph structure, used for creating the {@code QueryPlan}.
 */
public class QueryGraph {

    private Map<String, QueryVariableAdjList> queryGraph = new HashMap<>();
    private Set<QueryEdge> queryEdges = new HashSet<>();

    public Set<String> getAllVariables() {
        return queryGraph.keySet();
    }

    public QueryVariableAdjList getQueryVariableAdjList(String variable) {
        return queryGraph.get(variable);
    }

    public void addEdge(String fromVariable, String toVariable) {
        addVariable(toVariable);
        addVariable(fromVariable);
        queryGraph.get(toVariable)
            .addNeighborVariable(fromVariable, QueryVariableAdjList.Direction.REVERSE);
        queryGraph.get(fromVariable)
            .addNeighborVariable(toVariable, QueryVariableAdjList.Direction.FORWARD);
        queryEdges.add(new QueryEdge(fromVariable, toVariable));
    }

    public Set<QueryEdge> getEdges() {
        return new HashSet<>(queryEdges);
    }

    /**
     * Add a variable string as a vertex in the {@code queryGraph}.
     */
    private void addVariable(String variable) {
        if (!queryGraph.containsKey(variable)) {
            queryGraph.put(variable, new QueryVariableAdjList());
        }
    }

    @Override
    public String toString() {
        StringBuilder graph = new StringBuilder();
        for (String key : queryGraph.keySet()) {
            QueryVariableAdjList queryVariableAdjList = queryGraph.get(key);
            graph.append(key + " (degree = " + queryVariableAdjList.getTotalDegree() + ")\n");
            for (String neighborVariable : queryVariableAdjList.getAllNeighborVariables()) {
                QueryVariableAdjList.Direction direction = queryVariableAdjList
                    .getDirectionTo(neighborVariable);
                graph.append((direction == QueryVariableAdjList.Direction.FORWARD ? (key + "->" +
                    neighborVariable) : (neighborVariable + "->" + key)) + "\n");
            }
        }
        return graph.toString();
    }

    public int getQueryVariableCount() {
        return queryGraph.size();
    }
}

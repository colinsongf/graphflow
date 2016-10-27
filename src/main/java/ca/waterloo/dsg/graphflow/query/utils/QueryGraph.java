package ca.waterloo.dsg.graphflow.query.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a user query as a graph structure, used for creating the {@code QueryPlan}.
 */
public class QueryGraph {

    private Map<String, QueryVariableAdjList> queryGraph = new HashMap<>();

    public Set<String> getAllVariables() {
        return queryGraph.keySet();
    }

    public int getTotalCount() {
        return queryGraph.size();
    }

    public QueryVariableAdjList getQueryVariableAdjList(String variable) {
        return queryGraph.get(variable);
    }

    public void addEdge(String fromVariable, String toVariable) {
        addVariable(toVariable);
        addVariable(fromVariable);
        queryGraph.get(toVariable).addNeighborVariable(fromVariable,
            QueryVariableAdjList.Direction.REVERSE);
        queryGraph.get(fromVariable).addNeighborVariable(toVariable,
            QueryVariableAdjList.Direction.FORWARD);
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
            for (Map.Entry<String, QueryVariableAdjList.Direction> entry : queryVariableAdjList
                .getNeighborVariables().entrySet()) {
                graph.append((entry
                    .getValue() == QueryVariableAdjList.Direction.FORWARD ? (key + "->" + entry
                    .getKey()) : (entry.getKey() + "->" + key)) + "\n");
            }
        }
        return graph.toString();
    }
}

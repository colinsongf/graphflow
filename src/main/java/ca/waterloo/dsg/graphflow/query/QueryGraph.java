package ca.waterloo.dsg.graphflow.query;

import java.util.HashMap;
import java.util.Map;

public class QueryGraph {

    // Represents the query graph structure
    private Map<String, QueryVariable> queryGraph = new HashMap<>();

    public Map<String, QueryVariable> getQueryGraph() {
        return queryGraph;
    }

    public QueryVariable getQueryVariableData(String variable) {
        return queryGraph.get(variable);
    }

    public void addEdge(String fromVariable, String toVariable) {
        addVariable(toVariable);
        addVariable(fromVariable);
        queryGraph.get(toVariable).addNeighborVariable(fromVariable,
            QueryVariable.Direction.FORWARD);
        queryGraph.get(fromVariable).addNeighborVariable(toVariable,
            QueryVariable.Direction.REVERSE);
    }

    private void addVariable(String variable) {
        if (!queryGraph.containsKey(variable)) {
            queryGraph.put(variable, new QueryVariable());
        }
    }

    public String toString() {
        String graph = "";
        for (String key : queryGraph.keySet()) {
            QueryVariable queryVariable = queryGraph.get(key);
            graph += key + " (degree = " + queryVariable.getTotalDegree() + ")\n";
            for (Map.Entry<String, QueryVariable.Direction> entry : queryVariable
                .getNeighborVariables().entrySet()) {
                graph += (entry.getValue() == QueryVariable.Direction.FORWARD ? (key + "->" + entry
                    .getKey()) : (entry.getKey() + "->" + key)) + "\n";
            }
        }
        return graph;
    }
}

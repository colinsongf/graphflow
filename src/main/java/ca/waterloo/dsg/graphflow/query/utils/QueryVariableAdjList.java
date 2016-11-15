package ca.waterloo.dsg.graphflow.query.utils;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This represents a query variable and its adjacency list.
 */
public class QueryVariableAdjList {

    private Map<String, Graph.EdgeDirection> neighborVariables = new HashMap<>();

    public int getTotalDegree() {
        return neighborVariables.size();
    }

    public Set<String> getAllNeighborVariables() {
        return neighborVariables.keySet();
    }

    public boolean hasNeighborVariable(String neighborVariable) {
        return neighborVariables.containsKey(neighborVariable);
    }

    public Graph.EdgeDirection getDirectionTo(String neighborVariable) {
        return neighborVariables.get(neighborVariable);
    }

    public void addNeighborVariable(String neighborVariable, Graph.EdgeDirection edgeDirection) {
        neighborVariables.put(neighborVariable, edgeDirection);
    }
}

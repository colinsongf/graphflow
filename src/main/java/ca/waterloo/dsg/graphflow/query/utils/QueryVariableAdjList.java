package ca.waterloo.dsg.graphflow.query.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This represents a query variable and its adjacency list.
 */
public class QueryVariableAdjList {

    public enum Direction {
        FORWARD,
        REVERSE
    }

    private Map<String, Direction> neighborVariables = new HashMap<>();

    public int getTotalDegree() {
        return neighborVariables.size();
    }

    public Set<String> getAllNeighborVariables() {
        return neighborVariables.keySet();
    }

    public boolean hasNeighborVariable(String neighborVariable) {
        return neighborVariables.containsKey(neighborVariable);
    }

    public Direction getDirectionTo(String neighborVariable) {
        return neighborVariables.get(neighborVariable);
    }

    public void addNeighborVariable(String neighborVariable, Direction direction) {
        neighborVariables.put(neighborVariable, direction);
    }
}

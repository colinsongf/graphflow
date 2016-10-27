package ca.waterloo.dsg.graphflow.query.utils;

import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Direction> getNeighborVariables() {
        return neighborVariables;
    }

    public void addNeighborVariable(String neighborVariable, Direction direction) {
        neighborVariables.put(neighborVariable, direction);
    }
}

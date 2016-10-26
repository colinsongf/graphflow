package ca.waterloo.dsg.graphflow.query;

import java.util.HashMap;
import java.util.Map;

public class QueryVariable {

    public enum Direction {
        FORWARD,
        REVERSE
    }
    private int totalDegree = 0;
    private boolean visited = false;
    private Map<String, Direction> neighborVariables = new HashMap<>();

    public int getTotalDegree() {
        return totalDegree;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Map<String, Direction> getNeighborVariables() {
        return neighborVariables;
    }

    public void addNeighborVariable(String neighborVariable, Direction direction) {
        neighborVariables.put(neighborVariable, direction);
        totalDegree++;
    }
}

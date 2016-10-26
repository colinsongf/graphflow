package ca.waterloo.dsg.graphflow.util;

import java.util.HashMap;
import java.util.Map;

public class QueryVertex {

    private int totalDegree = 0;
    private boolean visited = false;
    private Map<String, Boolean> neighbourVertexVariables = new HashMap<>();

    public int getTotalDegree() {
        return totalDegree;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Map<String, Boolean> getNeighbourVertexVariables() {
        return neighbourVertexVariables;
    }

    public void addNeighbourVertexVariable(String neighbourVertexVariable, Boolean isReverse) {
        neighbourVertexVariables.put(neighbourVertexVariable, isReverse);
        totalDegree++;
    }
}

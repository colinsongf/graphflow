package ca.waterloo.dsg.graphflow.demograph;

import ca.waterloo.dsg.graphflow.query.parser.Edge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple graph representation used to demo graph queries.
 */
public class Graph {

    private Map<Integer, Set<Integer>> forwardVertexMap = new HashMap<>();
    private Map<Integer, Set<Integer>> reverseVertexMap = new HashMap<>();

    public void addEdge(Edge edge) {
        int fromVertex = Integer.parseInt(edge.getFromVertex());
        int toVertex = Integer.parseInt(edge.getToVertex());

        if (this.forwardVertexMap.containsKey(fromVertex)) {
            this.forwardVertexMap.get(fromVertex).add(toVertex);
        } else {
            Set<Integer> toVertices = new HashSet<>();
            toVertices.add(toVertex);
            this.forwardVertexMap.put(fromVertex, toVertices);
        }

        if (this.reverseVertexMap.containsKey(toVertex)) {
            this.reverseVertexMap.get(toVertex).add(fromVertex);
        } else {
            Set<Integer> fromVertices = new HashSet<>();
            fromVertices.add(fromVertex);
            this.reverseVertexMap.put(toVertex, fromVertices);
        }
    }

    public void deleteEdge(Edge edge) {
        int fromVertex = Integer.parseInt(edge.getFromVertex());
        int toVertex = Integer.parseInt(edge.getToVertex());

        if (this.forwardVertexMap.containsKey(fromVertex)) {
            this.forwardVertexMap.get(fromVertex).remove(toVertex);
            if (this.forwardVertexMap.get(fromVertex).isEmpty()) {
                this.forwardVertexMap.remove(fromVertex);
            }
        }

        if (this.reverseVertexMap.containsKey(toVertex)) {
            this.reverseVertexMap.get(toVertex).remove(fromVertex);
            if (this.reverseVertexMap.get(toVertex).isEmpty()) {
                this.reverseVertexMap.remove(toVertex);
            }
        }
    }

    public String getGraphString() {
        String graph = "";
        for (int fromVertex : this.forwardVertexMap.keySet()) {
            for (int toVertex : this.forwardVertexMap.get(fromVertex)) {
                graph += fromVertex + " -> " + toVertex + "\n";
            }
        }
        return graph;
    }

    public void printGraph() {
        System.out.println(this.getGraphString());
    }
}

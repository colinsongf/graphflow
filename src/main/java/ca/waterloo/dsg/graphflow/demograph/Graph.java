package ca.waterloo.dsg.graphflow.demograph;

import ca.waterloo.dsg.graphflow.query.StructuredQueryEdge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple graph representation used to demo cli graph queries.
 * TODO: Replace with actual graph implementation
 */
public class Graph {

    private Map<Integer, Set<Integer>> forwardVertexMap = new HashMap<>();
    private Map<Integer, Set<Integer>> reverseVertexMap = new HashMap<>();

    public void addEdge(StructuredQueryEdge structuredQueryEdge) {
        int fromVertex = Integer.parseInt(structuredQueryEdge.getFromVertex());
        int toVertex = Integer.parseInt(structuredQueryEdge.getToVertex());

        if (forwardVertexMap.containsKey(fromVertex)) {
            forwardVertexMap.get(fromVertex).add(toVertex);
        } else {
            Set<Integer> toVertices = new HashSet<>();
            toVertices.add(toVertex);
            forwardVertexMap.put(fromVertex, toVertices);
        }

        if (reverseVertexMap.containsKey(toVertex)) {
            reverseVertexMap.get(toVertex).add(fromVertex);
        } else {
            Set<Integer> fromVertices = new HashSet<>();
            fromVertices.add(fromVertex);
            reverseVertexMap.put(toVertex, fromVertices);
        }
    }

    public void deleteEdge(StructuredQueryEdge structuredQueryEdge) {
        int fromVertex = Integer.parseInt(structuredQueryEdge.getFromVertex());
        int toVertex = Integer.parseInt(structuredQueryEdge.getToVertex());

        if (forwardVertexMap.containsKey(fromVertex)) {
            forwardVertexMap.get(fromVertex).remove(toVertex);
            if (forwardVertexMap.get(fromVertex).isEmpty()) {
                forwardVertexMap.remove(fromVertex);
            }
        }

        if (reverseVertexMap.containsKey(toVertex)) {
            reverseVertexMap.get(toVertex).remove(fromVertex);
            if (reverseVertexMap.get(toVertex).isEmpty()) {
                reverseVertexMap.remove(toVertex);
            }
        }
    }

    public String getGraphString() {
        String graph = "";
        for (int fromVertex : forwardVertexMap.keySet()) {
            for (int toVertex : forwardVertexMap.get(fromVertex)) {
                graph += fromVertex + " -> " + toVertex + "\n";
            }
        }
        return graph;
    }
}

package ca.waterloo.dsg.graphflow.query.utils;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a user query as a graph for fast lookup of variables and relations.
 */
public class QueryGraph {

    // Represents a map from fromVariable to toVariable and the list of {@code QueryEdge}s
    // between fromVariable and toVariable having different directions and edge types.
    private Map<String, Map<String, List<QueryEdge>>> queryGraph = new HashMap<>();

    /**
     * Adds an edge to the {@code QueryGraph}. The edge is stored both in forward and backward
     * direction. There can be multiple edges with different directions and edge types between two
     * variables.
     *
     * @param queryEdge The edge to be added.
     */
    public void addEdge(QueryEdge queryEdge) {
        // Get the vertex IDs.
        String fromVariable = queryEdge.getFromQueryVariable().getVariableId();
        String toVariable = queryEdge.getToQueryVariable().getVariableId();
        // Add the forward edge {@code fromVariable} -> {@code toVariable} to the queryGraph.
        addEdgeToQueryGraph(fromVariable, toVariable, queryEdge);
        // Add the reverse edge {@code toVariable} <- {@code fromVariable} to the queryGraph, by
        // creating a new {@code QueryEdge} with interchanged {@code QueryVariable}s in the
        // BACKWARD direction.
        QueryEdge reverseQueryEdge = new QueryEdge(queryEdge.getToQueryVariable(),
            queryEdge.getFromQueryVariable(), Direction.BACKWARD, queryEdge.getEdgeType());
        reverseQueryEdge.setEdgeType(queryEdge.getEdgeType());
        addEdgeToQueryGraph(toVariable, fromVariable, reverseQueryEdge);
    }

    /**
     * Adds the new edge to the {@code queryGraph} map.
     *
     * @param fromVariable The from variable.
     * @param toVariable The to variable.
     * @param queryEdge The edge object which contains details about edge and variable types.
     */
    private void addEdgeToQueryGraph(String fromVariable, String toVariable, QueryEdge queryEdge) {
        if (!queryGraph.containsKey(fromVariable)) {
            queryGraph.put(fromVariable, new HashMap<>());
        }
        if (!queryGraph.get(fromVariable).containsKey(toVariable)) {
            queryGraph.get(fromVariable).put(toVariable, new ArrayList<>());
        }
        queryGraph.get(fromVariable).get(toVariable).add(queryEdge);
    }

    /**
     * @return All the variables present in the query.
     */
    public Set<String> getAllVariables() {
        return queryGraph.keySet();
    }

    /**
     * @return The number of variables present in the query.
     */
    public int getTotalNumberOfVariables() {
        return queryGraph.size();
    }

    /**
     * @param variable The from variable whose number of adjacent relations is required.
     * @return The count of all incoming and outgoing relations of {@code variable}.
     */
    public int getNumberOfAdjacentRelations(String variable) {
        if (!queryGraph.containsKey(variable)) {
            throw new RuntimeException("The variable '" + variable + "' is not present.");
        }
        // Use lambda expressions to get the sum of the incoming and outgoing relations of
        // each {@code neighbourVariable} of {@code variable}.
        return queryGraph.get(variable).keySet().stream().mapToInt(neighbourVariable ->
            queryGraph.get(variable).get(neighbourVariable).size()).sum();
    }

    /**
     * @param variable The from variable whose neighbors are required.
     * @return The unordered list of neighbor variables.
     */
    public Set<String> getAllNeighborVariables(String variable) {
        if (!queryGraph.containsKey(variable)) {
            throw new RuntimeException("The variable '" + variable + "' is not present.");
        }
        return queryGraph.get(variable).keySet();
    }

    /**
     * @param variable1 One of the variables.
     * @param variable2 The other variable.
     * @return {@code true} if there is an edge between {@code variable1} and
     * {@code variable2} in any direction, {@code false} otherwise.
     */
    public boolean containsEdge(String variable1, String variable2) {
        return queryGraph.containsKey(variable1) && queryGraph.get(variable1)
            .containsKey(variable2);
    }

    /**
     * @param fromVariable The from variable.
     * @param toVariable The to variable.
     * @return A read-only list of {@code QueryEdge}s representing all the edges present between
     * {@code variable} and {@code neighborVariable}.
     */
    public List<QueryEdge> getAdjacentEdges(String fromVariable, String toVariable) {
        if (!queryGraph.containsKey(fromVariable)) {
            throw new RuntimeException("The variable '" + fromVariable + "' is not present.");
        }
        if (!queryGraph.get(fromVariable).containsKey(toVariable)) {
            throw new RuntimeException("The neighbour variable '" + toVariable + "' is not " +
                "present in the adjacency list of '" + fromVariable + "'.");
        }
        return Collections.unmodifiableList(queryGraph.get(fromVariable).get(toVariable));
    }
}

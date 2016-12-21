package ca.waterloo.dsg.graphflow.query.structuredquery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Represents a user query as a graph for fast lookup of variables and relations.
 */
public class QueryGraph {

    // Represents a map from fromVariable to toVariable and the list of {@code QueryRelation}s
    // between fromVariable and toVariable.
    private Map<String, Map<String, List<QueryRelation>>> queryGraph = new HashMap<>();

    /**
     * Adds a relation to the {@link QueryGraph}. The relation is stored both in forward and
     * backward direction. There can be multiple relations with different directions and relation
     * types between two variables. A backward relation between fromVariable and toVariable is
     * represented by a {@link QueryRelation} from toVariable to fromVariable.
     *
     * @param queryRelation The relation to be added.
     */
    public void addRelation(QueryRelation queryRelation) {
        // Get the vertex IDs.
        String fromVariable = queryRelation.getFromQueryVariable().getVariableId();
        String toVariable = queryRelation.getToQueryVariable().getVariableId();
        // Add the forward relation {@code fromVariable} -> {@code toVariable} to the queryGraph.
        addRelationToQueryGraph(fromVariable, toVariable, queryRelation);
        // Add the reverse relation {@code toVariable} <- {@code fromVariable} to the queryGraph.
        addRelationToQueryGraph(toVariable, fromVariable, queryRelation);
    }

    /**
     * Adds the new relation to the {@code queryGraph} map.
     *
     * @param fromVariable The from variable.
     * @param toVariable The to variable.
     * @param queryRelation The {@link QueryRelation} containing the relation and variable types.
     */
    private void addRelationToQueryGraph(String fromVariable, String toVariable,
        QueryRelation queryRelation) {
        if (!queryGraph.containsKey(fromVariable)) {
            queryGraph.put(fromVariable, new HashMap<>());
        }
        if (!queryGraph.get(fromVariable).containsKey(toVariable)) {
            queryGraph.get(fromVariable).put(toVariable, new ArrayList<>());
        }
        queryGraph.get(fromVariable).get(toVariable).add(queryRelation);
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
     * @throws NoSuchElementException if {@code variable} is not present in the {@link QueryGraph}.
     */
    public int getNumberOfAdjacentRelations(String variable) {
        if (!queryGraph.containsKey(variable)) {
            throw new NoSuchElementException("The variable '" + variable + "' is not present.");
        }
        // Use lambda expressions to get the sum of the incoming and outgoing relations of
        // each {@code neighbourVariable} of {@code variable}.
        return queryGraph.get(variable).keySet().stream().mapToInt(neighbourVariable ->
            queryGraph.get(variable).get(neighbourVariable).size()).sum();
    }

    /**
     * @param variable The from variable whose neighbors are required.
     * @return The unordered list of neighbor variables.
     * @throws NoSuchElementException if {@code variable} is not present in the {@link QueryGraph}.
     */
    public Set<String> getAllNeighborVariables(String variable) {
        if (!queryGraph.containsKey(variable)) {
            throw new NoSuchElementException("The variable '" + variable + "' is not present.");
        }
        return queryGraph.get(variable).keySet();
    }

    /**
     * @param variable1 One of the variables.
     * @param variable2 The other variable.
     * @return {@code true} if there is an relation between {@code variable1} and
     * {@code variable2} in any direction, {@code false} otherwise.
     */
    public boolean containsRelation(String variable1, String variable2) {
        return queryGraph.containsKey(variable1) && queryGraph.get(variable1)
            .containsKey(variable2);
    }

    /**
     * @param fromVariable The from variable.
     * @param toVariable The to variable.
     * @return A read-only list of {@link QueryRelation}s representing all the relations present
     * between
     * {@code variable} and {@code neighborVariable}.
     * @throws NoSuchElementException if either {@code fromVariable} or {@code toVariable} is not
     * present in the {@link QueryGraph}.
     */
    public List<QueryRelation> getAdjacentRelations(String fromVariable, String toVariable) {
        if (!queryGraph.containsKey(fromVariable)) {
            throw new NoSuchElementException("The variable '" + fromVariable + "' is not present.");
        }
        if (!queryGraph.get(fromVariable).containsKey(toVariable)) {
            throw new NoSuchElementException("The neighbour variable '" + toVariable + "' is not " +
                "present in the adjacency list of '" + fromVariable + "'.");
        }
        return Collections.unmodifiableList(queryGraph.get(fromVariable).get(toVariable));
    }
}

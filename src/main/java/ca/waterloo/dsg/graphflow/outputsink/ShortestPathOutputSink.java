package ca.waterloo.dsg.graphflow.outputsink;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Outputs shortest path query results in teh form of a graph.
 */
public class ShortestPathOutputSink {

    private Map<Integer, Set<Integer>> results;

    public Map<Integer, Set<Integer>> getResults() {
        return results;
    }

    public void setResults(Map<Integer, Set<Integer>> results) {
        this.results = results;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<Integer, Set<Integer>> entry: results.entrySet()) {
            builder.append(entry.getKey()+": "+ Arrays.toString(entry.getValue().toArray()));
            builder.append("\n");
        }
        return builder.toString();
    }
}

package ca.waterloo.dsg.graphflow.outputsink;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Outputs shortest path query results in the form of a graph.
 */
public class ShortestPathOutputSink {

    private int source;
    private int destination;
    private Map<Integer, Set<Integer>> results;

    public Map<Integer, Set<Integer>> getResults() {
        return results;
    }

    public void setResults(Map<Integer, Set<Integer>> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<Integer, Set<Integer>> entry: results.entrySet()) {
            builder.append(entry.getKey()+": "+ Arrays.toString(entry.getValue().toArray()));
            builder.append("\n");
        }
        return builder.toString();
    }

    public boolean isSameAs(ShortestPathOutputSink that) {
        if (null == that) {
            return false;
        }
        if (this == that) {
            return true;
        }

        if (that.results == this.results) {
            return true;
        }
        if (that.results == null || this.results == null) {
            return false;
        }
        if(that.results.size() != this.results.size()) {
            return false;
        }
        for (Entry<Integer, Set<Integer>> entry: results.entrySet()) {
            if (!that.results.containsKey(entry.getKey()) || !that.results.get(entry.getKey())
                .equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }
}

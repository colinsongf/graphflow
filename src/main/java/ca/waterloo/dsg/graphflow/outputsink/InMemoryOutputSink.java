package ca.waterloo.dsg.graphflow.outputsink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps output from a query as an in memory data structure.
 */
public class InMemoryOutputSink implements OutputSink {

    private List<int[]> results;

    public InMemoryOutputSink() {
        results = new ArrayList<>();
    }

    @Override
    public void append(int[][] results) {
        Collections.addAll(this.results, results);
    }

    public List<int[]> getResults() {
        return results;
    }
}

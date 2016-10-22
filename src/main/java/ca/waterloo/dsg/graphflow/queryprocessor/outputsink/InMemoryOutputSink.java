package ca.waterloo.dsg.graphflow.queryprocessor.outputsink;

import java.util.ArrayList;

/**
 * Keeps output from a Generic Join query as an in memory data structure
 */
public class InMemoryOutputSink implements OutputSink{

    private ArrayList<int[]> results;

    public InMemoryOutputSink() {
        results = new ArrayList<>();
    }

    @Override
    public void setName(String name) { }

    @Override
    public void append(int[][] results) {
        for(int[] result : results) {
            this.results.add(result);
        }
    }

    public ArrayList<int[]> getResults() {
        return results;
    }
}

package ca.waterloo.dsg.graphflow.queryprocessor.outputsink;

/**
 * Interface for outputting query results.
 */
public interface OutputSink {

    /**
     * Appends the given results to the output sink.
     *
     * @param results
     */
    void append(int[][] results);
}

package ca.waterloo.dsg.graphflow.outputsink;

import ca.waterloo.dsg.graphflow.query.executors.MatchQueryResultType;

/**
 * Interface for outputting query results.
 */
public interface OutputSink {

    /**
     * Appends the given results to the output sink.
     */
    void append(MatchQueryResultType matchQueryResultType, int[][] results);
}

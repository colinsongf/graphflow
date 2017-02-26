package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.executors.ContinuousMatchQueryExecutor;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.QueryOutputUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Outputs query results to a file.
 */
public class FileOutputSink extends AbstractDBOperator {

    private File location;
    private PrintWriter writer;

    public FileOutputSink(File location) throws IOException {
        super(null /* no output operator */);
        this.location = location;
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(location, true)));
    }

    /**
     * Appends data from {@code matchQueryOutput} to the configured {@code File}.
     * <p>
     * Warning: This class is currently only used by {@link ContinuousMatchQueryExecutor}. Currently
     * these plans are primitive and only consist of GJ outputs that consist of a set of vertex
     * IDs. This method assumes this property of {@link ContinuousMatchQueryExecutor}.
     *
     * @param matchQueryOutput The output data.
     */
    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        System.out.println(QueryOutputUtils.getStringMatchQueryOutput(matchQueryOutput.results,
            matchQueryOutput.resultLength, matchQueryOutput.matchQueryResultType));
        writer.println(QueryOutputUtils.getStringMatchQueryOutput(matchQueryOutput.results,
            matchQueryOutput.resultLength, matchQueryOutput.matchQueryResultType));
        writer.flush();
    }

    @Override
    public String getHumanReadableOperator() {
        return "FileOutputSink:\n";
    }

    @Override
    public String toString() {
        return "Output written to: " + location.getAbsolutePath();
    }
}

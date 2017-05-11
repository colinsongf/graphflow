package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Outputs query results to a file.
 */
public class FileOutputSink extends AbstractDBOperator {

    private File location;
    private PrintWriter writer;

    public FileOutputSink(File location) throws IOException {
        super(null /* no nextOperator, always last operator in the OneTimeMatchQueryPlan. */);
        this.location = location;
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(location, true)));
    }

    /**
     * Appends data from {@code matchQueryOutput} to the configured {@code File}.
     * @param result A result record to append to the sink.
     */
    @Override
    public void append(String result) {
        writer.println(result);
        writer.flush();
    }

    /**
     * Appends data from {@code matchQueryOutput} to the configured {@code File}.
     * @param matchQueryOutput A result record to append to the sink.
     */
    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        writer.println(Arrays.toString(matchQueryOutput.vertexIds) + " " + matchQueryOutput.
            matchQueryResultType.name());
        writer.flush();
    }

    @Override
    public String getHumanReadableOperator() {
        return "FileOutputSink:\n";
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonOperator = new JsonObject();
        jsonOperator.addProperty(JsonUtils.TYPE, JsonUtils.SINK);
        jsonOperator.addProperty(JsonUtils.NAME, this.getClass().getSimpleName());
        return jsonOperator;
    }

    @Override
    public String toString() {
        return "Output written to: " + location.getAbsolutePath();
    }
}

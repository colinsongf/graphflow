package ca.waterloo.dsg.graphflow.outputsink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Outputs query results to a file.
 */
public class FileOutputSink implements OutputSink {

    private File location;
    private PrintWriter writer;

    public FileOutputSink(File location) throws IOException {
        this.location = location;
        this.writer = new PrintWriter(new BufferedWriter(new FileWriter(location, true)));
    }

    /**
     * Appends the {@code result} to the configured {@code File}.
     *
     * @param result the output {@code String}.
     */
    @Override
    public void append(String result) {
        writer.println(result);
        writer.flush();
    }

    @Override
    public String toString() {
        return "Output written to: " + location.getAbsolutePath();
    }
}

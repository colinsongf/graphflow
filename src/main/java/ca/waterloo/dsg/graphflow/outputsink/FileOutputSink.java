package ca.waterloo.dsg.graphflow.outputsink;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * Outputs query results to a file.
 */
public class FileOutputSink implements OutputSink {

    private String fileName;
    private File directory;
    private PrintWriter writer;

    public FileOutputSink(File location, String fileName) {
        this.setDirectory(location);
        this.fileName = fileName;
    }

    /**
     * Creates the output file at the given directory.
     *
     * @param directory
     */
    public void setDirectory(File directory) {
        if (directory.isDirectory()) {
            this.directory = directory;
        } else {
            this.directory = directory.getParentFile();
        }
    }

    /**
     * Sets the given fileName as the filename of the output sink.
     *
     * @param fileName
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void append(int[][] results) {
        try {
            for (int[] result : results) {
                this.getWriter().println(Arrays.toString(result));
            }
            this.getWriter().flush();
        } catch (IOException e) {
            //TODO: write these to error log
            e.printStackTrace();
        }
    }

    /**
     * Returns a writer to the file specified by directory and fileName.
     * Initializes the writer first if necessary.
     *
     * @return PrintWriter
     * @throws IOException
     */
    private PrintWriter getWriter() throws IOException {
        if (this.writer == null) {
            this.writer = new PrintWriter(
                new BufferedWriter(new FileWriter(new File(this.directory, this.fileName), true)));
        }
        return this.writer;
    }
}

package ca.waterloo.dsg.graphflow.queryprocessor.outputsink;

import ca.waterloo.dsg.graphflow.util.IntArrayList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;

/**
 * outputs query results to a file.
 */
public class FileOutputSink implements OutputSink{

  private String name;
  private File location;
  private PrintWriter writer;

  public FileOutputSink(File location, String name) {

    this.setLocation(location);
    this.name = name;
  }

  /**
   * Returns a writer to the file specified by location and name
   * @return PrintWriter
   * @throws IOException
   */
  private PrintWriter getWriter() throws IOException{
    if(this.writer == null) {
      this.writer = new PrintWriter(
              new BufferedWriter(new FileWriter(new File(this.location, this.name), true))
      );
    }
    return this.writer;
  }

  /**
   * Creates the output file at the given location.
   * @param location
   */
  public void setLocation(File location) {
    if(location.isDirectory()) {
      this.location = location;
    } else {
      this.location = location.getParentFile();
    }
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void append(int[][] results) {
    try {
      for (int[] result: results) {
        this.getWriter().println(Arrays.toString(result));
      }
      this.getWriter().flush();

    } catch (IOException e) {
      //TODO: write these to error log
      e.printStackTrace();
    }
  }
}

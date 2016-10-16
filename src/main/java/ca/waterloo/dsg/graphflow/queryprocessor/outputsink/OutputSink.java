package ca.waterloo.dsg.graphflow.queryprocessor.outputsink;

import ca.waterloo.dsg.graphflow.util.IntArrayList;

/**
 * Interface for outputting query results.
 */
public interface OutputSink {

  /**
   *Sets a name that will be used to differentiate output sinks where applicable.
   * @param name
   */
  public void setName(String name);

  /**
   * Appends the given results to the output sink.
   * @param results
   */
  public void append(IntArrayList[] results);
}

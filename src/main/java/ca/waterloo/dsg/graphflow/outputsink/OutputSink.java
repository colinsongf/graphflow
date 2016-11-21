package ca.waterloo.dsg.graphflow.outputsink;

/**
 * Common interface for defining an output endpoint used to store messages or results.
 */
public interface OutputSink {

    void append(String result);
}

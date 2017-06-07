package ca.waterloo.dsg.graphflow.query.operator.sinks;

import ca.waterloo.dsg.graphflow.query.operator.AbstractOperator;
import ca.waterloo.dsg.graphflow.query.operator.FileOutputSink;
import ca.waterloo.dsg.graphflow.query.operator.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.query.operator.UDFSink;

/**
 * This operator encapsulates common functionality between the {@link InMemoryOutputSink},
 * {@link FileOutputSink}, and {@link UDFSink} operators.
 */
public class OutputSink extends AbstractOperator {

    public OutputSink() {
        super(null); /* an output sink is always the last operator */
    }

    /**
     * Appends a new {@link String} output to this operator.
     *
     * @param stringQueryOutput a {@link String} output.
     */
    public void append(String stringQueryOutput) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not " +
            "support the append(String stringQueryOutput) method.");
    }
}

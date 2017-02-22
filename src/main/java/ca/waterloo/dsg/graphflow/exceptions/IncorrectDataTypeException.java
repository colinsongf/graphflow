package ca.waterloo.dsg.graphflow.exceptions;

import ca.waterloo.dsg.graphflow.util.DataType;

/**
 * Thrown to indicate that there is an inconsistent or incorrect use of a {@link DataType} with a
 * given property key.
 */
public class IncorrectDataTypeException extends IllegalArgumentException {

    public IncorrectDataTypeException(String message) {
        super(message);
    }
}

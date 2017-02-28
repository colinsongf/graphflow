package ca.waterloo.dsg.graphflow.exceptions;

/**
 * Thrown to indicate that there is an inconsistent or incorrect use of a vertex type.
 */
public class IncorrectTypeException extends IllegalArgumentException {

    public IncorrectTypeException(String message) {
        super(message);
    }
}

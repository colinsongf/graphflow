package ca.waterloo.dsg.graphflow.exceptions;

/**
 * Thrown to indicate that there is an inconsistent or incorrect use of a vertex type.
 */
public class IncorrectVertexTypeException extends IllegalArgumentException {

    public IncorrectVertexTypeException(String message) {
        super(message);
    }
}

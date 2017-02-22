package ca.waterloo.dsg.graphflow.exceptions;

import java.util.NoSuchElementException;

/**
 * Thrown to indicate that a vertexID does not exist in the system.
 */
public class NoSuchVertexIDException extends NoSuchElementException {

    public NoSuchVertexIDException(String message) {
        super(message);
    }
}

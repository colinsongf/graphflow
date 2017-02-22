package ca.waterloo.dsg.graphflow.exceptions;

/**
 * Thrown to indicate that the MATCH statement is not well formed.
 */
public class MalformedMatchQueryException extends IllegalArgumentException {

    public MalformedMatchQueryException(String message) {
        super(message);
    }
}


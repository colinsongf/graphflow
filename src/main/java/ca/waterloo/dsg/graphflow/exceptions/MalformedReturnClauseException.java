package ca.waterloo.dsg.graphflow.exceptions;

/**
 * Thrown to indicate that the RETURN clause contains syntax and/or semantic errors.
 */
public class MalformedReturnClauseException extends IllegalArgumentException {

    public MalformedReturnClauseException(String message) {
        super(message);
    }
}

package ca.waterloo.dsg.graphflow.exceptions;

/**
 * Thrown to indicate that the RETURN statement contains variables that are not defined in the
 * query.
 */
public class MalformedReturnClauseException extends IllegalArgumentException {

    public MalformedReturnClauseException(String message) {
        super(message);
    }

    public MalformedReturnClauseException(Exception e) {
        super(e);
    }
}

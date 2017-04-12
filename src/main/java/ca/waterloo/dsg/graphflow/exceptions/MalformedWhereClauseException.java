package ca.waterloo.dsg.graphflow.exceptions;

/**
 * Thrown to indicate that the WHERE clause contains syntax and/or semantic errors.
 */
public class MalformedWhereClauseException extends IllegalArgumentException {

    public MalformedWhereClauseException(String message) {
        super(message);
    }
}
package ca.waterloo.dsg.graphflow.exceptions;

/**
 * Thrown to indicate that the WHERE statement contains variables that are not defined in the query.
 */
public class MalformedWhereClauseException extends IllegalArgumentException {

    public MalformedWhereClauseException(String message) {
        super(message);
    }
}
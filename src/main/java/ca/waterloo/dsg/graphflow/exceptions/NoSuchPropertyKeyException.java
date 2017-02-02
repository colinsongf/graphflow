package ca.waterloo.dsg.graphflow.exceptions;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;

import java.util.NoSuchElementException;

/**
 * Thrown to indicate a property key does not exist in the {@link TypeAndPropertyKeyStore}.
 */
public class NoSuchPropertyKeyException extends NoSuchElementException {
    public NoSuchPropertyKeyException(String message) { super(message); }
}

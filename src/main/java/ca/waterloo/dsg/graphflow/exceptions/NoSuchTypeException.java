package ca.waterloo.dsg.graphflow.exceptions;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;

import java.util.NoSuchElementException;

/**
 * Thrown to indicate a type does not exist in the {@link TypeAndPropertyKeyStore}.
 */
public class NoSuchTypeException extends NoSuchElementException {

    public NoSuchTypeException(String message) {
        super(message);
    }
}

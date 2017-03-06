package ca.waterloo.dsg.graphflow.exceptions;

/**
 * Thrown to indicate an error when serializing or deserializing the graph state.
 */
public class SerializationDeserializationException extends RuntimeException {

    public SerializationDeserializationException(String message) {
        super(message);
    }

    public SerializationDeserializationException(Exception e) {
        super(e);
    }
}

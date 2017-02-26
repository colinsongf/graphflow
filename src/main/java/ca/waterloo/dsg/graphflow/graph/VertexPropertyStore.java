package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;
import org.antlr.v4.runtime.misc.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Stores the properties of each vertex in the graph in serialized bytes.
 */
public class VertexPropertyStore extends PropertyStore {

    private static final VertexPropertyStore INSTANCE = new VertexPropertyStore();

    private static final int INITIAL_CAPACITY = 2;
    @VisibleForTesting
    byte[][] vertexProperties = null;

    /**
     * Empty private constructor enforces usage of the singleton object {@link #INSTANCE} for this
     * class.
     */
    private VertexPropertyStore() {
        reset();
    }

    /**
     * Overwrites all of the key-value pairs in the list at the given {@code vertexId} with the
     * given map. It does nothing if the map provided is {@code null}.
     *
     * @param vertexId The vertexID of the list at which the key-value pairs are overridden.
     * @param properties The map containing the key to {code DataType} and value pairs to override
     * those of the list at the given {@code vertexId}.
     */
    public void set(int vertexId, Map<Short, Pair<DataType, String>> properties) {
        vertexProperties = ArrayUtils.resizeIfNecessary(vertexProperties, vertexId + 1);
        if (null == properties) {
            return;
        }
        vertexProperties[vertexId] = serializeProperties(properties);
    }

    /**
     * Returns the {@code Short} key, and {@code Object} value pair properties of the vertex with
     * the given ID.
     * <p>
     * Warning: If the vertex ID is less than the highest created vertex ID, the vertex with the
     * given ID might still have never been created. The properties of a not yet created vertex
     * less than the highest created vertex ID returns an empty map.
     *
     * @param vertexId The ID of the vertex.
     * @return The properties of the edge as a Map<Short, Object> if properties are not null. If the
     * properties are {@code null}, an empty Map is returned.
     * @throws NoSuchElementException if the vertex with ID {@code vertexId} is larger than the
     * highest vertex ID previously created.
     */
    public Map<Short, Object> getProperties(int vertexId) {
        if (vertexId >= vertexProperties.length) {
            throw new NoSuchElementException("Vertex with ID " + vertexId + " does not exist.");
        }
        Map<Short, Object> properties = new HashMap<>();
        byte[] data = vertexProperties[vertexId];
        if (null == data) {
            return properties;
        }
        propertyIterator.reset(data, 0, data.length);
        Pair<Short, Object> keyValue;
        while (propertyIterator.hasNext()) {
            keyValue = propertyIterator.next();
            properties.put(keyValue.a, keyValue.b);
        }
        return properties;
    }

    /**
     * Given a vertex ID, and key, returns the property of the vertex with the given vertex ID
     * that has the given key. If the vertex does not contain a property with the given key, returns
     * null.
     * <p>
     * Warning: If the vertex ID is less than the highest created vertex ID, the vertex with the
     * given ID might still have never been created. The property with the given key of a not yet
     * created vertex returns null.
     *
     * @param vertexId ID of a vertex.
     * @param key key of a property.
     * @return the given vertex's property with the given key or null if no such property exists.
     * @throws NoSuchElementException if the vertex with ID {@code vertexId} is larger than the
     * highest vertex ID previously created.
     */
    public Object getProperty(int vertexId, short key) {
        if (vertexId >= vertexProperties.length) {
            throw new NoSuchElementException("Vertex with ID " + vertexId + " does not exist.");
        }
        byte[] data = vertexProperties[vertexId];
        if (null != data) {
            propertyIterator.reset(data, 0, data.length);
            return getPropertyFromIterator(key);
        }
        return null;
    }

    @VisibleForTesting
    public void reset() {
        vertexProperties = new byte[INITIAL_CAPACITY][];
    }

    public void serialize(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.writeObject(vertexProperties);
    }

    public void deserialize(ObjectInputStream objectInputStream) throws IOException,
        ClassNotFoundException {
        vertexProperties = (byte[][]) objectInputStream.readObject();
    }

    /**
     * Returns the singleton instance {@link #INSTANCE}.
     */
    public static VertexPropertyStore getInstance() {
        return INSTANCE;
    }
}

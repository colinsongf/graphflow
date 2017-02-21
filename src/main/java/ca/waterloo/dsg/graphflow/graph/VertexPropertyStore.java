package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Map;

/**
 * Stores the properties of each vertex in the graph in serialized bytes.
 * TODO(amine): Add methods to read the stored properties.
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
     * given map. It does nothing if the map provided is {@code null} or empty.
     *
     * @param vertexId The vertexID of the list at which the key-value pairs are overridden.
     * @param properties The map containing the key to {code DataType} and value pairs to
     * override those of the list at the given {@code vertexId}.
     */
    public void set(int vertexId, Map<Short, Pair<DataType, String>> properties) {
        vertexProperties = ArrayUtils.resizeIfNecessary(vertexProperties, vertexId + 1);
        if (null == properties) {
            return;
        }
        vertexProperties[vertexId] = serializeProperties(properties);
    }
    
    /**
     * Given a vertex ID, and key, returns the property of the vertex with the given vertex ID
     * that has the given key. If the vertex does not contain a property with the given key, returns
     * null.
     * 
     * @param vertexId ID of a vertex.
     * @param key key of a property.
     * @return the given vertex's property with the given key or null if no such property exists.
     */
    public Object getProperty(int vertexId, short key) {
        if (vertexId >= vertexProperties.length) {
            // Since we cannot tell whether the vertex does not exist in the graph or does not have
            // a property yet (in both cases vertexId would be >= vertexProperties.length), we
            // return null. 
            return null;
        }
        byte[] data = vertexProperties[vertexId];
        propertyIterator.reset(data, 0, data.length);
        return getPropertyFromIterator(key);
    }

    /**
     * Returns the singleton instance {@link #INSTANCE}.
     */
    public static VertexPropertyStore getInstance() {
        return INSTANCE;
    }
    
    @VisibleForTesting
    public void reset() {
        vertexProperties = new byte[INITIAL_CAPACITY][];
    }
}

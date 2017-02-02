package ca.waterloo.dsg.graphflow.graph;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.PackagePrivateForTesting;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Map;

/**
 * Stores the properties of each vertex in the graph in serialized bytes.
 * TODO(amine): Add methods to read the stored properties.
 */
public class VertexPropertyStore extends PropertyStore {

    private static final int INITIAL_CAPACITY = 2;
    @PackagePrivateForTesting
    byte[][] vertexProperties;

    /**
     * Creates {@link VertexPropertyStore} with default capacity.
     */
    public VertexPropertyStore() {
        vertexProperties = new byte[INITIAL_CAPACITY][];
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
}

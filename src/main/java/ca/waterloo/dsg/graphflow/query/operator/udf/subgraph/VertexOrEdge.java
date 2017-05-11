package ca.waterloo.dsg.graphflow.query.operator.udf.subgraph;

import java.util.Collections;
import java.util.Map;

/**
 * A base class for the {@link Edge} and {@link Vertex} .
 */
public abstract class VertexOrEdge {

    private String type;
    private Map<String, String> properties;

    VertexOrEdge(String type, Map<String, String> properties) {
        this.type = type;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}

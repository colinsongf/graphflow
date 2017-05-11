package ca.waterloo.dsg.graphflow.query.operator.udf.subgraph;

import java.util.Map;

/**
 * A vertex of the {@link Subgraph} containing a vertex Id, a type, and properties map of {@code
 * String} key to {@code String} values.
 */
public class Vertex extends VertexOrEdge {

    private int id;

    /**
     * @param id The vertex Id.
     * @param type The {@code String} vertex type.
     * @param properties The properties as a {@code Map<String, String>}.
     **/
    Vertex(int id, String type, Map<String, String> properties) {
        super(type, properties);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

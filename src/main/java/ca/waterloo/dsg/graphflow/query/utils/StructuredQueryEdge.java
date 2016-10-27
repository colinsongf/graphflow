package ca.waterloo.dsg.graphflow.query.utils;

/**
 * Simple edge representation used by {@code StructuredQuery}.
 */
public class StructuredQueryEdge {

    private String fromVertex;
    private String toVertex;

    public StructuredQueryEdge(String fromVertex, String toVertex) {
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }

    public String getFromVertex() {
        return fromVertex;
    }

    public String getToVertex() {
        return toVertex;
    }
}

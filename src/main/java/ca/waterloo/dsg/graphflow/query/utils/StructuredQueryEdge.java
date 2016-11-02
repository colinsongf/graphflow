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

    // Used for unit testing.
    public boolean equalsTo(StructuredQueryEdge that) {
        if (that == null) {     // Null check.
            return false;
        }
        return (this == that || (this.fromVertex.equals(that.fromVertex) && this.toVertex
            .equals(that.toVertex)));
    }
}

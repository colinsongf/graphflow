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

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The expected object.
     *
     * @return {@code true} if the current object values match perfectly with the expected object
     * values, {@code false} otherwise.
     */
    public boolean isSameAs(StructuredQueryEdge that) {
        return null != that && (this == that || (this.fromVertex.equals(that.fromVertex) &&
            this.toVertex.equals(that.toVertex)));
    }
}

package ca.waterloo.dsg.graphflow.query.parser;

/**
 * Simple edge representation used by {@code StructuredQuery}.
 */
public class Edge {

    private String fromVertex;
    private String toVertex;

    public Edge(String fromVertex, String toVertex) {
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

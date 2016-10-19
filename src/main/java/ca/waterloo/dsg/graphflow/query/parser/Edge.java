package ca.waterloo.dsg.graphflow.query.parser;

/**
 * Simple edge representation used by StructuredQuery.
 */
public class Edge {

    private String fromVertex;
    private String toVertex;

    public Edge(String fromVertex, String toVertex) {
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }

    public void setEdge(String fromVertex, String toVertex) {
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }

    public String getFromVertex() {
        return fromVertex;
    }

    public void setFromVertex(String fromVertex) {
        this.fromVertex = fromVertex;
    }

    public String getToVertex() {
        return toVertex;
    }

    public void setToVertex(String toVertex) {
        this.toVertex = toVertex;
    }
}

package ca.waterloo.dsg.graphflow.graphmodel;

/**
 * Represents an edge in the graph.
 * TODO(chathura): Use this class to return individual edges.
 */
public class Edge {

    private Vertex fromVertex;
    private Vertex toVertex;

    public Edge() { }

    public Edge(Vertex fromVertex, Vertex toVertex, Double weight) {
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;
    }

    @Override
    public String toString() {
        return fromVertex.getId() + " to " + toVertex.getId();
    }

    public Vertex getFromVertex() {
        return fromVertex;
    }

    public void setFromVertex(Vertex fromVertex) {
        this.fromVertex = fromVertex;
    }

    public Vertex getToVertex() {
        return toVertex;
    }

    public void setToVertex(Vertex toVertex) {
        this.toVertex = toVertex;
    }
}

package ca.waterloo.dsg.graphflow.graphmodel;

/**
 * Represents a vertex in the graph.
 * TODO(chathura): Use this to return a single vertex from graph.
 */
public class Vertex implements Comparable<Vertex> {

    private int id;

    public Vertex(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Vertex o) {
        return (new Integer(this.getId()).compareTo(o.getId()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

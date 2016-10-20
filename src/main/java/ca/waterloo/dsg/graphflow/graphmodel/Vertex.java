package ca.waterloo.dsg.graphflow.graphmodel;

/**
 * Represents a vertex in the graph.
 * TODO(chathura): Use this to return a single vertex from graph.
 */
public class Vertex implements Comparable<Vertex> {
    private String label;
    private int id;
    private String weight;

    public Vertex(String label) {
        this.label = label.toUpperCase();
    }

    public Vertex(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public Vertex(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(Vertex o) {
        return (new Integer(this.getId()).compareTo(o.getId()));
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}

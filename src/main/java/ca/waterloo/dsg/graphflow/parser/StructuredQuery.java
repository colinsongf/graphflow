package ca.waterloo.dsg.graphflow.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates edge data during the parse tree traversal.
 */
public class StructuredQuery {

    public enum Operation {
        CREATE,
        MATCH,
        DELETE,
        IMPORT
    }

    private List<String[]> edges;
    private Operation operation;

    public StructuredQuery() {
        this.edges = new ArrayList<>();
    }

    public List<String[]> getEdges() {
        return edges;
    }

    public void addEdge(String[] edge) {
        this.edges.add(edge);
    }

    public void addEdge(int pos, String[] vertex) {
        this.edges.add(pos, vertex);
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

}

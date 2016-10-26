package ca.waterloo.dsg.graphflow.query;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the query structure formed from parse tree traversal.
 */
public class StructuredQuery {

    private List<StructuredQueryEdge> structuredQueryEdges;
    private Operation operation;

    public enum Operation {
        CREATE,
        MATCH,
        DELETE
    }

    public StructuredQuery() {
        this.structuredQueryEdges = new ArrayList<>();
    }

    public List<StructuredQueryEdge> getStructuredQueryEdges() {
        return structuredQueryEdges;
    }

    public void addEdge(StructuredQueryEdge structuredQueryEdge) {
        this.structuredQueryEdges.add(structuredQueryEdge);
    }

    public void addEdge(int pos, StructuredQueryEdge structuredQueryEdge) {
        this.structuredQueryEdges.add(pos, structuredQueryEdge);
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}

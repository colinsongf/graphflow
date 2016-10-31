package ca.waterloo.dsg.graphflow.query.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the query structure formed from parse tree traversal.
 */
public class StructuredQuery {

    public enum Operation {
        CREATE,
        MATCH,
        DELETE
    }

    private List<StructuredQueryEdge> structuredQueryEdges;
    private Operation operation;

    public StructuredQuery() {
        this.structuredQueryEdges = new ArrayList<>();
    }

    public List<StructuredQueryEdge> getStructuredQueryEdges() {
        return structuredQueryEdges;
    }

    public void addEdge(StructuredQueryEdge structuredQueryEdge) {
        this.structuredQueryEdges.add(structuredQueryEdge);
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}

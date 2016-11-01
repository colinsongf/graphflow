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

    // Used for unit testing.
    public boolean equalsTo(StructuredQuery that) {
        if (that == null) {     // Null check.
            return false;
        }
        if (this == that) {     // Same object check.
            return true;
        }

        if (this.operation != that.operation) {
            return false;
        }

        if (this.structuredQueryEdges.size() != that.structuredQueryEdges.size()) {
            return false;
        }
        for (int i = 0; i < this.structuredQueryEdges.size(); i++) {
            if (!this.structuredQueryEdges.get(i).equalsTo(that.structuredQueryEdges.get(i))) {
                return false;
            }
        }

        return true;
    }
}

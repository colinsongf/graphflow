package ca.waterloo.dsg.graphflow.query.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class encapsulates the query structure formed from parse tree traversal.
 */
public class StructuredQuery {

    public enum QueryOperation {
        CREATE,
        MATCH,
        DELETE
    }

    private List<StructuredQueryEdge> structuredQueryEdges;
    private QueryOperation queryOperation;

    public StructuredQuery() {
        this.structuredQueryEdges = new ArrayList<>();
    }

    public List<StructuredQueryEdge> getStructuredQueryEdges() {
        return Collections.unmodifiableList(structuredQueryEdges);  // Return a read-only list.
    }

    public void addEdge(StructuredQueryEdge structuredQueryEdge) {
        this.structuredQueryEdges.add(structuredQueryEdge);
    }

    public QueryOperation getQueryOperation() {
        return queryOperation;
    }

    public void setQueryOperation(QueryOperation queryOperation) {
        this.queryOperation = queryOperation;
    }

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The expected object.
     *
     * @return {@code true} if the current object values match perfectly with the expected object
     * values, {@code false} otherwise.
     */
    public boolean isSameAs(StructuredQuery that) {
        if (null == that) {
            return false;
        }
        if (this == that) {
            return true;
        }
        if (this.queryOperation != that.queryOperation) {
            return false;
        }
        if (this.structuredQueryEdges.size() != that.structuredQueryEdges.size()) {
            return false;
        }
        for (int i = 0; i < this.structuredQueryEdges.size(); i++) {
            if (!this.structuredQueryEdges.get(i).isSameAs(that.structuredQueryEdges.get(i))) {
                return false;
            }
        }
        return true;
    }
}

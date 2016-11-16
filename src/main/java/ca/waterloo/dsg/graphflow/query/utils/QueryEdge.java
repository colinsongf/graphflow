package ca.waterloo.dsg.graphflow.query.utils;

import java.util.Objects;

/**
 * Represents an edge in the {@code QueryGraph}
 */
public class QueryEdge {
    public String toVariable;
    public String fromVariable;

    public QueryEdge(String fromVariable, String toVariable) {
        this.fromVariable = fromVariable;
        this.toVariable = toVariable;
    }

    @Override
    public int hashCode() {
        return Objects.hash(toVariable, fromVariable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryEdge queryEdge = (QueryEdge) o;
        if (null != toVariable ? !toVariable.equals(queryEdge.toVariable) : null != queryEdge
            .toVariable) {
            return false;
        }
        return fromVariable != null ? fromVariable.equals(queryEdge.fromVariable) : null ==
            queryEdge.fromVariable;
    }

    @Override
    public String toString() {
        return this.fromVariable + "->" + this.toVariable;
    }
}

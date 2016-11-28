package ca.waterloo.dsg.graphflow.query.utils;

import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class encapsulates the query structure formed by parsing a {@code String} query.
 */
public class StructuredQuery implements AbstractStructuredQuery {

    public enum QueryOperation {
        CREATE,
        MATCH,
        DELETE,
        SHORTEST_PATH,
        CONTINUOUS_MATCH
    }

    private List<QueryEdge> queryEdges = new ArrayList<>();
    private QueryOperation queryOperation;
    private String continuousMatchAction;
    private String continuousMatchOutputLocation;

    /**
     * @return A read-only list of query edges.
     */
    public List<QueryEdge> getQueryEdges() {
        return Collections.unmodifiableList(queryEdges);
    }

    /**
     * Adds a {@link QueryEdge} to the list of edges.
     *
     * @param queryEdge The {@link QueryEdge} to be added.
     */
    public void addEdge(QueryEdge queryEdge) {
        this.queryEdges.add(queryEdge);
    }

    public QueryOperation getQueryOperation() {
        return queryOperation;
    }

    public void setQueryOperation(QueryOperation queryOperation) {
        this.queryOperation = queryOperation;
    }

    public String getContinuousMatchAction() {
        return continuousMatchAction;
    }

    public void setContinuousMatchAction(String continuousMatchAction) {
        this.continuousMatchAction = continuousMatchAction;
    }

    public String getContinuousMatchOutputLocation() {
        return continuousMatchOutputLocation;
    }

    public void setContinuousMatchOutputLocation(String continuousMatchOutputLocation) {
        this.continuousMatchOutputLocation = continuousMatchOutputLocation;
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a The actual object.
     * @param b The expected object.
     * @return {@code true} if the {@code actual} object values are the same as the
     * {@code expected} object values, {@code false} otherwise.
     */
    @ExistsForTesting
    public static boolean isSameAs(StructuredQuery a, StructuredQuery b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (!(a.queryOperation == b.queryOperation &&
            Objects.equals(a.continuousMatchAction, b.continuousMatchAction) &&
            Objects.equals(a.continuousMatchOutputLocation, b.continuousMatchOutputLocation))) {
            return false;
        }
        if (a.queryEdges.size() != b.queryEdges.size()) {
            return false;
        }
        for (int i = 0; i < a.queryEdges.size(); i++) {
            if (!QueryEdge.isSameAs(a.queryEdges.get(i), b.queryEdges.get(i))) {
                return false;
            }
        }
        return true;
    }
}

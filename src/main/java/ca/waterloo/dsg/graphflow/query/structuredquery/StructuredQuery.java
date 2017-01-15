package ca.waterloo.dsg.graphflow.query.structuredquery;

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

    private List<QueryRelation> queryRelations = new ArrayList<>();
    private QueryOperation queryOperation;
    private String continuousMatchAction;
    private String continuousMatchOutputLocation;

    /**
     * @return A read-only list of query relations.
     */
    public List<QueryRelation> getQueryRelations() {
        return Collections.unmodifiableList(queryRelations);
    }

    /**
     * Adds a {@link QueryRelation} to the list of relations.
     *
     * @param queryRelation The {@link QueryRelation} to be added.
     */
    public void addRelation(QueryRelation queryRelation) {
        this.queryRelations.add(queryRelation);
    }

    public QueryOperation getQueryOperation() {
        return queryOperation;
    }

    public void setQueryOperation(QueryOperation queryOperation) {
        this.queryOperation = queryOperation;
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
     * @param a One of the objects.
     * @param b The other object.
     * @return {@code true} if the {@code a} object values are the same as the
     * {@code b} object values, {@code false} otherwise.
     */
    @ExistsForTesting
    public static boolean isSameAs(StructuredQuery a, StructuredQuery b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (!(a.queryOperation == b.queryOperation &&
            Objects.equals(a.continuousMatchAction, b.continuousMatchAction) &&
            Objects.equals(a.continuousMatchOutputLocation, b.continuousMatchOutputLocation))) {
            return false;
        }
        if (a.queryRelations.size() != b.queryRelations.size()) {
            return false;
        }
        for (int i = 0; i < a.queryRelations.size(); i++) {
            if (!QueryRelation.isSameAs(a.queryRelations.get(i), b.queryRelations.get(i))) {
                return false;
            }
        }
        return true;
    }
}

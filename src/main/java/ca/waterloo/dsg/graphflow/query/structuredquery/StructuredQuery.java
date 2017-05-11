package ca.waterloo.dsg.graphflow.query.structuredquery;

import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import org.antlr.v4.runtime.misc.Pair;

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
        CONTINUOUS_MATCH,
        LOAD_GRAPH,
        SAVE_GRAPH,
        EXPLAIN,
        CONTINUOUS_EXPLAIN
    }

    private List<QueryRelation> queryRelations = new ArrayList<>();
    private List<QueryVariable> queryVariables = new ArrayList<>();
    private List<String> returnVariables = new ArrayList<>();
    private List<Pair<String, String>> returnVariablePropertyPairs = new ArrayList<>();
    private List<QueryAggregation> queryAggregations = new ArrayList<>();
    private QueryOperation queryOperation;
    private String continuousMatchAction;
    private String continuousMatchOutputLocation;
    private String filePath;
    private List<QueryPropertyPredicate> queryPropertyPredicates = new ArrayList<>();

    /**
     * @return A read-only list of query relations.
     */
    public List<QueryRelation> getQueryRelations() {
        return Collections.unmodifiableList(queryRelations);
    }

    /**
     * @return A read-only list of query variables.
     */
    public List<QueryVariable> getQueryVariables() {
        return Collections.unmodifiableList(queryVariables);
    }

    /**
     * Returns a read-only list of {@link QueryPropertyPredicate}s.
     *
     * @return List of {@link QueryPropertyPredicate}.
     */
    public List<QueryPropertyPredicate> getQueryPropertyPredicates() {
        return Collections.unmodifiableList(queryPropertyPredicates);
    }

    @UsedOnlyByTests
    public void setQueryPropertyPredicates(List<QueryPropertyPredicate> queryPropertyPredicates) {
        this.queryPropertyPredicates = queryPropertyPredicates;
    }

    /**
     * Adds a {@link QueryRelation} to the list of relations.
     *
     * @param queryRelation The {@link QueryRelation} to be added.
     */
    public void addRelation(QueryRelation queryRelation) {
        this.queryRelations.add(queryRelation);
    }

    /**
     * Adds a {@link QueryVariable} to the list of variables.
     *
     * @param queryVariable The {@link QueryVariable} to be added.
     */
    public void addVariable(QueryVariable queryVariable) {
        this.queryVariables.add(queryVariable);
    }

    /**
     * Adds a {@link QueryPropertyPredicate} to the list of query property predicates.
     *
     * @param queryPropertyPredicate The {@link QueryPropertyPredicate} to be added.
     */
    public void addQueryPropertyPredicate(QueryPropertyPredicate queryPropertyPredicate) {
        this.queryPropertyPredicates.add(queryPropertyPredicate);
    }

    public QueryOperation getQueryOperation() {
        return queryOperation;
    }

    public void setQueryOperation(QueryOperation queryOperation) {
        this.queryOperation = queryOperation;
    }

    public void addReturnVariable(String variable) {
        returnVariables.add(variable);
    }

    public List<String> getReturnVariables() {
        return returnVariables;
    }

    public void addReturnVariablePropertyPair(Pair<String, String> variableAndProperty) {
        returnVariablePropertyPairs.add(variableAndProperty);
    }

    public List<Pair<String, String>> getReturnVariablePropertyPairs() {
        return returnVariablePropertyPairs;
    }

    public List<QueryAggregation> getQueryAggregations() {
        return queryAggregations;
    }

    public void addQueryAggregation(QueryAggregation queryAggregation) {
        queryAggregations.add(queryAggregation);
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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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
    @UsedOnlyByTests
    public static boolean isSameAs(StructuredQuery a, StructuredQuery b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (!(a.queryOperation == b.queryOperation &&
            Objects.equals(a.continuousMatchAction, b.continuousMatchAction) &&
            Objects.equals(a.filePath, b.filePath))) {
            return false;
        }
        if (a.queryVariables.size() != b.queryVariables.size()) {
            return false;
        }
        for (int i = 0; i < a.queryVariables.size(); i++) {
            if (!QueryVariable.isSameAs(a.queryVariables.get(i), b.queryVariables.get(i))) {
                return false;
            }
        }
        if (a.queryRelations.size() != b.queryRelations.size()) {
            return false;
        }
        for (int i = 0; i < a.queryRelations.size(); i++) {
            if (!QueryRelation.isSameAs(a.queryRelations.get(i), b.queryRelations.get(i))) {
                return false;
            }
        }

        if (!isSameLists(a.returnVariables, b.returnVariables) ||
            !isSameLists(a.returnVariablePropertyPairs, b.returnVariablePropertyPairs)) {
            return false;
        }

        if (a.queryAggregations.size() != b.queryAggregations.size()) {
            return false;
        }
        for (int i = 0; i < a.queryAggregations.size(); ++i) {
            if (!QueryAggregation.isSameAs(a.queryAggregations.get(i),
                b.queryAggregations.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static <T> boolean isSameLists(List<T> listA, List<T> listB) {
        if (listA.size() != listB.size()) {
            return false;
        }
        for (int i = 0; i < listA.size(); ++i) {
            if (!listA.get(i).equals(listB.get(i))) {
                return false;
            }
        }
        return true;
    }
}

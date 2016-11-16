package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Delta Generic Join query plan generated from a match query consisting of
 * relations between variables.
 */
public class ContinuousMatchQueryPlan implements QueryPlan {

    private List<List<List<GenericJoinIntersectionRule>>> queries = new ArrayList<>();

    @Override
    public String execute(Graph graph) {
        //TODO: perform actual generic join query
        return graph.toString();
    }

    public List<List<List<GenericJoinIntersectionRule>>> getQueries() {
        return queries;
    }

    public void addQuery(List<List<GenericJoinIntersectionRule>> stages) {
        queries.add(stages);
    }

    public List<List<GenericJoinIntersectionRule>> getQuery(int index) {
        return queries.get(index);
    }

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The expected object.
     *
     * @return {@code true} if the current object values match perfectly with the expected object
     * values, {@code false} otherwise.
     */
    public boolean isSameAs(ContinuousMatchQueryPlan that) {
        if (null == that) {
            return false;
        }
        if (this == that) {
            return true;
        }
        if (this.queries.size() != that.queries.size()) {
            return false;
        }
        for (int i = 0; i < this.queries.size(); i++) {
            if (this.queries.get(i).size() != that.queries.get(i).size()) {
                return false;
            }
            for (int j = 0; j < this.queries.get(i).size(); j++) {
                if (this.queries.get(i).get(j).size() != that.queries.get(i).get(j).size()) {
                    return false;
                }
                for (int k = 0; k < this.queries.get(i).get(j).size(); k++) {
                    if (!this.queries.get(i).get(j).get(k).isSameAs(that.queries.get(i).get(j)
                        .get(k))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

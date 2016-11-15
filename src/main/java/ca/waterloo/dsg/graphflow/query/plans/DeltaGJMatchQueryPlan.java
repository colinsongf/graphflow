package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a generic join delta query plan with multiple rounds of queries using different
 * versions of the graph.
 */
public class DeltaGJMatchQueryPlan implements QueryPlan {

    private List<List<List<GenericJoinIntersectionRule>>> joinQueries = new ArrayList<>();

    @Override
    public String execute(Graph graph) {
        //TODO: perform actual generic join query
        return graph.toString();
    }

    public void addQuery(List<List<GenericJoinIntersectionRule>> joinQuery) {
        joinQueries.add(joinQuery);
    }

    public int getQueryCount() {
        return joinQueries.size();
    }

    public List<List<GenericJoinIntersectionRule>> getQuery(int index) {
        return joinQueries.get(index);
    }
}

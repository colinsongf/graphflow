package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.executors.DeltaGenericJoinIntersectionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a generic join delta query plan with multiple rounds of queries using different
 * versions of the graph.
 */
public class DeltaGenericJoinQueryPlan implements QueryPlan {

    private List<List<List<DeltaGenericJoinIntersectionRule>>> joinQueries = new ArrayList<>();

    @Override
    public String execute(Graph graph) {
        //TODO: perform actual generic join query
        return graph.getGraphString();
    }

    public void addQuery(List<List<DeltaGenericJoinIntersectionRule>> joinQuery) {
        joinQueries.add(joinQuery);
    }

    public int getQueryCount() {
        return joinQueries.size();
    }

    public List<List<DeltaGenericJoinIntersectionRule>> getQuery(int index) {
        return joinQueries.get(index);
    }
}

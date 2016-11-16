package ca.waterloo.dsg.graphflow.outputsink;

import ca.waterloo.dsg.graphflow.query.executors.MatchQueryResultType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Keeps output from a query as an in memory data structure.
 */
public class InMemoryOutputSink implements OutputSink {

    private Map<MatchQueryResultType, List<int[]>> results = new HashMap<>();

    @Override
    public void append(MatchQueryResultType matchQueryResultType, int[][] results) {
        if (!this.results.containsKey(matchQueryResultType)) {
            this.results.put(matchQueryResultType, new ArrayList<>());
        }
        Collections.addAll(this.results.get(matchQueryResultType), results);
    }

    public Set<MatchQueryResultType> getMatchQueryResultTypes() {
        return results.keySet();
    }

    public List<int[]> getResults(MatchQueryResultType matchQueryResultType) {
        if (!results.containsKey(matchQueryResultType)) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(results.get(matchQueryResultType));
    }
}

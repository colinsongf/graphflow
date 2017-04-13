package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the registered {@link ContinuousMatchQueryPlan}s to be executed when the graph database
 * changes.
 */
public class ContinuousMatchQueryExecutor {

    private static final ContinuousMatchQueryExecutor INSTANCE = new ContinuousMatchQueryExecutor();
    private List<ContinuousMatchQueryPlan> continuousMatchQueryPlans = new ArrayList<>();

    /**
     * Empty private constructor enforces usage of the singleton object {@link #INSTANCE} for this
     * class.
     */
    private ContinuousMatchQueryExecutor() {}

    /**
     * Adds a new {@link ContinuousMatchQueryPlan} to the list of registered plans.
     *
     * @param continuousMatchQueryPlan the new {@link ContinuousMatchQueryPlan}.
     */
    public void addContinuousMatchQueryPlan(ContinuousMatchQueryPlan continuousMatchQueryPlan) {
        this.continuousMatchQueryPlans.add(continuousMatchQueryPlan);
    }

    /**
     * Executes all the registered {@link ContinuousMatchQueryPlan}s.
     *
     * @param graph the {@link Graph} instance to use for the execution of the
     * {@link ContinuousMatchQueryPlan}s.
     */
    public void execute(Graph graph) {
        continuousMatchQueryPlans.forEach(plan -> plan.execute(graph));
    }

    /**
     * Returns the singleton instance {@link #INSTANCE} of {@link ContinuousMatchQueryExecutor}.
     */
    public static ContinuousMatchQueryExecutor getInstance() {
        return INSTANCE;
    }

    /**
     * Deletes all the registered CONTINUOUS MATCH queries.
     */
    public void reset() {
        continuousMatchQueryPlans.clear();
    }
}

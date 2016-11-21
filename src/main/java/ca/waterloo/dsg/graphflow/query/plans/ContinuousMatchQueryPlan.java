package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Delta Generic Join query plan generated from a match query consisting of
 * relations between variables.
 */
public class ContinuousMatchQueryPlan implements QueryPlan {

    /**
     * The continuous match query plan is stored as a list of {@code OneTimeMatchQueryPlan}s,
     * which together produce the complete output for the Delta Generic Join query.
     */
    private List<OneTimeMatchQueryPlan> oneTimeMatchQueryPlans = new ArrayList<>();
    private OutputSink outputSink;

    public ContinuousMatchQueryPlan(OutputSink outputSink) {
        this.outputSink = outputSink;
    }

    /**
     * Adds a new {@link OneTimeMatchQueryPlan} to the {@link ContinuousMatchQueryPlan}.
     *
     * @param oneTimeMatchQueryPlan the {@link OneTimeMatchQueryPlan} to be added.
     */
    public void addOneTimeMatchQueryPlan(OneTimeMatchQueryPlan oneTimeMatchQueryPlan) {
        oneTimeMatchQueryPlans.add(oneTimeMatchQueryPlan);
    }

    /**
     * Executes the {@link CreateQueryPlan}.
     *
     * @param graph the {@link Graph} instance to use during the plan execution.
     */
    public void execute(Graph graph) {
        for (OneTimeMatchQueryPlan oneTimeMatchQueryPlan : oneTimeMatchQueryPlans) {
            oneTimeMatchQueryPlan.execute(graph, outputSink);
        }
    }

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The expected object.
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
        if (this.oneTimeMatchQueryPlans.size() != that.oneTimeMatchQueryPlans.size()) {
            return false;
        }
        for (int i = 0; i < this.oneTimeMatchQueryPlans.size(); i++) {
            if (!this.oneTimeMatchQueryPlans.get(i).isSameAs(that.oneTimeMatchQueryPlans.get(i))) {
                return false;
            }
        }
        return true;
    }
}

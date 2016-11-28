package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

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
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a The actual object.
     * @param b The expected object.
     * @return {@code true} if the {@code actual} object values are the same as the
     * {@code expected} object values, {@code false} otherwise.
     */
    @ExistsForTesting
    public static boolean isSameAs(ContinuousMatchQueryPlan a, ContinuousMatchQueryPlan b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.oneTimeMatchQueryPlans.size() != b.oneTimeMatchQueryPlans.size()) {
            return false;
        }
        for (int i = 0; i < a.oneTimeMatchQueryPlans.size(); i++) {
            if (!OneTimeMatchQueryPlan.isSameAs(a.oneTimeMatchQueryPlans.get(i),
                b.oneTimeMatchQueryPlans.get(i))) {
                return false;
            }
        }
        return true;
    }
}

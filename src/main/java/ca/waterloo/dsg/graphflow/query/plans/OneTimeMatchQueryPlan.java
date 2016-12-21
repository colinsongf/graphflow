package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinExecutor;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.util.ExistsForTesting;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing plan for a MATCH operation.
 */
public class OneTimeMatchQueryPlan implements QueryPlan {

    private List<List<GenericJoinIntersectionRule>> stages = new ArrayList<>();

    public void addStage(List<GenericJoinIntersectionRule> stage) {
        this.stages.add(stage);
    }

    /**
     * Executes the {@link OneTimeMatchQueryPlan}.
     *
     * @param graph the {@link Graph} instance to use during the plan execution.
     * @param outputSink the {@link OutputSink} to which the execution output is written.
     */
    public void execute(Graph graph, OutputSink outputSink) {
        new GenericJoinExecutor(stages, outputSink, graph).execute();
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
    public static boolean isSameAs(OneTimeMatchQueryPlan a, OneTimeMatchQueryPlan b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (a.stages.size() != b.stages.size()) {
            return false;
        }
        for (int i = 0; i < a.stages.size(); i++) {
            if (a.stages.get(i).size() != b.stages.get(i).size()) {
                return false;
            }
            for (int j = 0; j < a.stages.get(i).size(); j++) {
                if (!GenericJoinIntersectionRule.isSameAs(a.stages.get(i).get(j),
                    b.stages.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}

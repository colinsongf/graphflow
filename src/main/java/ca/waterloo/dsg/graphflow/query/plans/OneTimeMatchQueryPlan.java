package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinExecutor;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;

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

    @Override
    public String toString() {
        StringBuilder plan = new StringBuilder();
        int i = 0;
        for (List<GenericJoinIntersectionRule> stage : stages) {
            plan.append("\nStage: ").append(i).append("\n");
            for (GenericJoinIntersectionRule rule : stage) {
                plan.append(rule.getPrefixIndex()).append(", ").append(rule.getDirection())
                    .append("\n");
            }
            i++;
        }
        return plan.toString();
    }

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The expected object.
     * @return {@code true} if the current object values match perfectly with the expected object
     * values, {@code false} otherwise.
     */
    public boolean isSameAs(OneTimeMatchQueryPlan that) {
        if (null == that) {
            return false;
        }
        if (this == that) {
            return true;
        }
        if (this.stages.size() != that.stages.size()) {
            return false;
        }
        for (int i = 0; i < this.stages.size(); i++) {
            if (this.stages.get(i).size() != that.stages.get(i).size()) {
                return false;
            }
            for (int j = 0; j < this.stages.get(i).size(); j++) {
                if (!this.stages.get(i).get(j).isSameAs(that.stages.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}

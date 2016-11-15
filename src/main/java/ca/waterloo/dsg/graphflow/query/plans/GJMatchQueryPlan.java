package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing plan for a MATCH operation.
 */
public class GJMatchQueryPlan implements QueryPlan {

    private List<List<GenericJoinIntersectionRule>> stages = new ArrayList<>();

    public void addStage(ArrayList<GenericJoinIntersectionRule> stage) {
        this.stages.add(stage);
    }

    @Override
    public String execute(Graph graph) {
        //TODO: perform actual generic join query
        return graph.getGraphString();
    }

    @Override
    public String toString() {
        StringBuilder plan = new StringBuilder();
        int i = 0;
        for (List<GenericJoinIntersectionRule> stage : stages) {
            plan.append("\nStage: ").append(i).append("\n");
            for (GenericJoinIntersectionRule rule : stage) {
                plan.append(rule.getPrefixIndex()).append(", ").append(rule.isForward()).append(
                    "\n");
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
    public boolean isSameAs(GJMatchQueryPlan that) {
        if (that == null) {
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

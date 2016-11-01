package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class representing plan for a MATCH operation.
 */
public class MatchQueryPlan implements QueryPlan {

    private List<List<GenericJoinIntersectionRule>> stages = new ArrayList<>();
    private List<String> orderedVariables = new ArrayList<>();

    @Override
    public String execute(Graph graph) {
        //TODO: perform actual generic join query
        return graph.getGraphString();
    }

    public Set<String> getAllOrderedVariables() {
        // Return a copy of the list.
        return orderedVariables.stream().collect(Collectors.toSet());
    }

    public String getOrderedVariableAt(int position) {
        return orderedVariables.get(position);
    }

    public void addOrderedVariable(String variable) {
        orderedVariables.add(variable);
    }

    public int getOrderedVariablesCount() {
        return orderedVariables.size();
    }

    public void addStage(ArrayList<GenericJoinIntersectionRule> stage) {
        this.stages.add(stage);
    }

    // Used for unit testing.
    @Override
    public boolean equalsTo(Object o) {
        if (o == null || this.getClass() != o.getClass()) {     // Null check.
            return false;
        }
        if (this == o) {     // Same object check.
            return true;
        }

        MatchQueryPlan that = (MatchQueryPlan) o;

        if (this.orderedVariables.size() != that.orderedVariables.size()) {
            return false;
        }
        for (int i = 0; i < this.orderedVariables.size(); i++) {
            if (!this.orderedVariables.get(i).equals(that.orderedVariables.get(i))) {
                return false;
            }
        }

        if (this.stages.size() != that.stages.size()) {
            return false;
        }
        for (int i = 0; i < this.stages.size(); i++) {
            if (this.stages.get(i).size() != that.stages.get(i).size()) {
                return false;
            }
            for (int j = 0; j < this.stages.get(i).size(); j++) {
                if (!this.stages.get(i).get(j).equalsTo(that.stages.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder plan = new StringBuilder();
        plan.append("Variables order: ").append(String.join(",", orderedVariables));
        int i = 0;
        for (List<GenericJoinIntersectionRule> stage : stages) {
            plan.append("\nStage: ").append(i).append("\n");
            for (GenericJoinIntersectionRule rule : stage) {
                plan.append(rule.getPrefixIndex()).append(", ").append(rule.isForward()).append("\n");
            }
            i++;
        }
        return plan.toString();
    }
}

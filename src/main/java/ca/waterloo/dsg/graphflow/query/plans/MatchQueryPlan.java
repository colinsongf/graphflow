package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing plan for a MATCH operation.
 */
public class MatchQueryPlan implements IQueryPlan {

    private ArrayList<ArrayList<GenericJoinIntersectionRule>> stages = new ArrayList<>();
    private List<String> orderedVariables = new ArrayList<>();

    @Override
    public String execute(Graph graph) {
        //TODO: perform actual generic join query
        return graph.getGraphString();
    }

    public List<String> getOrderedVariables() {
        return orderedVariables;
    }

    public void addStage(ArrayList<GenericJoinIntersectionRule> stage) {
        this.stages.add(stage);
    }

    @Override
    public String toString() {
        StringBuilder plan = new StringBuilder();
        plan.append("Variables order: " + String.join(",", orderedVariables));
        int i = 0;
        for (ArrayList<GenericJoinIntersectionRule> stage : stages) {
            plan.append("\nStage: " + i + "\n");
            for (GenericJoinIntersectionRule rule : stage) {
                plan.append(rule.getPrefixIndex() + ", " + rule.isForward() + "\n");
            }
            i++;
        }
        return plan.toString();
    }
}

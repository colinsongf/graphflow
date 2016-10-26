package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.genericjoin.GenericJoinIntersectionRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing plan for a MATCH operation.
 */
public class MatchQueryPlan implements IQueryPlan {

    ArrayList<ArrayList<GenericJoinIntersectionRule>> stages = new ArrayList<>();
    List<String> orderedVariables = new ArrayList<>();

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

    public String toString() {
        String plan = "Variables order: " + String.join(",", orderedVariables);
        int i = 0;
        for (ArrayList<GenericJoinIntersectionRule> stage : stages) {
            plan += "\nStage: " + i + "\n";
            for (GenericJoinIntersectionRule rule : stage) {
                plan += rule.getPrefixIndex() + ", " + rule.isForward() + "\n";
            }
            i++;
        }
        return plan;
    }
}

package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.demograph.Graph;
import ca.waterloo.dsg.graphflow.query.genericjoin.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.parser.StructuredQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing plan for a MATCH operation.
 */
public class MatchQueryPlan extends QueryPlan {

    ArrayList<ArrayList<GenericJoinIntersectionRule>> stages = new ArrayList<>();
    List<String> orderedVertexVariables = new ArrayList<>();

    public MatchQueryPlan(StructuredQuery structuredQuery) {
        super(structuredQuery);
    }

    @Override
    public String execute(Graph graph) {
        //TODO: perform actual generic join query
        return graph.getGraphString();
    }

    public List<String> getOrderedVertexVariables() {
        return orderedVertexVariables;
    }

    public void addOrderedVertexVariable(String orderedVertexVariable) {
        orderedVertexVariables.add(orderedVertexVariable);
    }

    public void addStage(ArrayList<GenericJoinIntersectionRule> stage) {
        this.stages.add(stage);
    }

    public String toString() {
        String plan = "Variables order: " + String.join(",", orderedVertexVariables);
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

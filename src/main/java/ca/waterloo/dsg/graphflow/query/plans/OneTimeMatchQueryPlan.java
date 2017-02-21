package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinExecutor;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing plan for a MATCH operation.
 */
public class OneTimeMatchQueryPlan extends AbstractDBOperator implements QueryPlan {

    /**
     * Constructs a new {@link OneTimeMatchQueryPlan} with null next operator.
     * 
     * TODO: This should go away once we converge on the best class structure for operators
     * and plans. 
     */
    public OneTimeMatchQueryPlan() {
        super(null);
    }

    private List<List<GenericJoinIntersectionRule>> stages = new ArrayList<>();

    public void addStage(List<GenericJoinIntersectionRule> stage) {
        this.stages.add(stage);
    }

    /**
     * Executes the {@link OneTimeMatchQueryPlan}.
     *
     * @param graph the {@link Graph} instance to use during the plan execution.
     */
    public void execute(Graph graph) {
        new GenericJoinExecutor(stages, nextOperator, graph).execute();
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
    @UsedOnlyByTests
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

    @Override
    protected String getHumanReadableOperator() {
        StringBuilder stringBuilder = new StringBuilder("OneTimeMatchQueryPlan: \n");
        for (int i = 0; i < stages.size(); ++i) {
            List<GenericJoinIntersectionRule> stage = stages.get(i);
            stringBuilder.append("\tStage " + i + "\n");
            for (GenericJoinIntersectionRule intersectionRule : stage) {
                stringBuilder.append("\t\t" + intersectionRule.toString() + "\n");
            }
        }
        return stringBuilder.toString();
    }
}

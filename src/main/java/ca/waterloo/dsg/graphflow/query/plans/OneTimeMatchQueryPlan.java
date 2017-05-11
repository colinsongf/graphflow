package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinExecutor;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing plan for a MATCH operation.
 */
public class OneTimeMatchQueryPlan extends AbstractDBOperator implements QueryPlan {

    private List<List<GenericJoinIntersectionRule>> stages = new ArrayList<>();
    private List<String> orderedVariables;
    private Map<String, Integer> variableIndicesMap;

    /**
     * Constructs a new {@link OneTimeMatchQueryPlan} with null next operator.
     * <p>
     * TODO: This should go away once we converge on the best class structure for operators
     * and plans.
     */
    public OneTimeMatchQueryPlan() {
        super(null);
    }

    public void addStage(List<GenericJoinIntersectionRule> stage) {
        this.stages.add(stage);
    }

    /**
     * Setter of {@code this.orderedVariables}.
     *
     * @param orderedVariables list of {@code String} variable symbols
     */
    public void setOrderedVariables(List<String> orderedVariables) {
        this.orderedVariables = orderedVariables;
        variableIndicesMap = new HashMap<>();
        for (int i = 0; i < orderedVariables.size(); ++i) {
            variableIndicesMap.put(orderedVariables.get(i), i);
        }
    }

    /**
     * Converts {@link OneTimeMatchQueryPlan} into JSON format
     *
     * @return {@code JsonArray} containing one or more {@code JsonObject}
     */
    @Override
    public JsonObject toJson() {
        // Add "name", "variableOrdering", and "sink" to {@code planJson}
        JsonObject planJson = new JsonObject();
        planJson.addProperty(JsonUtils.NAME, "Q");
        JsonArray variableOrdering = new JsonArray();
        orderedVariables.forEach((variable) -> variableOrdering.add(variable));
        planJson.add(JsonUtils.VAR_ORDERING, variableOrdering);

        // Construct "stages" and add it to {@code planJson}
        JsonArray stagesJson = new JsonArray();
        JsonArray stageJson;
        JsonObject ruleJson;
        GenericJoinIntersectionRule rule;
        for (int i = 0; i < stages.size(); ++i) {
            stageJson = new JsonArray();
            for (int j = 0; j < stages.get(i).size(); ++j) {
                rule = stages.get(i).get(j);
                ruleJson = new JsonObject();
                ruleJson.addProperty(JsonUtils.GRAPH_VERSION, rule.getGraphVersion().toString());
                ruleJson.addProperty(JsonUtils.VARIABLE, orderedVariables.get(rule
                    .getPrefixIndex()));
                ruleJson.addProperty(JsonUtils.DIRECTION, rule.getDirection().toString());
                Short edgeTypeFilter = rule.getEdgeTypeFilter();
                if (TypeAndPropertyKeyStore.ANY != edgeTypeFilter) {
                    ruleJson.addProperty(JsonUtils.EDGE_TYPE, TypeAndPropertyKeyStore.getInstance()
                        .mapShortToStringType(edgeTypeFilter));
                }
                Short fromVertexTypeFilter = rule.getFromVertexTypeFilter();
                if (TypeAndPropertyKeyStore.ANY != fromVertexTypeFilter) {
                    ruleJson.addProperty(JsonUtils.FROM_VERTEX_TYPE, TypeAndPropertyKeyStore
                        .getInstance().mapShortToStringType(fromVertexTypeFilter));
                }
                Short toVertexTypeFilter = rule.getToVertexTypeFilter();
                if (TypeAndPropertyKeyStore.ANY != toVertexTypeFilter) {
                    ruleJson.addProperty(JsonUtils.TO_VERTEX_TYPE, TypeAndPropertyKeyStore
                        .getInstance().mapShortToStringType(toVertexTypeFilter));
                }
                stageJson.add(ruleJson);
            }
            stagesJson.add(stageJson);
        }
        planJson.add(JsonUtils.STAGES, stagesJson);

        return planJson;
    }

    /**
     * Executes the {@link OneTimeMatchQueryPlan}.
     *
     * @param graph the {@link Graph} instance to use during the plan execution.
     */
    public void execute(Graph graph) {
        new GenericJoinExecutor(stages, variableIndicesMap, nextOperator).execute();
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
}

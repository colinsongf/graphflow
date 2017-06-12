package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.AbstractOperator;
import ca.waterloo.dsg.graphflow.query.operator.Scan;
import ca.waterloo.dsg.graphflow.query.operator.genericjoin.EdgeIntersectionRule;
import ca.waterloo.dsg.graphflow.query.operator.genericjoin.StageOperator;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing plan for a MATCH operation.
 */
public class OneTimeMatchQueryPlan implements QueryPlan {

    private List<String> orderedVariables;
    private Scan firstOperator;

    public Scan getFirstOperator() {
        return firstOperator;
    }

    public void setFirstOperator(StageOperator firstOperator) {
        this.firstOperator = (Scan) firstOperator;
    }

    /**
     * @return the last {@link StageOperator} in the execution pipeline.
     */
    private StageOperator getLastStageOperator() {
        StageOperator lastOperator = firstOperator;
        while (lastOperator.nextOperator != null &&
            lastOperator.nextOperator instanceof StageOperator) {
            lastOperator = (StageOperator) lastOperator.nextOperator;
        }
        return lastOperator;
    }

    /**
     * Setter of {@code this.orderedVariables}.
     *
     * @param orderedVariables list of {@code String} variable symbols
     */
    public void setOrderedVariables(List<String> orderedVariables) {
        this.orderedVariables = orderedVariables;
    }

    /**
     * Executes the {@link OneTimeMatchQueryPlan}.
     */
    public void execute() {
        firstOperator.execute();
    }

    /**
     * @return a String human readable representation of an operator and all of its next operators.
     */
    public String getHumanReadablePlan() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getHumanReadableGenericJoinOperators());
        int level = 1;
        AbstractOperator operator = getLastStageOperator().nextOperator;
        while (null != operator) {
            stringBuilder.append(getIndentedString("nextOperator -> " + operator.
                getHumanReadableOperator(), level++));
            operator = operator.nextOperator;
        }
        return stringBuilder.toString();
    }

    private String getHumanReadableGenericJoinOperators() {
        StringBuilder stringBuilder = new StringBuilder("OneTimeMatchQueryPlan: \n");
        StageOperator currentOperator = firstOperator;
        int stageCount = 0;
        while (currentOperator != null) {
            List<EdgeIntersectionRule> stage = currentOperator.getIntersectionRules();
            stringBuilder.append("\tStage ").append(stageCount).append("\n");
            for (EdgeIntersectionRule intersectionRule : stage) {
                stringBuilder.append("\t\t").append(intersectionRule.toString()).append("\n");
            }
            if (currentOperator.nextOperator instanceof StageOperator) {
                currentOperator = (StageOperator) currentOperator.nextOperator;
                stageCount++;
            } else {
                currentOperator = null;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * @return a {@code JsonArray} query plan
     */
    public JsonArray getJsonPlan() {
        JsonArray jsonPlans = new JsonArray();
        JsonObject jsonPlan = toJson();
        JsonArray nextOperators = new JsonArray();
        AbstractOperator operator = getLastStageOperator().nextOperator;
        while (null != operator) {
            nextOperators.add(operator.toJson());
            operator = operator.nextOperator;
        }
        jsonPlan.add(JsonUtils.NEXT_OPERATORS, nextOperators);
        jsonPlans.add(jsonPlan);
        return jsonPlans;
    }

    /**
     * Converts {@link OneTimeMatchQueryPlan} into JSON format
     *
     * @return {@code JsonArray} containing one or more {@code JsonObject}
     */
    private JsonObject toJson() {
        // Add "name", "variableOrdering", and "sink" to {@code planJson}
        JsonObject planJson = new JsonObject();

        planJson.addProperty(JsonUtils.NAME, "Q");
        JsonArray variableOrdering = new JsonArray();
        orderedVariables.forEach((variable) -> variableOrdering.add(variable));
        planJson.add(JsonUtils.VAR_ORDERING, variableOrdering);

        // Construct "stages" and add it to {@code planJson}
        JsonArray stagesJson = new JsonArray();
        StageOperator currentOperator = this.firstOperator;
        while (null != currentOperator) {
            List<EdgeIntersectionRule> stage = currentOperator.getIntersectionRules();
            JsonArray stageJson = new JsonArray();
            for (EdgeIntersectionRule rule : stage) {
                JsonObject ruleJson = new JsonObject();
                ruleJson.addProperty(JsonUtils.GRAPH_VERSION, rule.getGraphVersion().toString());
                ruleJson.addProperty(JsonUtils.VARIABLE, orderedVariables.get(rule.
                    getPrefixIndex()));
                ruleJson.addProperty(JsonUtils.DIRECTION, rule.getDirection().toString());
                Short edgeTypeFilter = rule.getEdgeTypeFilter();
                if (TypeAndPropertyKeyStore.ANY != edgeTypeFilter) {
                    ruleJson.addProperty(JsonUtils.EDGE_TYPE, TypeAndPropertyKeyStore.getInstance().
                        mapShortToStringType(edgeTypeFilter));
                }
                if (currentOperator instanceof Scan) {
                    Short fromVertexTypeFilter = ((Scan) currentOperator).getFromVertexTypeFilter();
                    if (TypeAndPropertyKeyStore.ANY != fromVertexTypeFilter) {
                        ruleJson.addProperty(JsonUtils.FROM_VERTEX_TYPE, TypeAndPropertyKeyStore.
                            getInstance().mapShortToStringType(fromVertexTypeFilter));
                    }
                }
                Short toVertexTypeFilter = currentOperator.getToVertexTypeFilter();
                if (TypeAndPropertyKeyStore.ANY != toVertexTypeFilter) {
                    ruleJson.addProperty(JsonUtils.TO_VERTEX_TYPE, TypeAndPropertyKeyStore.
                        getInstance().mapShortToStringType(toVertexTypeFilter));
                }
                stageJson.add(ruleJson);
            }
            stagesJson.add(stageJson);
            if (currentOperator.nextOperator instanceof StageOperator) {
                currentOperator = (StageOperator) currentOperator.nextOperator;
            } else {
                currentOperator = null;
            }
        }
        planJson.add(JsonUtils.STAGES, stagesJson);

        return planJson;
    }

    /**
     * @param numTabsToIndent the number of tab indentations to have.
     *
     * @return a String that indents each line of the given unindented String by {@code
     * numTabsToIndent} tabs.
     */
    private String getIndentedString(String unindentedString, int numTabsToIndent) {
        String indentation = "";
        for (int i = 0; i < numTabsToIndent; ++i) {
            indentation += "\t";
        }
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = unindentedString.split("\n");
        for (String line : lines) {
            stringBuilder.append(indentation).append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    private List<List<EdgeIntersectionRule>> getPlanStages() {
        List<List<EdgeIntersectionRule>> rules = new ArrayList<>();
        StageOperator currentOperator = firstOperator;
        rules.add(currentOperator.getIntersectionRules());
        while (currentOperator.nextOperator != null &&
            currentOperator.nextOperator instanceof StageOperator) {
            currentOperator = (StageOperator) currentOperator.nextOperator;
            rules.add(currentOperator.getIntersectionRules());
        }

        return rules;
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     *
     * @return {@code true} if the {@code a} object values are the same as the {@code b} object
     * values, {@code false} otherwise.
     */
    @UsedOnlyByTests
    public static boolean isSameAs(OneTimeMatchQueryPlan a, OneTimeMatchQueryPlan b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        List<List<EdgeIntersectionRule>> aStages = a.getPlanStages();
        List<List<EdgeIntersectionRule>> bStages = b.getPlanStages();
        if (aStages.size() != bStages.size()) {
            return false;
        }
        for (int i = 0; i < aStages.size(); i++) {
            if (aStages.get(i).size() != bStages.get(i).size()) {
                return false;
            }
            for (int j = 0; j < aStages.get(i).size(); j++) {
                if (!EdgeIntersectionRule.isSameAs(aStages.get(i).get(j),
                    bStages.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}

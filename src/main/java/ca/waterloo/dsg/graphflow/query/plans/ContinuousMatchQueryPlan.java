package ca.waterloo.dsg.graphflow.query.plans;

import ca.waterloo.dsg.graphflow.query.operator.AbstractOperator;
import ca.waterloo.dsg.graphflow.query.operator.UDFSink;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Delta Generic Join query plan generated from a match query consisting of
 * relations between variables.
 */
public class ContinuousMatchQueryPlan implements QueryPlan {

    /**
     * The continuous match query plan is stored as a list of {@code OneTimeMatchQueryPlan}s,
     * which together produce the complete output for the Delta Generic Join query.
     */
    private List<OneTimeMatchQueryPlan> oneTimeMatchQueryPlans = new ArrayList<>();
    private AbstractOperator outputSink;

    public ContinuousMatchQueryPlan(AbstractOperator outputSink) {
        this.outputSink = outputSink;
    }

    /**
     * Adds a new {@link OneTimeMatchQueryPlan} to the {@link ContinuousMatchQueryPlan}.
     *
     * @param oneTimeMatchQueryPlan the {@link OneTimeMatchQueryPlan} to be added.
     */
    public void addOneTimeMatchQueryPlan(OneTimeMatchQueryPlan oneTimeMatchQueryPlan) {
        oneTimeMatchQueryPlans.add(oneTimeMatchQueryPlan);
    }

    /**
     * @return a String human readable representation of {@code ContinuousMatchQueryPlan}
     */
    public String getHumanReadablePlan() {
        StringBuilder stringBuilder = new StringBuilder("ContinuousMatchQueryPlan: \n");
        for (int i = 0; i < oneTimeMatchQueryPlans.size(); i += 2) {
            stringBuilder.append("dQ").append(i / 2 + 1).append("\n");
            stringBuilder.append(oneTimeMatchQueryPlans.get(i).getHumanReadablePlan()).append("\n");
        }
        return stringBuilder.toString();
    }

    /**
     * Converts {@link ContinuousMatchQueryPlan} into JSON format
     *
     * @return {@code JsonArray} containing one or more {@code JsonObject}
     */
    public JsonArray getJsonPlan() {
        JsonArray jsonArray = new JsonArray();
        JsonObject oneTimeMatchQueryPlanJson;
        for (int i = 0; i < oneTimeMatchQueryPlans.size(); i += 2) {
            oneTimeMatchQueryPlanJson = (JsonObject) oneTimeMatchQueryPlans.get(i).getJsonPlan().
                get(0);
            oneTimeMatchQueryPlanJson.remove("name");
            oneTimeMatchQueryPlanJson.addProperty("name", "dQ" + (i / 2 + 1));
            jsonArray.add(oneTimeMatchQueryPlanJson);
        }
        return jsonArray;
    }

    /**
     * Executes the {@link CreateQueryPlan}.
     */
    public void execute() {
        for (OneTimeMatchQueryPlan oneTimeMatchQueryPlan : oneTimeMatchQueryPlans) {
            oneTimeMatchQueryPlan.execute();
        }
        if (outputSink instanceof UDFSink) {
            ((UDFSink) outputSink).executeUDF();
        }
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
    public static boolean isSameAs(ContinuousMatchQueryPlan a, ContinuousMatchQueryPlan b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (a.oneTimeMatchQueryPlans.size() != b.oneTimeMatchQueryPlans.size()) {
            return false;
        }
        for (int i = 0; i < a.oneTimeMatchQueryPlans.size(); i++) {
            if (!OneTimeMatchQueryPlan.isSameAs(a.oneTimeMatchQueryPlans.get(i),
                b.oneTimeMatchQueryPlans.get(i))) {
                return false;
            }
        }
        return true;
    }
}

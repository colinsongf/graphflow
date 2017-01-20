package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.executors.GenericJoinExecutor;
import ca.waterloo.dsg.graphflow.query.output.JsonOutputable;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Base class for various database operators, such as projections, as well as output sinks, such
 * as {@link FileOutputSink}. Contains the following methods and fields:
 * <ul>
 * <li> Different append methods that accept different query outputs that this operator will
 * process.
 * <li> A field for storing a possibly null next operator whose append methods this operator
 * should call. Database operators may generate further query outputs as they process the query
 * outputs that are appended to it.
 * </ul>
 */
public abstract class AbstractDBOperator implements JsonOutputable {

    @VisibleForTesting
    public AbstractDBOperator nextOperator;

    /**
     * Default constructor.
     *
     * @param nextOperator possibly null next database operator that this operator should append
     * query outputs to.
     */
    public AbstractDBOperator(AbstractDBOperator nextOperator) {
        this.nextOperator = nextOperator;
    }

    /**
     * @param nextOperator the {@link AbstractDBOperator} to which the
     * {@link GenericJoinExecutor}'s output is appended to.
     */
    public void setNextOperator(AbstractDBOperator nextOperator) {
        this.nextOperator = nextOperator;
    }

    /**
     * This method is called when the operator appending outputs to this operator will not append
     * any more outputs.
     */
    public void done() {
        if (null != nextOperator) {
            nextOperator.done();
        }
    }

    /**
     * Appends a new {@link MatchQueryOutput} output to this operator.
     *
     * @param matchQueryOutput a {@link MatchQueryOutput}.
     */
    public void append(MatchQueryOutput matchQueryOutput) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not " +
            "support the append(MatchQueryOutput matchQueryOutputs) method.");
    }

    /**
     * Appends a new {@link String} output to this operator.
     *
     * @param stringQueryOutput a {@link String} output.
     */
    public void append(String stringQueryOutput) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not " +
            "support the append(String stringQueryOutput) method.");
    }

    /**
     * @return a String human readable representation of an operator excluding its next operator.
     */
    protected abstract String getHumanReadableOperator();

    /**
     * @return a String human readable representation of an operator and all of its next operators.
     */
    public String getHumanReadablePlan() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getHumanReadableOperator());
        int level = 1;
        AbstractDBOperator operator = nextOperator;
        while (null != operator) {
            stringBuilder.append(getIndentedString("nextOperator -> " +
                operator.getHumanReadableOperator(), level++));
            operator = operator.nextOperator;
        }
        return stringBuilder.toString();
    }

    /**
     * @param numTabsToIndent the number of tab indentations to have.
     * @return a String that indents each line of the given unindented String by
     * {@code numTabsToIndent} tabs.
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

    /**
     * @return a {@code JsonArray} query plan
     */
    public JsonArray getJsonPlan() {
        JsonArray jsonPlans = new JsonArray();
        JsonObject jsonPlan = toJson();
        JsonArray nextOperators = new JsonArray();
        AbstractDBOperator operator = nextOperator;

        while (null != operator) {
            nextOperators.add(operator.toJson());
            operator = operator.nextOperator;
        }

        jsonPlan.add(JsonUtils.NEXT_OPERATORS, nextOperators);
        jsonPlans.add(jsonPlan);

        return jsonPlans;
    }

    /**
     * This method first converts a list of objects to a comma separated String that is: (1)
     * prefixed with a tab and the given prefix string and ends with a new line. Then it appends
     * the string to the given {@link StringBuilder}.
     *
     * @param stringBuilder {@link StringBuilder} object to append to.
     * @param objects The list of objects to convert to comma separated String.
     * @param prefix A String that will be the prefix of the appended String.
     */
    protected static <T> void appendListAsCommaSeparatedString(StringBuilder stringBuilder,
        List<T> objects, String prefix) {
        stringBuilder.append("\t").append(prefix).append(": {");
        if (null != objects) {
            for (Object object : objects) {
                stringBuilder.append(" " + object);
            }
        }
        stringBuilder.append(" }\n");
    }
}

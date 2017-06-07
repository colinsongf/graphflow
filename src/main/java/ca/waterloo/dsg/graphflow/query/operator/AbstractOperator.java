package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.query.output.JsonOutputable;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.StringJoiner;

/**
 * Base class for various database operators, such as projections, as well as output sinks.
 * Contains the following methods and fields:
 * <ul>
 * <li> Different append methods that accept different query outputs that this operator will
 * process.
 * <li> A field for storing a possibly null next operator whose append methods this operator
 * should call. Database operators may generate further query outputs as they process the query
 * outputs that are appended to it.
 * </ul>
 */
public abstract class AbstractOperator implements JsonOutputable {

    public AbstractOperator nextOperator;
    protected Graph graph = Graph.getInstance();

    /**
     * @param nextOperator possibly null next database operator that this operator should append
     * query outputs to.
     */
    public AbstractOperator(AbstractOperator nextOperator) {
        this.nextOperator = nextOperator;
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
     * This method is called when the operator appending outputs to this operator will not append
     * any more outputs.
     */
    public void finalizeOperator() {
        if (null != nextOperator) {
            nextOperator.finalizeOperator();
        }
    }

    public String getHumanReadableOperator() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not " +
            "support the getHumanReadableOperator() method.");
    }

    /**
     * Converts {@link OneTimeMatchQueryPlan} into JSON format
     *
     * @return {@code JsonArray} containing one or more {@code JsonObject}
     */
    @Override
    public JsonObject toJson() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not " +
            "support the getHumanReadableOperator() method.");
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
    static <T> void appendListAsCommaSeparatedString(StringBuilder stringBuilder,
        List<T> objects, String prefix) {
        StringJoiner stringJoiner = new StringJoiner(" ", "\t" + prefix + ": {", " }\n");
        if (null != objects) {
            for (Object object : objects) {
                stringJoiner.add(object.toString());
            }
        }
        stringBuilder.append(stringJoiner.toString());
    }
}

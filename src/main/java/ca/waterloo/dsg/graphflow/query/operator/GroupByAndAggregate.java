package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.operator.aggregator.AbstractAggregator;
import ca.waterloo.dsg.graphflow.query.operator.aggregator.CountStar;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import ca.waterloo.dsg.graphflow.util.StringToIntKeyMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.misc.Pair;

import java.util.List;
import java.util.Map.Entry;

/**
 * Operator for grouping MATCH query outputs by zero more keys and aggregating each group by one
 * or more values.
 */
public class GroupByAndAggregate extends PropertyReadingOperator {

    private static String GROUP_BY_KEY_DELIMETER = "-";

    private List<EdgeOrVertexPropertyDescriptor> valuesToGroupBy;
    private List<Pair<EdgeOrVertexPropertyDescriptor, AbstractAggregator>> valueAggregatorPairs;
    private StringToIntKeyMap groupByKeys;

    /**
     * Default constructor.
     *
     * @param nextOperator next operator to append outputs to.
     * @param valuesToGroupBy descriptions of the list of values to group by.
     * @param valueAggregatorPairs descriptions of the values to aggregate and the aggregator to
     * use for these values.
     */
    public GroupByAndAggregate(AbstractDBOperator nextOperator,
        List<EdgeOrVertexPropertyDescriptor> valuesToGroupBy,
        List<Pair<EdgeOrVertexPropertyDescriptor, AbstractAggregator>> valueAggregatorPairs) {
        super(nextOperator, valuesToGroupBy);
        this.valuesToGroupBy = valuesToGroupBy;
        this.valueAggregatorPairs = valueAggregatorPairs;
        this.groupByKeys = new StringToIntKeyMap();
    }

    @Override
    public void done() {
        for (Entry<String, Integer> groupByKeyAndIndex : groupByKeys.entrySet()) {
            String groupByKey = groupByKeyAndIndex.getKey();
            int index = groupByKeyAndIndex.getValue();
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append(groupByKey);
            for (Pair<EdgeOrVertexPropertyDescriptor, AbstractAggregator> valueAggregatorPair :
                valueAggregatorPairs) {
                stringBuilder.append(" " + valueAggregatorPair.b.getStringValue(index));
            }
            nextOperator.append(stringBuilder.toString());
        }
        nextOperator.done();
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        clearAndFillStringBuilder(matchQueryOutput, GROUP_BY_KEY_DELIMETER);
        String groupByKey = stringBuilder.toString();
        int index = groupByKeys.getKeyAsIntOrInsert(groupByKey);
        for (Pair<EdgeOrVertexPropertyDescriptor, AbstractAggregator> valueAggregatorPair :
            valueAggregatorPairs) {
            if (valueAggregatorPair.b instanceof CountStar) {
                valueAggregatorPair.b.aggregate(index, 1 /* we aggregate count(*) by 1 */);
                continue;
            }
            Object propertyOrId = getPropertyOrId(matchQueryOutput, valueAggregatorPair.a);
            valueAggregatorPair.b.aggregate(index, propertyOrId);
        }
    }

    @Override
    protected String getHumanReadableOperator() {
        StringBuilder stringBuilder = new StringBuilder("GroupByAndAggregate:\n");
        appendListAsCommaSeparatedString(stringBuilder, valuesToGroupBy, "valuesToGroupBy");
        appendListAsCommaSeparatedString(stringBuilder, valueAggregatorPairs,
            "valueAggregatorPairs");
        return stringBuilder.toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonOperator = new JsonObject();
        JsonArray jsonArguments = new JsonArray();

        JsonObject jsonArgument = new JsonObject();
        jsonArgument.addProperty(JsonUtils.NAME, "Values to Group-By");
        JsonArray jsonValuesToGroupBy = new JsonArray();
        for (int i = 0; i < valuesToGroupBy.size(); ++i) {
            jsonValuesToGroupBy.add(valuesToGroupBy.get(i).toJson());
        }
        jsonArgument.add(JsonUtils.VALUE, jsonValuesToGroupBy);
        jsonArguments.add(jsonArgument);

        jsonArgument = new JsonObject();
        jsonArgument.addProperty(JsonUtils.NAME, "Aggregator Pairs");
        JsonArray jsonAggregatorPairs = new JsonArray();
        for (int i = 0; i < valueAggregatorPairs.size(); ++i) {
            Pair<EdgeOrVertexPropertyDescriptor, AbstractAggregator> aggregatorPair =
                valueAggregatorPairs.get(i);
            if (aggregatorPair.a.toJson().getAsJsonPrimitive(JsonUtils.TYPE).getAsString().equals(
                JsonUtils.COUNT_STAR_DESCRIPTOR)) {
                jsonAggregatorPairs.add(aggregatorPair.b.toString());
            } else {
                jsonAggregatorPairs.add(aggregatorPair.toString());
            }
        }
        jsonArgument.add(JsonUtils.VALUE, jsonAggregatorPairs);
        jsonArguments.add(jsonArgument);

        jsonOperator.addProperty(JsonUtils.NAME, "Group-By & Aggregate (&Gamma;)");
        jsonOperator.add(JsonUtils.ARGS, jsonArguments);
        return jsonOperator;
    }
}

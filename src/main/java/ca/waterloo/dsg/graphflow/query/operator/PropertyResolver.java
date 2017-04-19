package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * An operator that takes a list of vertex and edge properties to look up in the
 * {@link VertexPropertyStore} and {@link EdgeStore}. When it is appended {@link MatchQueryOutput}s,
 * it resolves the properties and appends them to the next operator. Other vertices and edges
 * that do not have properties to resolve are also appended to the next operator with only their
 * IDs.
 * <p>
 * Note: For now this operator only appends String outputs to the next operator.
 */
public class PropertyResolver extends PropertyReadingOperator {

    private static String STRING_OUTPUT_DELIMITER = " ";

    /**
     * @see PropertyReadingOperator#PropertyReadingOperator(AbstractDBOperator, List).
     */
    public PropertyResolver(AbstractDBOperator nextOperator,
        List<EdgeOrVertexPropertyDescriptor> propertyDescriptors) {
        super(nextOperator, propertyDescriptors);
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        clearAndFillStringBuilder(matchQueryOutput, STRING_OUTPUT_DELIMITER);
        stringBuilder.append(" " + matchQueryOutput.matchQueryResultType.name());
        nextOperator.append(stringBuilder.toString());
    }

    @Override
    public String getHumanReadableOperator() {
        StringBuilder stringBuilder = new StringBuilder("PropertyResolver:\n");
        appendListAsCommaSeparatedString(stringBuilder, propertyDescriptors,
            "EdgeOrVertexPropertyDescriptors");
        return stringBuilder.toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonOperator = new JsonObject();

        JsonArray jsonArguments = new JsonArray();
        JsonObject jsonArgument = new JsonObject();
        jsonArgument.addProperty(JsonUtils.NAME, "Descriptors");
        JsonArray jsonDescriptors = new JsonArray();
        for (int i = 0; i < propertyDescriptors.size(); ++i) {
            EdgeOrVertexPropertyDescriptor descriptor = propertyDescriptors.get(i);
            jsonDescriptors.add(descriptor.toJson());
        }
        jsonArgument.add(JsonUtils.VALUE, jsonDescriptors);
        jsonArguments.add(jsonArgument);

        jsonOperator.addProperty(JsonUtils.NAME, "Property Resolver");
        jsonOperator.add(JsonUtils.ARGS, jsonArguments);
        return jsonOperator;
    }
}

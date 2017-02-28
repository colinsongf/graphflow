package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;

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

    private static String STRING_OUTPUT_DELIMETER = " ";

    /**
     * @see PropertyReadingOperator#PropertyReadingOperator(AbstractDBOperator, List).
     */
    public PropertyResolver(AbstractDBOperator nextOperator,
        List<EdgeOrVertexPropertyDescriptor> propertyDescriptors) {
        super(nextOperator, propertyDescriptors);
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        clearAndFillStringBuilder(matchQueryOutput, STRING_OUTPUT_DELIMETER);
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
}

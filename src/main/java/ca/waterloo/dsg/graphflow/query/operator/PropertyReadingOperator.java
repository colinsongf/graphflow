package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Base class for operators that need to possibly read properties of vertices and edges. Contains
 * methods that take in an {@link EdgeOrVertexPropertyDescriptor} and a {@link MatchQueryOutput}
 * and reads either the ID or a property of an edge or a vertex.
 */
public abstract class PropertyReadingOperator extends AbstractOperator {

    private static final Logger logger = LogManager.getLogger(PropertyReadingOperator.class);

    private static int MAX_STRING_BUILDER_LENGTH = 10000;
    protected StringBuilder stringBuilder = new StringBuilder();
    protected List<EdgeOrVertexPropertyDescriptor> propertyDescriptors;

    /**
     * Default constructor.
     *
     * @param nextOperator next operator to append outputs to.
     * @param propertyDescriptors a list of {@link EdgeOrVertexPropertyDescriptor} that indicate
     * which vertices and edges and/or their properties should be used by this operator.
     */
    public PropertyReadingOperator(AbstractOperator nextOperator,
        List<EdgeOrVertexPropertyDescriptor> propertyDescriptors) {
        super(nextOperator);
        this.propertyDescriptors = propertyDescriptors;
    }

    /**
     * Empties and constructs the {@link StringBuilder} field of this class with a {@link String}
     * that appends the values corresponding to the {@link #propertyDescriptors} in the given
     * {@link MatchQueryOutput}. Uses the delimiter to separate each value read from
     * {@link MatchQueryOutput}.
     *
     * @param matchQueryOutput {@link MatchQueryOutput} to read values from.
     * @param delimiter delimiter to use when appending the values read from the given {@link
     * MatchQueryOutput}.
     */
    protected void clearAndFillStringBuilder(MatchQueryOutput matchQueryOutput, String delimiter) {
        // The below code ensures that stringBuilder does not grow excessively.
        if (stringBuilder.length() > MAX_STRING_BUILDER_LENGTH) {
            stringBuilder = new StringBuilder();
        }
        stringBuilder.delete(0, stringBuilder.length());
        boolean isFirstDescriptor = true;
        for (EdgeOrVertexPropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (isFirstDescriptor) {
                isFirstDescriptor = false;
            } else {
                stringBuilder.append(delimiter);
            }
            stringBuilder.append(getPropertyOrId(matchQueryOutput, propertyDescriptor));
        }
    }

    /**
     * Reads a value from the given {@link MatchQueryOutput} according to the given
     * {@link EdgeOrVertexPropertyDescriptor}. Either returns a property of an edge or a vertex
     * or the ID of an edge or a vertex.
     *
     * @param matchQueryOutput {@link MatchQueryOutput} to read values from.
     * @param propertyDescriptor descriptor of which values to read from the given {@link
     * MatchQueryOutput}.
     *
     * @return the value from {@link MatchQueryOutput} specified by the given {@link
     * EdgeOrVertexPropertyDescriptor}.
     */
    protected Object getPropertyOrId(MatchQueryOutput matchQueryOutput,
        EdgeOrVertexPropertyDescriptor propertyDescriptor) {
        int index = propertyDescriptor.index;
        short key = propertyDescriptor.key;
        switch (propertyDescriptor.descriptorType) {
            case EDGE_ID:
                return matchQueryOutput.edgeIds[index];
            case EDGE_PROPERTY:
                return EdgeStore.getInstance().getProperty(matchQueryOutput.edgeIds[index], key);
            case VERTEX_ID:
                return matchQueryOutput.vertexIds[index];
            case VERTEX_PROPERTY:
                return VertexPropertyStore.getInstance().getProperty(
                    matchQueryOutput.vertexIds[index], key);
            default:
                logger.warn("Trying to read the property or id of a MatchQueryOutput using an"
                    + " EdgeOrVertexPropertyDescriptor with descriptorType: "
                    + propertyDescriptor.descriptorType + ". Returning -1.");
                return -1;
        }
    }
}

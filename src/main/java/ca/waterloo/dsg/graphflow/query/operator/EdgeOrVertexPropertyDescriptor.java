package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.operator.aggregator.CountStar;
import ca.waterloo.dsg.graphflow.query.output.JsonOutputable;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import com.google.gson.JsonObject;

/**
 * Represents the description of which vertex or edge IDs or properties should be used by an
 * operator.
 */
public class EdgeOrVertexPropertyDescriptor implements JsonOutputable {

    /**
     * Type of this descriptor, i.e., whether it describes a VERTEX_ID, VERTEX_PROPERTY, EDGE_ID,
     * or EDGE_PROPERTY. There is also a placeholder type that is used by the {@link CountStar}
     * aggregator.
     */
    public enum DescriptorType {
        EDGE_ID,
        EDGE_PROPERTY,
        VERTEX_ID,
        VERTEX_PROPERTY,
        COUNT_STAR_PLACEHOLDER,
    }

    /**
     * This dummy instance is intended for using in {@link GroupByAndAggregate} when aggregating
     * {@link CountStar}, which does not need to ready any edge or vertex property.
     */
    public static final EdgeOrVertexPropertyDescriptor COUNTSTAR_DUMMY_DESCRIPTOR =
        new EdgeOrVertexPropertyDescriptor(DescriptorType.COUNT_STAR_PLACEHOLDER, -1, (short) -1);

    public DescriptorType descriptorType;
    public int index;
    public short key;

    /**
     * Default constructor that sets the following fields.
     * <ul>
     * <li> isVertexOrVertexProperty if true then the operator should use either a vertex ID or
     * vertex property.
     * <li> index: the index into vertexIds or edgeIds of {@link MatchQueryOutput} to read
     * the ID of a vertex or edge.
     * <li> key: the key of the property to be used by the operator. When -1 only the ID of the
     * vertex or edge should be used.
     * </ul>
     *
     * @param descriptorType Whether this descriptor is describing a vertex ID/property or edge
     * ID/property.
     * @param index index of the vertex or edge in the vertexIds or edgeIds fields of {@link
     * MatchQueryOutput}.
     * @param key the short key of the vertex or edge property (or -1 if the descriptor is
     * describing an ID).
     */
    public EdgeOrVertexPropertyDescriptor(DescriptorType descriptorType, int index, short key) {
        this.descriptorType = descriptorType;
        this.index = index;
        this.key = key;
    }

    @Override
    public String toString() {
        if (COUNTSTAR_DUMMY_DESCRIPTOR == this) {
            return "DUMMY_" + this.getClass().getSimpleName();
        }
        return "isVertexOrVertexProperty: " + descriptorType + " index: " + index +
            " key: " + key;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonDescriptor = new JsonObject();
        if (COUNTSTAR_DUMMY_DESCRIPTOR == this) {
            jsonDescriptor.addProperty(JsonUtils.TYPE, JsonUtils.COUNT_STAR_DESCRIPTOR);
        } else {
            jsonDescriptor.addProperty(JsonUtils.TYPE, descriptorType.toString());
            jsonDescriptor.addProperty(JsonUtils.INDEX, index);
            jsonDescriptor.addProperty(JsonUtils.KEY, key);
        }
        return jsonDescriptor;
    }
}

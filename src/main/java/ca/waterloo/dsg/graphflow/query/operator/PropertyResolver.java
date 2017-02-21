package ca.waterloo.dsg.graphflow.query.operator;

import java.util.List;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;

/**
 * An operator that takes a list of vertex and edge properties to look up in the
 * {@link VertexPropertyStore} and {@link EdgeStore}. When it is appended {@link MatchQueryOutput}s,
 * it resolves the properties and appends them to the next operator. Other vertices and edges
 * that do not have properties to resolve are also appended to the next operator with only their
 * IDs.
 * 
 * Note: For now this operator only appends String outputs to the next operator. 
 */
public class PropertyResolver extends AbstractDBOperator {

    private StringBuilder stringBuilder = new StringBuilder();
    private static int MAX_STRING_BUILDER_LENGTH = 10000;
    private List<EdgeOrVertexPropertyIndices> edgeOrVertexPropertyIndices;

    /**
     * Default constructor.
     * 
     * @param nextOperator next operator to append outputs to.
     * @param edgeOrVertexPropertyIndices a list of {@link EdgeOrVertexPropertyIndices} that
     * indicate which vertices and edges should be output as IDs and which ones as their
     * properties.
     */
    public PropertyResolver(AbstractDBOperator nextOperator,
        List<EdgeOrVertexPropertyIndices> edgeOrVertexPropertyIndices) {
        super(nextOperator);
        this.edgeOrVertexPropertyIndices = edgeOrVertexPropertyIndices;
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        // The below code ensures that stringBuilder does not grow excessively.
        if (stringBuilder.length() > MAX_STRING_BUILDER_LENGTH) {
            stringBuilder = new StringBuilder();
        }
        stringBuilder.delete(0, stringBuilder.length());
        long id;
        short key;
        int index;
        Object property;
        boolean isVertexOrVertexProperty;
        for (EdgeOrVertexPropertyIndices edgeOrVertexPropertyIndex : edgeOrVertexPropertyIndices) {
            isVertexOrVertexProperty = edgeOrVertexPropertyIndex.isVertexOrVertexProperty;
            index = edgeOrVertexPropertyIndex.index;
            id = isVertexOrVertexProperty ? (long) matchQueryOutput.vertexIds[index] :
                matchQueryOutput.edgeIds[index];
            key = edgeOrVertexPropertyIndex.key;
            if (key >= 0) {
                property = isVertexOrVertexProperty ? VertexPropertyStore.getInstance()
                    .getProperty((int) id, key) : EdgeStore.getInstance().getProperty(id, key);
                stringBuilder.append(" "  + property);
            } else {
                stringBuilder.append(" " + id);
            }
        }
        stringBuilder.append(" " + matchQueryOutput.matchQueryResultType.name());
        nextOperator.append(stringBuilder.toString());
    }

    @Override
    public String getHumanReadableOperator() {
        StringBuilder stringBuilder = new StringBuilder("PropertyResolver:\n");
        appendListAsCommaSeparatedString(stringBuilder, edgeOrVertexPropertyIndices,
            "EdgeOrVertexPropertyIndices");
        return stringBuilder.toString();
    }
    
    /**
     * Represents the description of which vertex or edge IDs or properties should be output by
     * {@link PropertyResolver}.
     */
    public static class EdgeOrVertexPropertyIndices {
        public boolean isVertexOrVertexProperty;
        public int index;
        public short key;
        
        /**
         * Default constructor that sets the following fields.
         * <ul>
         *   <li> isVertexOrVertexProperty if true then will output either a vertex ID or property.
         *   <li> index: the index into vertexIds or edgeIds of {@link MatchQueryOutput} to read
         *        the ID of a vertex or edge.
         *   <li> key: the key of the property to return. When -1 only the ID of the vertex or
         *        edge is returned.
         * </ul>
         */
        public EdgeOrVertexPropertyIndices(boolean isVertexOrVertexProperty, int index, short key) {
            this.isVertexOrVertexProperty = isVertexOrVertexProperty;
            this.index = index;
            this.key = key;
        }
        
        @Override
        public String toString() {
            return "isVertexOrVertexProperty: " + isVertexOrVertexProperty + " index: " + index +
                " key: " + key; 
        }
    }
}

package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.output.JsonOutputable;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Given a {@link MatchQueryOutput} whose only vertexIds are set, resolves the edge IDs that are
 * needed in the query between some of these vertices. The edge IDs are only searched in the
 * {@link GraphVersion#PERMANENT}.
 */
public class EdgeIdResolver extends AbstractDBOperator {

    private Graph graph = Graph.getInstance();
    private List<SourceDestinationIndexAndType> srcDstVertexIndicesAndTypes;
    private long[] edgeIds;

    /**
     * Default constructor.
     *
     * @param nextOperator next operator to append {@link MatchQueryOutput}s to.
     * @param srcDstVertexIndicesAndTypes a list of {@link SourceDestinationIndexAndType}s. For
     * each (srcIndex, dstIndex, type) triple in the list, this operator will resolve the ID of
     * the edge between matchQueryOutput.vertexIds[srcIndex] and matchQueryOutput.
     * vertexIds[srcIndex] with the given type in each {@link MatchQueryOutput} appended to this
     * operator.
     */
    public EdgeIdResolver(AbstractDBOperator nextOperator,
        List<SourceDestinationIndexAndType> srcDstVertexIndicesAndTypes) {
        super(nextOperator);
        this.srcDstVertexIndicesAndTypes = srcDstVertexIndicesAndTypes;
        this.edgeIds = new long[srcDstVertexIndicesAndTypes.size()];
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        SourceDestinationIndexAndType srcDstVertexIndicesAndType;
        int srcId, dstId;
        for (int i = 0; i < srcDstVertexIndicesAndTypes.size(); ++i) {
            srcDstVertexIndicesAndType = srcDstVertexIndicesAndTypes.get(i);
            srcId = matchQueryOutput.vertexIds[srcDstVertexIndicesAndType.sourceIndex];
            dstId = matchQueryOutput.vertexIds[srcDstVertexIndicesAndType.destinationIndex];
            edgeIds[i] = graph.getEdgeIdFromGraph(srcId, dstId, srcDstVertexIndicesAndType.type);
        }
        matchQueryOutput.edgeIds = edgeIds;
        matchQueryOutput.srcDstVertexIndicesAndTypes = srcDstVertexIndicesAndTypes;
        nextOperator.append(matchQueryOutput);
    }

    @Override
    public String getHumanReadableOperator() {
        StringBuilder stringBuilder = new StringBuilder("EdgeIdResolver:\n");
        appendListAsCommaSeparatedString(stringBuilder, srcDstVertexIndicesAndTypes,
            "SourceDestinationIndexAndTypes");
        return stringBuilder.toString();
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonOperator = new JsonObject();

        JsonArray jsonArguments = new JsonArray();
        JsonObject jsonArgument = new JsonObject();
        jsonArgument.addProperty(JsonUtils.NAME, "Src & Dst Vertex Indices & Types");
        JsonArray jsonIndicesAndTypes = new JsonArray();
        for (int i = 0; i < srcDstVertexIndicesAndTypes.size(); ++i) {
            SourceDestinationIndexAndType indexAndType = srcDstVertexIndicesAndTypes.get(i);
            jsonIndicesAndTypes.add(indexAndType.toJson());
        }
        jsonArgument.add(JsonUtils.VALUE, jsonIndicesAndTypes);
        jsonArguments.add(jsonArgument);

        jsonOperator.addProperty(JsonUtils.NAME, "Edge ID Resolver");
        jsonOperator.add(JsonUtils.ARGS, jsonArguments);
        return jsonOperator;
    }

    /**
     * A triple storing a (sourceIndex, destinationIndex, type) for an edge in the prefix.
     */
    public static class SourceDestinationIndexAndType implements JsonOutputable {

        public int sourceIndex;
        public int destinationIndex;
        public short type;

        /**
         * @param sourceIndex index in vertexIds array to read the ID of the source vertex.
         * @param destinationIndex index in vertexIds array to read the ID of the destination
         * vertex.
         * @param type of the edge between vertexIds[sourceIndex] and
         * vertexIds[destinationIndex].
         */
        public SourceDestinationIndexAndType(int sourceIndex, int destinationIndex, short type) {
            this.sourceIndex = sourceIndex;
            this.destinationIndex = destinationIndex;
            this.type = type;
        }

        @Override
        public String toString() {
            return "(sourceIndex: " + sourceIndex + ", destinationIndex: " + destinationIndex
                + ", type: " + type + ")";
        }

        @Override
        public JsonObject toJson() {
            JsonObject jsonSrcDstIndexAndType = new JsonObject();
            jsonSrcDstIndexAndType.addProperty("Src Index", sourceIndex);
            jsonSrcDstIndexAndType.addProperty("Dest Index", destinationIndex);
            jsonSrcDstIndexAndType.addProperty("Type", type);
            return jsonSrcDstIndexAndType;
        }
    }
}

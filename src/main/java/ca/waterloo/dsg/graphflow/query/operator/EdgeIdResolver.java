package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;

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
            edgeIds[i] = graph.getEdgeIdFromPermanentGraph(srcId, dstId,
                srcDstVertexIndicesAndType.type);
        }
        matchQueryOutput.edgeIds = edgeIds;
        nextOperator.append(matchQueryOutput);
    }

    @Override
    public String getHumanReadableOperator() {
        StringBuilder stringBuilder = new StringBuilder("EdgeIdResolver:\n");
        appendListAsCommaSeparatedString(stringBuilder, srcDstVertexIndicesAndTypes,
            "SourceDestinationIndexAndTypes");
        return stringBuilder.toString();
    }

    /**
     * A triple storing a (sourceIndex, destinationIndex, type).
     */
    public static class SourceDestinationIndexAndType {

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
    }
}

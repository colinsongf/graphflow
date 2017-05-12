package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.EdgeStore;
import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.graph.VertexPropertyStore;
import ca.waterloo.dsg.graphflow.query.executors.MatchQueryResultType;
import ca.waterloo.dsg.graphflow.query.operator.EdgeIdResolver.SourceDestinationIndexAndType;
import ca.waterloo.dsg.graphflow.query.operator.udf.UDFAction;
import ca.waterloo.dsg.graphflow.query.operator.udf.UDFResolver;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Edge;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Edge.EdgeUpdate;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Subgraph;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Subgraph.SubgraphType;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.SubgraphFactory;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Vertex;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Acts as an output sink. Operators append {@link MatchQueryOutput} one at a time. For each
 * match output tuple, the udf method resolved by the {@link UDFResolver} is executed.
 */
public class UDFSink extends AbstractDBOperator {

    private static final Logger logger = LogManager.getLogger(UDFSink.class);

    private UDFAction udfObject;
    private List<Subgraph> subgraphList = new ArrayList<>();

    /**
     * @param udfObject The {@link UDFAction} to execute.
     */
    public UDFSink(UDFAction udfObject) {
        super(null /* No nextOperator, always last operator in the OneTimeMatchQueryPlan. */);
        this.udfObject = udfObject;
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        List<Vertex> vertices = getVertices(matchQueryOutput);
        List<Edge> edges = getEdges(matchQueryOutput);
        SubgraphType subgraphType = getSubgraphType(matchQueryOutput);
        subgraphList.add(SubgraphFactory.getSubgraph(vertices, edges, subgraphType,
            matchQueryOutput.vertexIndices));
    }

    @Override
    public void done() {
        if (subgraphList.size() > 0) {
            udfObject.evaluate(subgraphList);
        }
        subgraphList = new ArrayList<>();
    }

    private List<Vertex> getVertices(MatchQueryOutput matchQueryOutput) {
        List<Vertex> vertices = new ArrayList<>();
        for (int vertexId : matchQueryOutput.vertexIds) {
            vertices.add(SubgraphFactory.getVertex(vertexId, TypeAndPropertyKeyStore.getInstance().
                    mapShortToStringType(Graph.getInstance().getVertexTypes().get(vertexId)),
                VertexPropertyStore.getInstance().getPropertiesAsStrings(vertexId)));
        }
        return vertices;
    }

    private List<Edge> getEdges(MatchQueryOutput matchQueryOutput) {
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < matchQueryOutput.edgeIds.length; ++i) {
            SourceDestinationIndexAndType edgeInfo = matchQueryOutput.srcDstVertexIndicesAndTypes.
                get(i);
            int fromVertexId = matchQueryOutput.vertexIds[edgeInfo.sourceIndex];
            int toVertexId = matchQueryOutput.vertexIds[edgeInfo.destinationIndex];
            String type = null;
            if (TypeAndPropertyKeyStore.ANY != edgeInfo.type) {
                type = TypeAndPropertyKeyStore.getInstance().mapShortToStringType(edgeInfo.type);
            }
            Map<String, String> properties = EdgeStore.getInstance().getPropertiesAsStrings(
                matchQueryOutput.edgeIds[i]);
            EdgeUpdate edgeUpdate = getEdgeUpdate(fromVertexId, toVertexId, matchQueryOutput.
                matchQueryResultType);
            edges.add(SubgraphFactory.getEdge(fromVertexId, toVertexId, type, properties,
                edgeUpdate));
        }
        return edges;
    }

    private SubgraphType getSubgraphType(MatchQueryOutput matchQueryOutput) {
        if (MatchQueryResultType.EMERGED == matchQueryOutput.matchQueryResultType) {
            return SubgraphType.EMERGED;
        } else {
            return SubgraphType.DELETED;
        }
    }

    private EdgeUpdate getEdgeUpdate(int fromVertexId, int toVertexId,
        MatchQueryResultType matchQueryResultType) {
        List<int[]> diffEdges;
        if (MatchQueryResultType.EMERGED == matchQueryResultType) {
            diffEdges = Graph.getInstance().getDiffEdges(GraphVersion.DIFF_PLUS);
        } else {
            diffEdges = Graph.getInstance().getDiffEdges(GraphVersion.DIFF_MINUS);
        }
        for (int[] edge : diffEdges) {
            if (edge[0] == fromVertexId && edge[1] == toVertexId) {
                if (MatchQueryResultType.EMERGED == matchQueryResultType) {
                    return EdgeUpdate.INSERTION;
                } else {
                    return EdgeUpdate.DELETION;
                }
            }
        }
        return EdgeUpdate.NONE;
    }

    /**
     * @return a String human readable representation of an operator excluding its next operator.
     */
    protected String getHumanReadableOperator() {
        return "UDFSink: " + udfObject.getClass().getCanonicalName() + ".evaluate(Subgraph)";
    }

    @Override
    public JsonObject toJson() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not " +
            "support the toJson() method.");
    }
}

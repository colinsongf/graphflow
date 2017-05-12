package ca.waterloo.dsg.graphflow.query.operator.udf.subgraph;

import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Edge.EdgeUpdate;
import ca.waterloo.dsg.graphflow.query.operator.udf.subgraph.Subgraph.SubgraphType;

import java.util.List;
import java.util.Map;

/**
 * Used to create {@link Subgraph}, {@link Edge}, {@link Vertex} instances as they are read-only
 * and their creation logic is hidden.
 */
public class SubgraphFactory {

    public static Subgraph getSubgraph(List<Vertex> vertices, List<Edge> edges,
        SubgraphType subgraphType, Map<String, Integer> vertexIndices) {
        return new Subgraph(vertices, edges, subgraphType, vertexIndices);
    }

    public static Vertex getVertex(int id, String type, Map<String, String> properties) {
        return new Vertex(id, type, properties);
    }

    public static Edge getEdge(int fromVertexId, int toVertexId, String type, Map<String, String>
        properties, EdgeUpdate edgeUpdate) {
        return new Edge(fromVertexId, toVertexId, type, properties, edgeUpdate);
    }
}

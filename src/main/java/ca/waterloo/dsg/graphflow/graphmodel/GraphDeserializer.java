package ca.waterloo.dsg.graphflow.graphmodel;

import com.google.gson.*;

import java.lang.reflect.Type;

class GraphDeserializer implements JsonDeserializer<Graph> {

    @Override
    public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        System.out.println("Deserializing......");
        JsonObject rawGraph = json.getAsJsonObject();
        JsonArray edges = rawGraph.get("edges").getAsJsonArray();
        JsonArray vertices = rawGraph.get("vertices").getAsJsonArray();

        Graph graph = new Graph();
        for (JsonElement vertex : vertices) {
            JsonObject vertexObj = vertex.getAsJsonObject();
            graph.addVertice(new Vertex(vertexObj.get("id").getAsInt(), vertexObj.get("label").getAsString()));
        }

        for (JsonElement edge: edges) {
            JsonObject edgeObj = edge.getAsJsonObject();
            graph.addEdge(new Edge(
                    new Vertex(edgeObj.get("fromVertex").getAsInt()),
                    new Vertex(edgeObj.get("toVertex").getAsInt()),
                    edgeObj.get("weight").getAsDouble())
            );
        }

        return graph;
    }
}

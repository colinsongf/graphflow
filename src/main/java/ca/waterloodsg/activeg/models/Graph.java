package ca.waterloodsg.activeg.models;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by chathura on 10/2/16.
 */
public class Graph {

    private List<Edge> edges;
    private Set<Vertex> vertices;

    public Graph() {
        edges = new ArrayList<>();
        vertices = new TreeSet<>();
    }

    public static Graph getInstance(File file) throws IOException {
      
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new GraphDeserializer());

        Gson gson = gsonBuilder.create();

        System.out.println(file.getAbsolutePath());
        return gson.fromJson(new BufferedReader(new FileReader(file)), Graph.class);
    }

    public void addEdge(Edge edge) {
        getEdges().add(edge);
        getVertices().add(edge.getFromVertex());
        getVertices().add(edge.getToVertex());
    }

    public void addVertex(Vertex v) {
        getVertices().add(v);
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public static void printGraph(Graph g) {
        System.out.println("Vertices...");
        for (Vertex v : g.getVertices()) {
            System.out.print(v.getLabel() + " ");
        }
        System.out.println("");
        System.out.println("Edges...");
        for (Edge e : g.getEdges()) {
            System.out.println(e);
        }
    }

}

class GraphDeserializer implements JsonDeserializer<Graph> {

    @Override
    public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        System.out.println("Deserializing......");
        JsonObject rawGraph = json.getAsJsonObject();
        JsonArray edges = (JsonArray) rawGraph.get("edges").getAsJsonArray();
        JsonArray vertices = (JsonArray) rawGraph.get("vertices").getAsJsonArray();

        Graph graph = new Graph();
        for (JsonElement vertex : vertices) {
            JsonObject vertexObj = vertex.getAsJsonObject();
            graph.addVertex(new Vertex(vertexObj.get("id").getAsInt(), vertexObj.get("label").getAsString()));
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





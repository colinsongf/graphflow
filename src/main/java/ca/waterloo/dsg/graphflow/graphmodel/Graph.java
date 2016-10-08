package ca.waterloo.dsg.graphflow.graphmodel;

import com.google.gson.*;

import java.io.*;
import java.util.ArrayList;
import java.util.TreeSet;

public class Graph {

    private ArrayList<Edge> edges;
    private TreeSet<Vertex> vertices;

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

    public void addVertice(Vertex v) {
        getVertices().add(v);
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public TreeSet<Vertex> getVertices() {
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





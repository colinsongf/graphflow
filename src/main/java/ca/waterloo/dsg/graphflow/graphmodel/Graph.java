package ca.waterloo.dsg.graphflow.graphmodel;

import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Encapsulates the Graph representation and provides utility methods.
 */
public class Graph {

    private static final int DEFAULT_GRAPH_SIZE = 10;
    public static Graph graph;

    private SortedIntArrayList vertices;
    private SortedIntArrayList[] forwardAdjLists;
    private SortedIntArrayList[] reverseAdjLists;

    public Graph() {
        forwardAdjLists = new SortedIntArrayList[DEFAULT_GRAPH_SIZE];
        reverseAdjLists = new SortedIntArrayList[DEFAULT_GRAPH_SIZE];
    }

    public Graph(int vertexLength) {
        //TODO(chathura): Store vertices in seperate array and ensure none of hte adj lists are empty.
        forwardAdjLists = new SortedIntArrayList[vertexLength];
        reverseAdjLists = new SortedIntArrayList[vertexLength];
    }

    /**
     * Creates a graph object from given file.
     * @param file JSON file with pattern {"num-vertices": x, "edges" : [("src": 1, "dst": 2),
     *             ("src": 2, "dst": 3)...]}. Vertex indices are assumed to start from 0.
     * @return Graph
     * @throws IOException
     */
    public static Graph getInstance(File file) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new Graph.GraphDeserializer());
        Gson gson = gsonBuilder.create();
        graph = gson.fromJson(new BufferedReader(new FileReader(file)), Graph.class);
        return graph;
    }

    /**
     * Returns an already loaded graph or creates and returns an empty graph.
     * @return Graph
     */
    public static Graph getInstance() {
        if (graph == null) {
            graph = new Graph();
        }
        return graph;
    }

    /**
     * Adds a reference to array of destination/source indices for the given vertex.
     * @param vertexIndex
     * @param adjList
     * @param isForward
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setAdjacencyList(int vertexIndex, SortedIntArrayList adjList, boolean isForward)
        throws ArrayIndexOutOfBoundsException {
        if (isForward) {
            forwardAdjLists[vertexIndex] = adjList;
        } else {
            reverseAdjLists[vertexIndex] = adjList;
        }
    }

    /**
     * Returns an array of destination/source indices for the given vertex.
     * @param vertexIndex
     * @param isForward
     * @return SortedIntArrayList
     * @throws ArrayIndexOutOfBoundsException
     */
    public SortedIntArrayList getAdjacencyList(int vertexIndex, boolean isForward)
        throws ArrayIndexOutOfBoundsException {
        if (isForward) {
            return forwardAdjLists[vertexIndex];
        } else {
            return reverseAdjLists[vertexIndex];
        }
    }

    /**
     * Returns the size of the adjacency list for the given vertex in the given direction.
     * @param vertexIndex
     * @param isForward
     * @return
     */
    public int getAdjacencyListSize(int vertexIndex, boolean isForward) {
        int result;
        if (isForward) {
            result = forwardAdjLists[vertexIndex].size();
        } else {
            result = reverseAdjLists[vertexIndex].size();
        }
        return result;
    }

    /**
     * Returns the number of forwardAdjLists in graph.
     * @return int
     */
    public int getVertexCount() {
        return forwardAdjLists.length;
    }

    /**
     * Returns the forwardAdjLists adjacency list if forwardAdjLists is True,
     * else returns reverseAdjLists adjacency list.
     * @param isForward
     * @return SortedIntArrayList[]
     */
    public SortedIntArrayList[] getAllAdjLists(boolean isForward) {
        if (isForward) {
            return this.forwardAdjLists;
        } else {
            return this.reverseAdjLists;
        }
    }

    /**
     * Returns all the vertex indices in the graph as a list.
     * @return SortedIntArrayList
     */
    public SortedIntArrayList getVertices() {
        if (this.vertices == null) {
            vertices = new SortedIntArrayList(getVertexCount());
            for (int i = 0; i < this.getVertexCount(); i++) {
                vertices.add(i);
            }
        }
        return vertices;
    }

    /**
     * Convert the graph to a string.
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder graph = new StringBuilder();
        int index = 0;
        for (SortedIntArrayList adjList : this.getAllAdjLists(true)) {
            graph.append(index + " :");
            graph.append(adjList.toString());
            graph.append("\n");
            index++;
        }

        index = 0;
        for (SortedIntArrayList adjList : this.getAllAdjLists(false)) {
            graph.append(index + " :");
            graph.append(adjList.toString());
            graph.append("\n");
            index++;
        }

        return graph.toString();
    }

    /**
     * Used to deserialize graph from a json file with a vertex count and list of edge objects.
     */
    private static class GraphDeserializer implements JsonDeserializer<Graph> {

        private static final String EDGES = "edges";
        private static final String VERTICES = "num-vertices";
        private static final String SOURCE = "src";
        private static final String DESTINATION = "dst";

        /**
         * Gets the root of a json object and returns a graph object populated with data.
         * @param json handle to the root element of the json file.
         * @param typeOfT
         * @param context
         * @return Graph
         * @throws JsonParseException
         */
        @Override
        public Graph deserialize(JsonElement json, Type typeOfT,
                                 JsonDeserializationContext context) throws JsonParseException {
            JsonObject rawGraph = json.getAsJsonObject();
            JsonArray edges = rawGraph.get(GraphDeserializer.EDGES).getAsJsonArray();
            int vertices = rawGraph.get(GraphDeserializer.VERTICES).getAsInt();
            Graph graph = new Graph(vertices);
            //find the sizes of the adjacency lists in order to minimize initial sizes
            int[] edgeLengths = new int[vertices];
            int[] edgeLengthsReversed = new int[vertices];

            for (JsonElement edge : edges) {
                JsonObject edgeObj = edge.getAsJsonObject();
                edgeLengths[edgeObj.get(GraphDeserializer.SOURCE).getAsInt()]++;
                edgeLengthsReversed[edgeObj.get(GraphDeserializer.DESTINATION).getAsInt()]++;
            }

            for (int i = 0; i < vertices; i++) {
                SortedIntArrayList adjList = new SortedIntArrayList(edgeLengths[i]);
                SortedIntArrayList adjListReversed = new SortedIntArrayList(edgeLengthsReversed[i]);
                for (JsonElement edge : edges) {
                    JsonObject edgeObj = edge.getAsJsonObject();
                    if (edgeObj.get(GraphDeserializer.SOURCE).getAsInt() == i) {
                        adjList.add(edgeObj.get(GraphDeserializer.DESTINATION).getAsInt());
                    }

                    if (edgeObj.get(GraphDeserializer.DESTINATION).getAsInt() == i) {
                        adjListReversed.add(edgeObj.get(GraphDeserializer.SOURCE).getAsInt());
                    }
                }
                graph.setAdjacencyList(i, adjList, true);
                graph.setAdjacencyList(i, adjListReversed, false);
            }

            return graph;
        }

        //TODO: serialize function to write human readable graph
    }
}

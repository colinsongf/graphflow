package ca.waterloo.dsg.graphflow.graphmodel;

import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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
    private SortedIntArrayList[] outgoingAdjLists;
    private SortedIntArrayList[] incomingAdjLists;

    public Graph() {
        outgoingAdjLists = new SortedIntArrayList[DEFAULT_GRAPH_SIZE];
        incomingAdjLists = new SortedIntArrayList[DEFAULT_GRAPH_SIZE];
    }

    public Graph(int vertexLength) {
        // TODO(chathura): Store vertices in separate array and ensure none of the adj lists are
        // empty.
        outgoingAdjLists = new SortedIntArrayList[vertexLength];
        incomingAdjLists = new SortedIntArrayList[vertexLength];
        // Initialize the adjacency lists for each vertex with empty lists.
        for (int i = 0; i < vertexLength; i++) {
            outgoingAdjLists[i] = new SortedIntArrayList();
            incomingAdjLists[i] = new SortedIntArrayList();
        }
    }

    /**
     * Creates a graph object from the given file.
     *
     * @param file JSON file with pattern {"num-vertices": x, "edges" : [("src": 1, "dst": 2),
     * ("src": 2, "dst": 3)...]}. Vertex indices are assumed to start from 0.
     * @return Graph
     * @throws IOException
     */
    public static Graph createInstance(File file) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Graph.class, new Graph.GraphDeserializer());
        Gson gson = gsonBuilder.create();
        graph = gson.fromJson(new BufferedReader(new FileReader(file)), Graph.class);
        return graph;
    }

    /**
     * Sets the outgoing or incoming adjacency list of the vertex with given ID
     * to the given adjacency list.
     *
     * @param vertexIndex
     * @param adjList
     * @param isForward
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setAdjacencyList(int vertexIndex, SortedIntArrayList adjList, boolean isForward)
        throws ArrayIndexOutOfBoundsException {
        if (isForward) {
            outgoingAdjLists[vertexIndex] = adjList;
        } else {
            incomingAdjLists[vertexIndex] = adjList;
        }
    }

    /**
     * Returns an array of outgoing or incoming adjacency lists for the given vertex.
     *
     * @param vertexIndex
     * @param isForward
     * @return SortedIntArrayList
     * @throws ArrayIndexOutOfBoundsException
     */
    public SortedIntArrayList getAdjacencyList(int vertexIndex, boolean isForward)
        throws ArrayIndexOutOfBoundsException {
        SortedIntArrayList result = null;
        if (isForward) {
            if (outgoingAdjLists[vertexIndex] == null) {
                outgoingAdjLists[vertexIndex] = new SortedIntArrayList();
            }
            result = outgoingAdjLists[vertexIndex];
        } else {
            if (incomingAdjLists[vertexIndex] == null) {
                incomingAdjLists[vertexIndex] = new SortedIntArrayList();
            }
            result = incomingAdjLists[vertexIndex];
        }
        return result;
    }

    /**
     * Returns the size of the adjacency list for the given vertex in the given direction.
     *
     * @param vertexIndex
     * @param isForward
     * @return
     */
    public int getAdjacencyListSize(int vertexIndex, boolean isForward) {
        int result;
        if (isForward) {
            if (outgoingAdjLists[vertexIndex] == null) {
                outgoingAdjLists[vertexIndex] = new SortedIntArrayList();
            }
            result = outgoingAdjLists[vertexIndex].size();
        } else {
            if (incomingAdjLists[vertexIndex] == null) {
                incomingAdjLists[vertexIndex] = new SortedIntArrayList();
            }
            result = incomingAdjLists[vertexIndex].size();
        }
        return result;
    }

    /**
     * Returns the number of vertices in the graph.
     *
     * @return int
     */
    public int getVertexCount() {
        return outgoingAdjLists.length;
    }

    /**
     * Returns all the vertex indices in the graph as a list.
     *
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
     *
     * @return String
     */
    @Override
    public String toString() {
        StringBuilder graph = new StringBuilder();
        graph.append(this.convertAdjListsToString(true));
        graph.append(this.convertAdjListsToString(false));
        return graph.toString();
    }

    /**
     * Converts the set of adjacency lists in the given direction to a {@code string}
     *
     * @param isForward
     * @return
     */
    private String convertAdjListsToString(boolean isForward) {
        SortedIntArrayList[] adjLists = isForward ? this.outgoingAdjLists : this.incomingAdjLists;
        StringBuilder adjString = new StringBuilder();
        int index = 0;
        for (SortedIntArrayList adjList : adjLists) {
            adjString.append(index + " :");
            adjString.append(adjList.toString());
            adjString.append("\n");
            index++;
        }
        return adjString.toString();
    }

    /**
     * Used to deserialize graph from a json file with a vertex count and list of edge objects.
     */
    private static class GraphDeserializer implements JsonDeserializer<Graph> {

        private static final String EDGES = "edges";
        private static final String NUM_VERTICES = "num-vertices";
        private static final String SOURCE = "src";
        private static final String DESTINATION = "dst";

        /**
         * Gets the root of a json object and returns a graph object populated with data.
         *
         * @param json handle to the root element of the json file.
         * @param typeOfT is used when deserializing to a specific Type.
         * @param context
         * @return Graph
         * @throws JsonParseException
         */
        @Override
        public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
            JsonObject rawGraph = json.getAsJsonObject();
            JsonArray edges = rawGraph.get(GraphDeserializer.EDGES).getAsJsonArray();
            int numVertices = rawGraph.get(GraphDeserializer.NUM_VERTICES).getAsInt();
            Graph graph = new Graph(numVertices);

            for (JsonElement edge : edges) {
                JsonObject edgeObj = edge.getAsJsonObject();
                int src = edgeObj.get(GraphDeserializer.SOURCE).getAsInt();
                int dst = edgeObj.get(GraphDeserializer.DESTINATION).getAsInt();
                graph.getAdjacencyList(src, true).add(dst);
                graph.getAdjacencyList(dst, false).add(src);
            }
            return graph;
        }
        //TODO: serialize function to write human readable graph
    }
}

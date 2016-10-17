package ca.waterloo.dsg.graphflow.graphmodel;


import ca.waterloo.dsg.graphflow.util.IntArrayList;
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


  private IntArrayList[] forwardAdjLists;
  private IntArrayList[] reverseAdjLists;

  public Graph(int vertexLength) {
    //TODO(chathura): Store vertices in seperate array and ensure none of hte adj lists are empty.
    forwardAdjLists = new IntArrayList[vertexLength];
    reverseAdjLists = new IntArrayList[vertexLength];
  }

  /**
   * Creates a graph object from given file.
   * @param file
   * @return Graph
   * @throws IOException
   */
  public static Graph getInstance(File file) throws IOException {

    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(Graph.class, new Graph.GraphDeserializer());
    Gson gson = gsonBuilder.create();
    return gson.fromJson(new BufferedReader(new FileReader(file)), Graph.class);
  }

  /**
   * Adds a reference to array of destination/source indices for the given vertex.
   * @param vertexIndex
   * @param adjList
   * @param isForward
   * @throws ArrayIndexOutOfBoundsException
   */
  public void setAdjacencyList(int vertexIndex, IntArrayList adjList, boolean isForward) throws ArrayIndexOutOfBoundsException{

    if(isForward) {
      forwardAdjLists[vertexIndex] = adjList;
    } else {
      reverseAdjLists[vertexIndex] = adjList;
    }

  }

  /**
   * Returns an array of destination/source indices for the given vertex.
   * @param vertexIndex
   * @param isForward
   * @return IntArrayList
   * @throws ArrayIndexOutOfBoundsException
   */
  public IntArrayList getAdjacencyList(int vertexIndex, boolean isForward) throws ArrayIndexOutOfBoundsException {

    if(isForward) {
      return forwardAdjLists[vertexIndex];
    } else {
      return reverseAdjLists[vertexIndex];
    }
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
   *  else returns reverseAdjLists adjacency list.
   * @param isForward
   * @return IntArrayList[]
   */
  public IntArrayList[] getAllAdjLists(boolean isForward) {

    if(isForward) {
      return this.forwardAdjLists;
    } else {
      return this.reverseAdjLists;
    }
  }

  /**
   * Returns the vertices which have outgoing edges in the given direction.
   * @param isForward
   * @return IntArrayList
   */
  public IntArrayList getVertices(boolean isForward) {
    IntArrayList[] adjLists = isForward? forwardAdjLists: reverseAdjLists;
    IntArrayList vertices = new IntArrayList(this.getVertexCount());
    for(int j = 0; j < adjLists.length; j++) {
      IntArrayList adjList = adjLists[j];
      for(int i=0; i<adjList.size(); i++) {
        int vertex = adjList.get(i);
        if(vertices.search(vertex) < 0) {
          vertices.add(vertex);
        }
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
    for(IntArrayList adjList: this.getAllAdjLists(true)) {
      graph.append(index+" :");
      graph.append(adjList.toString());
      graph.append("\n");
      index++;
    }

    index = 0;
    for(IntArrayList adjList: this.getAllAdjLists(false)) {
      graph.append(index+" :");
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
    private static final String VERTICES = "vertices";
    private static final String SOURCE = "src";
    private static final String DESTINATION = "dst";
    /**
     * Gets the root of a json object and returns a graph object populated with data.
     * @param json
     * @param typeOfT
     * @param context
     * @return Graph
     * @throws JsonParseException
     */
    @Override
    public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

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

      for (int i=0; i<vertices; i++) {
        IntArrayList adjList = new IntArrayList(edgeLengths[i]);
        IntArrayList adjListReversed = new IntArrayList(edgeLengthsReversed[i]);
        for (JsonElement edge : edges) {
          JsonObject edgeObj = edge.getAsJsonObject();
          if(edgeObj.get(GraphDeserializer.SOURCE).getAsInt() == i) {
            adjList.add(edgeObj.get(GraphDeserializer.DESTINATION).getAsInt());
          }

          if(edgeObj.get(GraphDeserializer.DESTINATION).getAsInt() == i) {
            adjListReversed.add(edgeObj.get(GraphDeserializer.SOURCE).getAsInt());
          }
        }
        graph.setAdjacencyList(i, adjList, true);
        graph.setAdjacencyList(i, adjListReversed, false);
      }

      return graph;
    }
  }
}

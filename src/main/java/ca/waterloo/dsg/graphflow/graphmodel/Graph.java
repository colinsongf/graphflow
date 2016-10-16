package ca.waterloo.dsg.graphflow.graphmodel;


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
import java.util.ArrayList;

/**
 * Encapsulates the Graph representation and provides utility methods.
 */
public class Graph {


  private ArrayList<ArrayList<Integer>> forwardAdjLists;
  private ArrayList<ArrayList<Integer>> reverseAdjLists;

  public Graph(int vertexLength) {
    //TODO(chathura): Store vertices in seperate array and ensure none of hte adj lists are empty.
    forwardAdjLists = new ArrayList<>(vertexLength);
    reverseAdjLists = new ArrayList<>(vertexLength);
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
  public void setAdjacencyList(int vertexIndex, ArrayList<Integer> adjList, boolean isForward) throws ArrayIndexOutOfBoundsException{

    if(isForward) {
      forwardAdjLists.add(vertexIndex,adjList);
    } else {
      reverseAdjLists.add(vertexIndex,adjList);
    }

  }

  /**
   * Returns an array of destination/source indices for the given vertex.
   * @param vertexIndex
   * @param isForward
   * @return ArrayList<Integer>
   * @throws ArrayIndexOutOfBoundsException
   */
  public ArrayList<Integer> getAdjacencyList(int vertexIndex, boolean isForward) throws ArrayIndexOutOfBoundsException {

    if(isForward) {
      return forwardAdjLists.get(vertexIndex);
    } else {
      return reverseAdjLists.get(vertexIndex);
    }
  }

  /**
   * Returns the number of forwardAdjLists in graph.
   * @return int
   */
  public int getVertexCount() {
    return forwardAdjLists.size();
  }

  /**
   * Returns the forwardAdjLists adjacency list if forwardAdjLists is True,
   *  else returns reverseAdjLists adjacency list.
   * @param isForward
   * @return ArrayList<ArrayList<Integer>>
   */
  public ArrayList<ArrayList<Integer>> getVertices(boolean isForward) {

    if(isForward) {
      return this.forwardAdjLists;
    } else {
      return this.reverseAdjLists;
    }
  }

  /**
   * Convert the graph to a string.
   * @return String
   */
  @Override
  public String toString() {

    StringBuilder graph = new StringBuilder();
    int index = 0;
    for(ArrayList<Integer> adjList: this.getVertices(true)) {
      graph.append(index+" :");
      for(Integer i : adjList) {
        graph.append(i+", ");
      }
      graph.append("\n");
      index++;
    }

    index = 0;
    for(ArrayList<Integer> adjList: this.getVertices(false)) {
      graph.append(index+" :");
      for(Integer i : adjList) {
        graph.append(i+", ");
      }
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
        ArrayList<Integer> adjList = new ArrayList<>(edgeLengths[i]);
        ArrayList<Integer> adjListReversed = new ArrayList<>(edgeLengthsReversed[i]);
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

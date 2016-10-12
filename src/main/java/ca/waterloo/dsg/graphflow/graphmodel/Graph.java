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


  private ArrayList<ArrayList<Integer>> srcToDst;
  private ArrayList<ArrayList<Integer>> dstToSrc;

  public Graph(int vertexLength) {
    //TODO: Store vertices in seperate array and ensure none of hte adj lists are empty
    srcToDst = new ArrayList<>(vertexLength);
    dstToSrc = new ArrayList<>(vertexLength);
  }

  /**
   * Creates graph object from given file
   * @param file
   * @return
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
   * @param isSrcToDst
   * @throws ArrayIndexOutOfBoundsException
   */
  public void setAdjacencyList(int vertexIndex, ArrayList<Integer> adjList, boolean isSrcToDst) throws ArrayIndexOutOfBoundsException{

    if(isSrcToDst) {
      srcToDst.add(vertexIndex,adjList);
    } else {
      dstToSrc.add(vertexIndex,adjList);
    }

  }

  /**
   * Returns array of destination/source indices for the given vertex.
   * @param vertexIndex
   * @return ArrayList<Integer>
   * @throws ArrayIndexOutOfBoundsException
   */
  public ArrayList<Integer> getAdjacencyList(int vertexIndex, boolean isSrcToDst) throws ArrayIndexOutOfBoundsException {

    if(isSrcToDst) {
      return srcToDst.get(vertexIndex);
    } else {
      return dstToSrc.get(vertexIndex);
    }

  }


  /**
   * Returns number of srcToDst in graph.
   * @return
   */
  public int getVertexCount() {
    return srcToDst.size();
  }

  /**
   * Returns the srcToDst adj. list if srcToDst is True, else returns dstToSrc adj. list.
   * @param srcToDst
   * @return
   */
  public ArrayList<ArrayList<Integer>> getVertices(Boolean srcToDst) {
    if(srcToDst) {
      return this.srcToDst;
    } else {
      return this.dstToSrc;
    }

  }


  /**
   * Convert graph to string.
   * @return
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

    /**
     * Gets the root of a json object and returns a graph object populated with data.
     * @param json
     * @param typeOfT
     * @param context
     * @return
     * @throws JsonParseException
     */
    @Override
    public Graph deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

      JsonObject rawGraph = json.getAsJsonObject();
      JsonArray edges = rawGraph.get("edges").getAsJsonArray();
      int vertices = rawGraph.get("vertices").getAsInt();

      Graph graph = new Graph(vertices);

      //find the sizes of the adjacency lists in order to minimize initial sizes
      int[] edgeLengths = new int[vertices];
      int[] edgeLengthsReversed = new int[vertices];
      for (JsonElement edge : edges) {
        JsonObject edgeObj = edge.getAsJsonObject();
        edgeLengths[edgeObj.get("src").getAsInt()]++;
        edgeLengthsReversed[edgeObj.get("dst").getAsInt()]++;
      }

      for (int i=0; i<vertices; i++) {
        ArrayList<Integer> adjList = new ArrayList<>(edgeLengths[i]);
        ArrayList<Integer> adjListReversed = new ArrayList<>(edgeLengthsReversed[i]);
        for (JsonElement edge : edges) {
          JsonObject edgeObj = edge.getAsJsonObject();
          if(edgeObj.get("src").getAsInt() == i) {
            adjList.add(edgeObj.get("dst").getAsInt());
          }

          if(edgeObj.get("dst").getAsInt() == i) {
            adjListReversed.add(edgeObj.get("src").getAsInt());
          }
        }
        graph.setAdjacencyList(i, adjList, true);
        graph.setAdjacencyList(i, adjListReversed, false);
      }

      return graph;
    }
  }
}

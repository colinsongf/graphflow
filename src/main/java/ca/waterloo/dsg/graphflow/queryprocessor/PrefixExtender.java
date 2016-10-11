package ca.waterloo.dsg.graphflow.queryprocessor;


import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;


/**
 * Represents a single relation's contribution to the Generic Join algorithm.
 */
public class PrefixExtender {


  private final ArrayList<ArrayList<Integer>> prefixes;
  private final int prefixIndex;
  private final boolean isSrcToDst;
  private final Graph g;
  int[] prefixNodes;

  public PrefixExtender(ArrayList<ArrayList<Integer>> prefixes, int prefixIndex, boolean isSrcToDst, Graph g) {
    this.prefixes = prefixes;
    this.prefixIndex = prefixIndex;
    this.isSrcToDst = isSrcToDst;
    this.g = g;
  }

  /**
   * Returns the list of nodes relevant to the PrefixExtender's relation from the prefix instances
   * @return int[]
   */
  public int[] getPrefixNodes() {

    if(prefixNodes == null) {

      if(prefixes != null) {
        prefixNodes = new int[prefixes.size()];

        for(int i=0; i< prefixes.size();i++) {
          prefixNodes[i] =prefixes.get(i).get(prefixIndex);
        }
      } else if (prefixIndex <0) {
        prefixNodes = IntStream.range(0,g.getVertexCount()).toArray();
      }

    }
    return prefixNodes;
  }

  /**
   * Returns the count of extensions this relation can propose for the given prefix.
   * @return int
   */
  public int count() {

    int count = 0;
    for(int node : getPrefixNodes()) {
      if(g.getAdjacencyList(node, this.isSrcToDst).size()>0) {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns the possible extensions counted earlier.
   * @return ArrayList<Integer>
   */
  public ArrayList<Integer> propose() {

    ArrayList<Integer> proposals = new ArrayList<>();
    for(int node : this.getPrefixNodes()) {
      //System.out.println(node+ ": "+g.getAdjacencyList(node, this.isSrcToDst));
        proposals.addAll(g.getAdjacencyList(node, this.isSrcToDst));
      }

    return proposals;
  }

  /**
   * Returns the intersection of the given proposals and possible proposals from this relation
   * @return ArrayList<Integer>
   */
  public ArrayList<ArrayList<Integer>> intersect(ArrayList<Integer> proposals) {

    ArrayList<ArrayList<Integer>> intersections = new ArrayList<>();
    //TODO: use faster method to intersect
    //iterate over prefixes and find matching proposals from each adjacency list

    for (int node: this.getPrefixNodes()) {
      ArrayList<Integer> adjList = g.getAdjacencyList(node, isSrcToDst);
      ArrayList<Integer> intersectNodes = (ArrayList<Integer>)adjList.clone();
      intersectNodes.retainAll(proposals);
      intersections.add(intersectNodes);
      //look at using streams here

//      //create a new arraylist adding the extension to the prefix tuple
//      for(Integer node: intersectNodes) {
//        ArrayList<Integer> intersectTuple = (ArrayList<Integer>)prefix.clone();
//        intersectTuple.add(node);
//        intersections.add(intersectTuple);
//      }
    }
    System.out.println("Intersections: "+intersections);
    return intersections;
  }


}

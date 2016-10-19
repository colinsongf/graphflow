package ca.waterloo.dsg.graphflow.queryprocessor;


import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.util.IntArrayList;

import java.util.stream.IntStream;


/**
 * Represents a single relation's contribution to the Generic Join algorithm.
 */
public class PrefixExtender {

  private IntArrayList[] prefixes;
  private final int prefixIndex;
  private final boolean isForward;
  private final Graph g;
  int[] prefixNodes;

  public PrefixExtender(int prefixIndex, boolean isForward, Graph g) {
    this.prefixIndex = prefixIndex;
    this.isForward = isForward;
    this.g = g;
  }

  public PrefixExtender(IntArrayList[] prefixes, int prefixIndex, boolean isForward, Graph g) {
    this.prefixes = prefixes;
    this.prefixIndex = prefixIndex;
    this.isForward = isForward;
    this.g = g;
  }

  /**
   * Sets the prefixes that this extender will use.
   * @param prefixes
   */
  public void setPrefixes(IntArrayList[] prefixes) {
    this.prefixes = prefixes;
  }

  /**
   * Returns the list of nodes relevant to the PrefixExtender's relation from the prefix instances.
   * @return int[]
   */
  public int[] getPrefixNodes() {
    if(prefixNodes == null) {

      if(prefixes != null && prefixIndex>=0) {
        prefixNodes = new int[prefixes.length];

        for(int i=0; i< prefixes.length;i++) {
          prefixNodes[i] =prefixes[i].get(prefixIndex);
        }
      } else if (prefixIndex <0) {
        //if no prefix is given return all possible vertices, ensuring the largest possible selection of proposals
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
      if(g.getAdjacencyList(node, this.isForward).size()>0) {
        count++;
      }
    }
    return count;
  }

  /**
   * Returns the possible extensions counted earlier.
   * @return IntArrayList
   */
  public IntArrayList propose() {
    IntArrayList proposals = new IntArrayList();
    for(int node : this.getPrefixNodes()) {
      //System.out.println(node+ ": "+g.getAdjacencyList(node, this.isForward));
        proposals.addAll(g.getAdjacencyList(node, this.isForward).toArray());
      }

    return proposals;
  }

  /**
   * Returns the intersection of the given proposals and possible proposals from this relation.
   * @param proposals
   * @return IntArrayList[]
   */
  public IntArrayList[] intersect(IntArrayList proposals) {

    IntArrayList[] intersections = new IntArrayList[this.prefixes.length];
    //TODO: use faster method to intersect
    //iterate over prefixes and find matching proposals from each adjacency list
    if(prefixIndex >= 0) {
      int counter = 0;
      for (int node: this.getPrefixNodes()) {
        IntArrayList adjList = g.getAdjacencyList(node, isForward);
        IntArrayList intersectNodes = adjList.getIntersection(proposals);
        intersections[counter] = intersectNodes;
        counter++;
        //look at using streams here
      }
    } else {
      //the case where one side of the relation is unbounded
      IntArrayList possibleExtensions = g.getVertices(isForward);
      possibleExtensions = possibleExtensions.getIntersection(proposals);
      for(int i=0; i< this.prefixes.length; i++) {
        intersections[i] = possibleExtensions;
      }
    }
    System.out.println("done with interseciton");
    return intersections;
  }
}

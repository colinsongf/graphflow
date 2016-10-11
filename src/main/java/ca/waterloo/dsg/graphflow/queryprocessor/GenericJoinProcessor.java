package ca.waterloo.dsg.graphflow.queryprocessor;


import ca.waterloo.dsg.graphflow.graphmodel.Graph;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * The main entrypoint to query processing
 */
public class GenericJoinProcessor {

  Graph queryGraph;

  public GenericJoinProcessor(Graph g) {
    queryGraph = g;
  }

  /**
   * Finds and returns all triangles in the query graph. Precursor to a more
   * generic function able to process ad hoc queries
   * @return ArrayList<ArrayList<Integer>>
   */
  public ArrayList<ArrayList<Integer>> processTriangles() {

    //get lists of possible 'a'
    ArrayList<PrefixExtender> prefixExtenders = new ArrayList<>();
    //prefix index, fromSource true/false

    ArrayList<ArrayList<Integer>> singles = new ArrayList<>();
    for(int i=0;i<queryGraph.getVertexCount();i++) {
      singles.add(new ArrayList<>());
      singles.get(i).add(i);
    }

    prefixExtenders = new ArrayList<>();

    prefixExtenders.add(new PrefixExtender(singles, 0, true, queryGraph));
    System.out.println("Stating pairs");
    // Prefix instances, prefix extenders
    GenericJoinExtender secondExtender = new GenericJoinExtender(singles, prefixExtenders, queryGraph);
    ArrayList<ArrayList<Integer>> pairs = secondExtender.extend();

    prefixExtenders = new ArrayList<>();
    System.out.println(pairs);
    prefixExtenders.add(new PrefixExtender(pairs, 1, true, queryGraph));
    prefixExtenders.add(new PrefixExtender(pairs, 0, false, queryGraph));
    System.out.println("Stating triangles");
    // Prefix instances, prefix extenders
    GenericJoinExtender thirdExtender = new GenericJoinExtender(pairs, prefixExtenders, queryGraph);
    ArrayList<ArrayList<Integer>> triangles = thirdExtender.extend();
    return triangles;
  }


}

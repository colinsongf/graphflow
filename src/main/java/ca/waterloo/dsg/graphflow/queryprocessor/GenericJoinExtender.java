package ca.waterloo.dsg.graphflow.queryprocessor;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;

import java.util.ArrayList;

/**
 * Represents a single iteration of the Generic Join algorithm.
 */
public class GenericJoinExtender {

  ArrayList<ArrayList<Integer>> prefixInstances;
  ArrayList<PrefixExtender> prefixExtenders;
  private final Graph queryGraph;

  public GenericJoinExtender(ArrayList<ArrayList<Integer>> prefixInstances, ArrayList<PrefixExtender> extenders, Graph queryGraph) {
    this.prefixInstances = prefixInstances;
    this.prefixExtenders = extenders;
    this.queryGraph = queryGraph;
  }

  /**
   * Returns a list of tuples which have been extended by one member.
   *
   * @return ArrayList<ArrayList<Integer>>
   */
  public ArrayList<ArrayList<Integer>> extend() {

    PrefixExtender lowestExtender = null;
    int count = Integer.MAX_VALUE;
    for (PrefixExtender extender : this.prefixExtenders) {
      if (extender.count() < count) {
        count = extender.count();
        lowestExtender = extender;
      }
    }

    //TODO: write exception for the possible nullpointer exception here
    ArrayList<Integer> proposals = lowestExtender.propose();
    System.out.println("Proposals");
    System.out.println(proposals);
    ArrayList<ArrayList<Integer>> extensions = null;


    for (PrefixExtender extender : this.prefixExtenders) {
      if (extensions == null) {
        extensions = extender.intersect(proposals);
      } else {
        ArrayList<ArrayList<Integer>> extensionStream = extender.intersect(proposals);
        int prefixCounter = 0;
        System.out.println("Extension streams");
        for (ArrayList<Integer> extensionsPerPrefix : extensionStream) {
          System.out.println(extensionsPerPrefix + ": " + extensions.get(prefixCounter));
          extensions.get(prefixCounter).retainAll(extensionsPerPrefix);
          prefixCounter++;
        }
      }
    }

    ArrayList<ArrayList<Integer>> extendedTuples = new ArrayList<>();


    for (int i = 0; i < prefixInstances.size(); i++) {
      for (Integer possibleExtension : extensions.get(i)) {
        ArrayList<Integer> extendedTuple = (ArrayList<Integer>) prefixInstances.get(i).clone();
        extendedTuple.add(possibleExtension);
        extendedTuples.add(extendedTuple);
      }
    }



//    for(ArrayList<Integer> prefix: this.prefixInstances) {
//      ArrayList<Integer> possibleExtensions = (ArrayList<Integer>) this.queryGraph.getAdjacencyList(prefix.get(prefix.size()-1),true).clone();
//      possibleExtensions.retainAll(extensions);
//      for (Integer extension: possibleExtensions) {
//        ArrayList<Integer> extendedTuple = (ArrayList<Integer>) prefix.clone();
//        extendedTuple.add(extension);
//        extendedTuples.add(extendedTuple);
//      }
//    }
  return extendedTuples;
}

}

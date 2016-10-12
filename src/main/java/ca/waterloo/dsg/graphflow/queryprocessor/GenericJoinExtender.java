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

    //each prefixExtender takes the intersection of the extensions provided by previous prefixExtenders
    //number of members of intersection results =  number of prefixes
    for (PrefixExtender extender : this.prefixExtenders) {
      if (extensions == null) {
        extensions = extender.intersect(proposals);
      } else {
        ArrayList<ArrayList<Integer>> extensionStream = extender.intersect(proposals);
        int prefixCounter = 0;
        for (ArrayList<Integer> extensionsPerPrefix : extensionStream) {

          extensions.get(prefixCounter).retainAll(extensionsPerPrefix);
          prefixCounter++;
        }
      }
    }

    ArrayList<ArrayList<Integer>> extendedTuples = new ArrayList<>();

    //create new extended tuples using the extensions per prefix calculated above and
    //the prefixes themselves
    for (int i = 0; i < prefixInstances.size(); i++) {
      for (Integer possibleExtension : extensions.get(i)) {
        ArrayList<Integer> extendedTuple = (ArrayList<Integer>) prefixInstances.get(i).clone();
        extendedTuple.add(possibleExtension);
        extendedTuples.add(extendedTuple);
      }
    }

  return extendedTuples;
}

}

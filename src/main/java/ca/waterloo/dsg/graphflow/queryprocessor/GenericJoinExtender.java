package ca.waterloo.dsg.graphflow.queryprocessor;

import java.util.ArrayList;

/**
 * Represents a single iteration of the Generic Join algorithm.
 */
public class GenericJoinExtender {

  ArrayList<ArrayList<Integer>> prefixInstances;
  ArrayList<PrefixExtender> prefixExtenders;

  public GenericJoinExtender(ArrayList<ArrayList<Integer>> prefixInstances, ArrayList<PrefixExtender> extenders) {
    this.prefixInstances = prefixInstances;
    this.prefixExtenders = extenders;
  }

  /**
   * Returns a list of tuples which have been extended by one member.
   * @return ArrayList<ArrayList<Integer>>
   */
  public ArrayList<ArrayList<Integer>> extend() {

    PrefixExtender lowestExtender = null;
    int count = Integer.MAX_VALUE;
    for(PrefixExtender extender: this.prefixExtenders) {
      if(extender.count()< count) {
        count = extender.count();
        lowestExtender =extender;
      }
    }

    //TODO: write exception for the possible nullpointer exception here
    ArrayList<Integer> proposals = lowestExtender.propose();
    System.out.println("Proposals");
    System.out.println(proposals);
    ArrayList<ArrayList<Integer>> extensions = new ArrayList<>();
    for(PrefixExtender extender: this.prefixExtenders) {
      extensions.addAll(extender.intersect(proposals));
    }

    return extensions;
  }

}

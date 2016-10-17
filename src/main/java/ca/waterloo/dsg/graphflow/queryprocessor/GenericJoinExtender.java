package ca.waterloo.dsg.graphflow.queryprocessor;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.queryprocessor.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.util.IntArrayList;

import java.util.ArrayList;

/**
 * Represents a single iteration of the Generic Join algorithm.
 */
public class GenericJoinExtender {
  public static final int PREFIXES_PER_TURN = 2;

  ArrayList<ArrayList<PrefixExtender>> stages;
  private final Graph queryGraph;
  private OutputSink outputSink;

  public GenericJoinExtender(ArrayList<ArrayList<PrefixExtender>> stages, OutputSink outputSink, Graph queryGraph) {
    this.stages = stages;
    this.outputSink = outputSink;
    this.queryGraph = queryGraph;
  }

  /**
   * Recursively extends the given prefixes according to the correct query plan stage
   *  and writes the output to the output sink
   */
  public void extend(IntArrayList[] prefixes, int stageIndex) {
    System.out.println("Starting new recursion. Stage: "+stageIndex);
    ArrayList<PrefixExtender> prefixExtenders = this.stages.get(stageIndex);
    PrefixExtender lowestExtender = null;
    int count = Integer.MAX_VALUE;
    int lowestExtenderIndex = -1;
    int counter = 0;
    for (PrefixExtender extender : prefixExtenders) {
      extender.setPrefixes(prefixes);
      if (extender.count() < count) {
        count = extender.count();
        lowestExtender = extender;
        lowestExtenderIndex = counter;
      }
      counter++;
    }

    //TODO: write exception for the possible nullpointer exception here
    IntArrayList proposals = lowestExtender.propose();
    System.out.println("Proposals");
    System.out.println(proposals);
    IntArrayList[] extensions = null;

    //each prefixExtender takes the intersection of the extensions provided by previous stages
    //number of members of intersection results =  number of prefixes
    counter = 0;
    for (PrefixExtender extender : prefixExtenders) {
      if(count == lowestExtenderIndex) continue;
      //a list of possible extensions for each element in a list of prefixes
      System.out.println("before intersection");
      IntArrayList[] extensionStream = extender.intersect(proposals);
      if (extensions == null) {

        extensions = extender.intersect(proposals);
      } else {
        int prefixCounter = 0;
        for (IntArrayList extensionsPerPrefix : extensionStream) {
          extensions[prefixCounter] = extensions[prefixCounter].getIntersection(extensionsPerPrefix);
          prefixCounter++;
        }
      }
      System.out.println(counter);
      counter++;
    }
    System.out.println("escaped extensions");
    IntArrayList[] newPrefixes = new IntArrayList[PREFIXES_PER_TURN];
    int newPrefixCount = 0;
    //create new extended tuples using the extensions per prefix calculated above and
    //the prefixes themselves.
    System.out.println("Got a set of extensions "+extensions);
    for (int i = 0; i < prefixes.length; i++) {
      for (int j = 0; j< extensions[i].size(); j++) {
        int possibleExtension = extensions[i].get(j);
        newPrefixes[newPrefixCount] = new IntArrayList();
        newPrefixes[newPrefixCount].addAll(prefixes[i].toArray());
        newPrefixes[newPrefixCount].add(possibleExtension);

        if(++newPrefixCount >= PREFIXES_PER_TURN) {
          //if this is the last stage output results, else do recursion.
          if(stageIndex == (stages.size()-1)) {
            outputSink.append(prefixes);
          }  else {
            this.extend(newPrefixes, ++stageIndex);
          }
          newPrefixes = new IntArrayList[PREFIXES_PER_TURN];
          newPrefixCount = 0;
        }
      }
    }
    //do recursion or output for the last remaining results
    if(stageIndex == (stages.size()-1)) {
      outputSink.append(prefixes);
    }  else {
      this.extend(newPrefixes, ++stageIndex);
    }
  }
}

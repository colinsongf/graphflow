package ca.waterloo.dsg.graphflow.queryprocessor;


import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.queryprocessor.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.util.IntArrayList;

import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * The main entrypoint to query processing.
 */
public class GenericJoinProcessor {

  Graph queryGraph;

  public GenericJoinProcessor(Graph g) {
    queryGraph = g;
  }

  /**
   * Finds and returns all triangles in the query graph. Precursor to a more
   * generic function able to process ad hoc queries.
   * @param outputSink
   */
  public void processTriangles(OutputSink outputSink) {
    ArrayList<ArrayList<PrefixExtender>> stages = new ArrayList<>();
    ArrayList<PrefixExtender> firstStage = new ArrayList<>();
    firstStage.add(new PrefixExtender(0, true, queryGraph));
    //This is an unbounded extender. Returns all vertices
    firstStage.add(new PrefixExtender(-1, false, queryGraph));
    stages.add(firstStage);
    ArrayList<PrefixExtender> secondStage = new ArrayList<>();
    secondStage.add(new PrefixExtender(1, true, queryGraph));
    secondStage.add(new PrefixExtender(0, false, queryGraph));
    stages.add(secondStage);

    IntArrayList[] prefixes = new IntArrayList[queryGraph.getVertexCount()];
    for(int i=0;i<queryGraph.getVertexCount();i++) {
      prefixes[i] = new IntArrayList(stages.size());
      prefixes[i].add(i);
    }

    GenericJoinExtender extender = new GenericJoinExtender(stages, outputSink, queryGraph);
    extender.extend(prefixes, 0);

  }

  /**
   * Finds and returns all squares in the query graph.
   */
  public void processSquares(OutputSink outputSink) {
    ArrayList<ArrayList<PrefixExtender>> stages = new ArrayList<>();
    ArrayList<PrefixExtender> firstStage = new ArrayList<>();
    firstStage.add(new PrefixExtender(0, true, queryGraph));
    firstStage.add(new PrefixExtender(-1, false, queryGraph));
    stages.add(firstStage);
    ArrayList<PrefixExtender> secondStage = new ArrayList<>();
    secondStage.add(new PrefixExtender(1, true, queryGraph));
    secondStage.add(new PrefixExtender( -1, false, queryGraph));
    ArrayList<PrefixExtender> thirdStage = new ArrayList<>();
    thirdStage.add(new PrefixExtender(2, true, queryGraph));
    thirdStage.add(new PrefixExtender(0, false, queryGraph));

    IntArrayList[] prefixes = new IntArrayList[queryGraph.getVertexCount()];
    for(int i=0;i<queryGraph.getVertexCount();i++) {
      prefixes[i] = new IntArrayList(stages.size());
      prefixes[i].add(i);
    }

    GenericJoinExtender extender = new GenericJoinExtender(stages, outputSink, queryGraph);
    extender.extend(prefixes, 0);
  }

}

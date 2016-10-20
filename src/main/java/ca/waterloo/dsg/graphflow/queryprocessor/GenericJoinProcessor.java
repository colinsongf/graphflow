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

    /**
     * Finds and returns all triangles in the query graph. Precursor to a more
     * generic function able to process ad hoc queries.
     *
     * @param outputSink
     */
    public void processTriangles(OutputSink outputSink) {
        ArrayList<ArrayList<JoinRule>> stages = new ArrayList<>();
        ArrayList<JoinRule> firstStage = new ArrayList<>();
        firstStage.add(new JoinRule(0, true));
        stages.add(firstStage);//first stage is an empty ArrayList signifying that this stage is unbounded
        ArrayList<JoinRule> secondStage = new ArrayList<>();
        secondStage.add(new JoinRule(1, true));
        secondStage.add(new JoinRule(0, false));
        stages.add(secondStage);

        int[][] prefixes = new int[Graph.getInstance().getVertexCount()][1];
        for (int i = 0; i < Graph.getInstance().getVertexCount(); i++) {
            prefixes[i][0] = i;
        }

        GenericJoinExtender extender = new GenericJoinExtender(stages, outputSink);
        extender.extend(prefixes, 0);
    }

    /**
     * Finds and returns all squares in the query graph.
     */
    public void processSquares(OutputSink outputSink) {
        ArrayList<ArrayList<JoinRule>> stages = new ArrayList<>();
        ArrayList<JoinRule> firstStage = new ArrayList<>();
        firstStage.add(new JoinRule(0, true));
        stages.add(firstStage);
        ArrayList<JoinRule> secondStage = new ArrayList<>();
        secondStage.add(new JoinRule(1, true));
        stages.add(secondStage);
        ArrayList<JoinRule> thirdStage = new ArrayList<>();
        thirdStage.add(new JoinRule(2, true));
        thirdStage.add(new JoinRule(0, false));
        stages.add(thirdStage);
        int[][] prefixes = new int[Graph.getInstance().getVertexCount()][1];
        for (int i = 0; i < Graph.getInstance().getVertexCount(); i++) {
            prefixes[i][0] = i;
        }

        GenericJoinExtender extender = new GenericJoinExtender(stages, outputSink);
        extender.extend(prefixes, 0);
    }
}

package ca.waterloo.dsg.graphflow.queryprocessor;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.queryprocessor.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.util.IntArrayList;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Represents a single iteration of the Generic Join algorithm.
 */
public class GenericJoinExtender {
    public static final int PREFIXES_PER_TURN = 2;

    private ArrayList<ArrayList<JoinRule>> stages;
    private OutputSink outputSink;

    public GenericJoinExtender(ArrayList<ArrayList<JoinRule>> stages,
                               OutputSink outputSink) {
        this.stages = stages;
        this.outputSink = outputSink;
    }

    /**
     * Recursively extends the given prefixes according to the correct query plan stage
     * and writes the output to the output sink.
     */
    public void extend(int[][] prefixes, int stageIndex) {
        System.out.println("Starting new recursion. Stage: " + stageIndex);
        ArrayList<JoinRule> joinRules = this.stages.get(stageIndex);
        int newPrefixCount = 0;
        int[][] newPrefixes = new int[PREFIXES_PER_TURN][];

        for (int i = 0; i < prefixes.length; i++) {
            JoinRule minCountRule = getMinCountIndex(prefixes[i], joinRules);
            IntArrayList extensions = Graph.getInstance().getAdjacencyList(
                prefixes[i][minCountRule.getPrefixIndex()], minCountRule.isForward());

            for (JoinRule rule : joinRules) {
                if (rule == minCountRule) {//if references point to the same object.
                    continue;
                }
                //intersect remaining extensions with the possible extensions from the rule under consideration.
                extensions = extensions.getIntersection(Graph.getInstance()
                    .getAdjacencyList(prefixes[i][rule.getPrefixIndex()], rule.isForward()));
            }

            for (int j = 0; j < extensions.size(); j++) {
                int[] newPrefix = new int[prefixes[i].length + 1];
                System.arraycopy(prefixes[i], 0, newPrefix, 0, prefixes[i].length);
                newPrefix[newPrefix.length - 1] = extensions.get(j);
                newPrefixes[newPrefixCount++] = newPrefix;
                System.out.println(Arrays.toString(prefixes[i]) + " : " + Arrays.toString(newPrefix));
                if (newPrefixCount >= PREFIXES_PER_TURN) {
                    if (stageIndex == (stages.size() - 1)) {
                        outputSink.append(newPrefixes);
                    } else {
                        this.extend(newPrefixes, stageIndex + 1);
                    }
                    newPrefixCount = 0;
                    // Arrays.fill(newPrefixes, null);
                }
            }
        }

        if (newPrefixCount > 0) {
            if (stageIndex == (stages.size() - 1)) {
                outputSink.append(Arrays.copyOfRange(newPrefixes, 0, newPrefixCount));
            } else {
                this.extend(Arrays.copyOfRange(newPrefixes, 0, newPrefixCount), stageIndex + 1);
            }
        }
    }

    /**
     * Returns the JoinRule with the lowest number of possible extensions for the given prefix
     *
     * @param prefix    A list of number representing a partial solution to the query
     * @param joinRules A list of relations in Generic Join
     * @return JoinRule
     */
    private JoinRule getMinCountIndex(int[] prefix, ArrayList<JoinRule> joinRules) {
        JoinRule minJoinRule = null;
        int minCount = Integer.MAX_VALUE;
        for (JoinRule rule : joinRules) {
            int extensionCount = Graph.getInstance().getAdjacencyListSize(
                prefix[rule.getPrefixIndex()], rule.isForward());
            if (extensionCount < minCount) {
                minCount = extensionCount;
                minJoinRule = rule;
            }
        }
        return minJoinRule;
    }
}

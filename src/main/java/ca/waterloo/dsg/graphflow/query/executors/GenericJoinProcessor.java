package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Processes the given query stages for the in-memory grpah using the Generic Join algorithm.
 * Processing is done in batches using recursion.
 */
public class GenericJoinProcessor {

    public static final int BATCH_SIZE = 2;

    /**
     * Stages represents a Generic Join query plan for a query consisting of particular set of edges
     * and vertices. Each stage is used to expand the result tuples to an additional vertex.
     */
    private ArrayList<ArrayList<GenericJoinIntersectionRule>> stages;

    private OutputSink outputSink;
    private Graph graph;

    public GenericJoinProcessor(ArrayList<ArrayList<GenericJoinIntersectionRule>> stages,
        OutputSink outputSink, Graph graph) {
        this.stages = stages;
        this.outputSink = outputSink;
        this.graph = graph;
    }

    /**
     * Recursively extends the given prefixes according to the correct query plan stage
     * and writes the output to the output sink.
     */
    public void extend(int[][] prefixes, int stageIndex) {
        System.out.println("Starting new recursion. Stage: " + stageIndex);
        ArrayList<GenericJoinIntersectionRule> genericJoinIntersectionRules = this.stages.get(
            stageIndex);
        int newPrefixCount = 0;
        int[][] newPrefixes = new int[BATCH_SIZE][];

        for (int i = 0; i < prefixes.length; i++) {
            // Gets the rule with the minimum of possible extensions for this prefix.
            GenericJoinIntersectionRule minCountRule = getMinCountIndex(prefixes[i],
                genericJoinIntersectionRules);
            SortedIntArrayList extensions = this.graph.getAdjacencyList(
                prefixes[i][minCountRule.getPrefixIndex()], minCountRule.isForward());

            for (GenericJoinIntersectionRule rule : genericJoinIntersectionRules) {
                // Skip rule if it is the minCountRule.
                if (rule == minCountRule) {
                    continue;
                }
                // Intersect remaining extensions with the possible extensions from the rule
                // under consideration.
                extensions = extensions.getIntersection(this.graph
                    .getAdjacencyList(prefixes[i][rule.getPrefixIndex()], rule.isForward()));
            }

            for (int j = 0; j < extensions.size(); j++) {
                int[] newPrefix = new int[prefixes[i].length + 1];
                // TODO: Consider storing prefixes and new prefixes as trees so we don't replicate
                // common prefixes.
                System.arraycopy(prefixes[i], 0, newPrefix, 0, prefixes[i].length);
                newPrefix[newPrefix.length - 1] = extensions.get(j);
                newPrefixes[newPrefixCount++] = newPrefix;
                System.out.println(
                    Arrays.toString(prefixes[i]) + " : " + Arrays.toString(newPrefix));
                // Output is done in batches. Once the array of new prefixes reaches size BATCH_SIZE
                // they are recursively executed till final results are obtained before
                // proceeding with the extending process in this stage.
                if (newPrefixCount >= BATCH_SIZE) {
                    if (stageIndex == (stages.size() - 1)) {
                        // Write to output sink if this is the last stage.
                        outputSink.append(newPrefixes);
                    } else {
                        // Recursing to extend to the next stage with a set of prefix results
                        // equaling BATCH_SIZE.
                        this.extend(newPrefixes, stageIndex + 1);
                    }
                    newPrefixCount = 0;
                }
            }
        }

        // Handling the last batch for extended prefixes which did not reach size of BATCH_SIZE.
        if (newPrefixCount > 0) {
            if (stageIndex == (stages.size() - 1)) {
                outputSink.append(Arrays.copyOfRange(newPrefixes, 0, newPrefixCount));
            } else {
                this.extend(Arrays.copyOfRange(newPrefixes, 0, newPrefixCount), stageIndex + 1);
            }
        }
    }

    /**
     * Returns the GenericJoinIntersectionRule with the lowest number of possible extensions for the
     * given prefix.
     *
     * @param prefix A list of number representing a partial solution to the query.
     * @param genericJoinIntersectionRules A list of relations in Generic Join.
     * @return GenericJoinIntersectionRule
     */
    private GenericJoinIntersectionRule getMinCountIndex(int[] prefix,
        ArrayList<GenericJoinIntersectionRule> genericJoinIntersectionRules) {
        GenericJoinIntersectionRule minGenericJoinIntersectionRule = null;
        int minCount = Integer.MAX_VALUE;
        for (GenericJoinIntersectionRule rule : genericJoinIntersectionRules) {
            int extensionCount = this.graph.getAdjacencyListSize(prefix[rule.getPrefixIndex()],
                rule.isForward());
            if (extensionCount < minCount) {
                minCount = extensionCount;
                minGenericJoinIntersectionRule = rule;
            }
        }
        return minGenericJoinIntersectionRule;
    }
}

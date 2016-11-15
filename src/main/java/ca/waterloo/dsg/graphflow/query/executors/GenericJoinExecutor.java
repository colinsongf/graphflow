package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.planner.OneTimeMatchQueryPlanner;
import ca.waterloo.dsg.graphflow.util.SortedIntArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Processes the given query stages for the in-memory graph using the Generic Join algorithm.
 * Processing is done in batches using recursion.
 */
public class GenericJoinExecutor {

    public static final int BATCH_SIZE = 2;
    private static final Logger logger = LogManager.getLogger(OneTimeMatchQueryPlanner.class);
    /**
     * Stages represents a Generic Join query plan for a query consisting of particular set of edges
     * and vertices. Each stage is used to expand the result tuples to an additional vertex.
     */
    private List<List<GenericJoinIntersectionRule>> stages;
    private OutputSink outputSink;
    private Graph graph;

    public GenericJoinExecutor(List<List<GenericJoinIntersectionRule>> stages,
        OutputSink outputSink, Graph graph) {
        this.stages = stages;
        this.outputSink = outputSink;
        this.graph = graph;
    }

    public void execute() {
        GenericJoinIntersectionRule firstRule = stages.get(0).get(0);
        int[][] initialPrefixes = graph.getCurrentEdges(firstRule.getEdgeDirection());
        extend(initialPrefixes, 1);
    }

    /**
     * Recursively extends the given prefixes according to the correct query plan stage
     * and writes the output to the output sink.
     */
    private void extend(int[][] prefixes, int stageIndex) {
        System.out.println("Starting new recursion. Stage: " + stageIndex);
        List<GenericJoinIntersectionRule> genericJoinIntersectionRules = this.stages.get(
            stageIndex);
        int newPrefixCount = 0;
        int[][] newPrefixes = new int[BATCH_SIZE][];

        for (int[] prefix : prefixes) {
            // Gets the rule with the minimum of possible extensions for this prefix.
            GenericJoinIntersectionRule minCountRule = getMinCountIndex(prefix,
                genericJoinIntersectionRules);
            SortedIntArrayList extensions = this.graph.getAdjacencyList(
                prefix[minCountRule.getPrefixIndex()], minCountRule.getEdgeDirection(),
                minCountRule.getGraphVersion());

            for (GenericJoinIntersectionRule rule : genericJoinIntersectionRules) {
                // Skip rule if it is the minCountRule.
                if (rule == minCountRule) {
                    continue;
                }
                // Intersect remaining extensions with the possible extensions from the rule
                // under consideration.
                extensions = extensions.getIntersection(this.graph
                    .getAdjacencyList(prefix[rule.getPrefixIndex()], rule.getEdgeDirection(),
                        rule.getGraphVersion()));
            }

            for (int j = 0; j < extensions.getSize(); j++) {
                int[] newPrefix = new int[prefix.length + 1];
                // TODO: Consider storing prefixes and new prefixes as trees so we don't replicate
                // common prefixes.
                System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
                newPrefix[newPrefix.length - 1] = extensions.get(j);
                newPrefixes[newPrefixCount++] = newPrefix;
                logger.info(Arrays.toString(prefix) + " : " + Arrays.toString(newPrefix));
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
        List<GenericJoinIntersectionRule> genericJoinIntersectionRules) {
        GenericJoinIntersectionRule minGenericJoinIntersectionRule = null;
        int minCount = Integer.MAX_VALUE;
        for (GenericJoinIntersectionRule rule : genericJoinIntersectionRules) {
            int extensionCount = this.graph.getAdjacencyList(prefix[rule.getPrefixIndex()],
                rule.getEdgeDirection(), rule.getGraphVersion()).getSize();
            if (extensionCount < minCount) {
                minCount = extensionCount;
                minGenericJoinIntersectionRule = rule;
            }
        }
        return minGenericJoinIntersectionRule;
    }
}

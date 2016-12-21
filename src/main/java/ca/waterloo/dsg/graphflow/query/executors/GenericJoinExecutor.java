package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.util.IntArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Executes the Generic Join algorithm encapsulated in {@code stages} on {@code graph} and writes
 * output to the {@code outputSink}. Processing is done in batches using recursion.
 */
public class GenericJoinExecutor {

    private static final int BATCH_SIZE = 2;
    private static final Logger logger = LogManager.getLogger(GenericJoinExecutor.class);
    /**
     * Stages represents a Generic Join query plan for a query consisting of particular set of
     * relations between variables. Each stage is used to expand the result tuples to an additional
     * vertex.
     */
    private List<List<GenericJoinIntersectionRule>> stages;
    private OutputSink outputSink;
    private Graph graph;

    public GenericJoinExecutor(List<List<GenericJoinIntersectionRule>> stages,
        OutputSink outputSink, Graph graph) {
        if (0 == stages.size() || 0 == stages.get(0).size()) {
            throw new RuntimeException("Incomplete stages.");
        }
        this.stages = stages;
        this.outputSink = outputSink;
        this.graph = graph;
    }

    public void execute() {
        GenericJoinIntersectionRule firstGJIntersectionRule = stages.get(0).get(0);
        // Get the initial set of edges filtered by the {@code GraphVersion}, the {@code Direction}
        // and the edge type using the {@code firstGJIntersectionRule} of the first stage.
        Iterator<int[]> iterator = graph.getEdgesIterator(firstGJIntersectionRule.getGraphVersion(),
            firstGJIntersectionRule.getDirection(), firstGJIntersectionRule.getEdgeType());
        if (!iterator.hasNext()) {
            // Obtained empty set of edges, nothing to execute.
            return;
        }
        MatchQueryResultType matchQueryResultType;
        // Select the {@code MatchQueryResultType} depending on the {@code GraphVersion} of the
        // {@code firstGJIntersectionRule}.
        if (GraphVersion.DIFF_PLUS == firstGJIntersectionRule.getGraphVersion()) {
            matchQueryResultType = MatchQueryResultType.EMERGED;
        } else if (GraphVersion.DIFF_MINUS == firstGJIntersectionRule.getGraphVersion()) {
            matchQueryResultType = MatchQueryResultType.DELETED;
        } else {
            matchQueryResultType = MatchQueryResultType.MATCHED;
        }
        int[][] initialPrefixes = new int[BATCH_SIZE][];
        int index = 0;
        // The set of initial prefixes obtained by applying the first rule of the first stage needs
        // to be further filtered using the rest of the {@code GenericJoinIntersectionRule}s of
        // the first stage, if present.
        while (iterator.hasNext()) {
            int[] prefix = iterator.next();
            boolean isPrefixPresentForAllRules = true;
            for (int i = 1; i < stages.get(0).size(); i++) {
                // For each additional {@code GenericJoinIntersectionRule} present in the first
                // stage, check if the edge ({@code prefix[0]}, {@code prefix[1]}) satisfies the
                // {@code GraphVersion}, the {@code Direction} and the edge type of the rule, by
                // checking its existence in the graph.
                GenericJoinIntersectionRule rule = stages.get(0).get(i);
                if (!graph.isEdgePresent(prefix[0], prefix[1], rule.getDirection(), rule.
                    getGraphVersion(), rule.getEdgeType())) {
                    // The {@code prefix} did not satisfy the rule {@code i} of the first stage.
                    isPrefixPresentForAllRules = false;
                    break;
                }
            }
            if (!isPrefixPresentForAllRules) {
                // Skip adding {@code prefix} to the list of {@code initialPrefixes}, because it
                // does not satisfy one of the {@code GenericJoinIntersectionRule}s of the first
                // stage.
                continue;
            }
            initialPrefixes[index++] = prefix;
            if (index == BATCH_SIZE) {
                // Extend the initial prefixes in batches of size BATCH_SIZE.
                extend(initialPrefixes, 1, matchQueryResultType);
                index = 0;
            }
        }
        if (index > 0) {
            // Handle the last batch of initial prefixes which did not reach size of BATCH_SIZE.
            extend(Arrays.copyOf(initialPrefixes, index), 1, matchQueryResultType);
        }
    }

    /**
     * Recursively extends the given prefixes according to the stage identified by the given
     * {@code stageIndex} and writes the output to the output sink.
     *
     * @param prefixes Array of prefixes to extend.
     * @param stageIndex Stage index to track progress of the execution.
     * @param matchQueryResultType The category to under which the output prefixes are stored.
     */
    private void extend(int[][] prefixes, int stageIndex, MatchQueryResultType
        matchQueryResultType) {
        if (stageIndex >= stages.size()) {
            // Write to output sink because this is the last stage.
            for (int[] result : prefixes) {
                outputSink.append(getStringOutput(result, matchQueryResultType));
            }
            return;
        }
        logger.debug("Starting new recursion. Stage: " + stageIndex);
        List<GenericJoinIntersectionRule> genericJoinIntersectionRules = this.stages
            .get(stageIndex);
        int newPrefixCount = 0;
        int[][] newPrefixes = new int[BATCH_SIZE][];

        short filterVertexTypeId;
        for (int[] prefix : prefixes) {
            // Gets the rule with the minimum of possible extensions for this prefix.
            GenericJoinIntersectionRule minCountRule = getMinCountIndex(prefix,
                genericJoinIntersectionRules);
            // We need the initial set of extensions to be filtered because the call to
            // {@link SortedAdjacencyList#getIntersection} below will assume the input extensions
            // are already filtered.
            IntArrayList extensions = this.graph.getSortedAdjacencyList(prefix[minCountRule
                .getPrefixIndex()], minCountRule.getDirection(), minCountRule.getGraphVersion()).
                getFilteredNeighbourIds(minCountRule.getEdgeType());
            if (null == extensions || extensions.getSize() == 0) {
                // No extensions found for the current {@code prefix}.
                continue;
            }
            for (GenericJoinIntersectionRule rule : genericJoinIntersectionRules) {
                // Skip rule if it is the minCountRule.
                if (rule == minCountRule) {
                    continue;
                }
                // Intersect current extensions with the possible extensions obtained from
                // {@code rule}. Refer to comments for {@link SortedAdjacencyList#getIntersection}
                // to get the details of the getIntersection method.
                extensions = this.graph.getSortedAdjacencyList(prefix[rule
                    .getPrefixIndex()], rule.getDirection(), rule.getGraphVersion())
                    .getIntersection(extensions, rule.getEdgeType());
            }
            for (int j = 0; j < extensions.getSize(); j++) {
                int[] newPrefix = new int[prefix.length + 1];
                // TODO: Consider storing prefixes and new prefixes as trees so we don't replicate
                // common prefixes.
                System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
                newPrefix[newPrefix.length - 1] = extensions.get(j);
                newPrefixes[newPrefixCount++] = newPrefix;
                logger.debug(Arrays.toString(prefix) + " : " + Arrays.toString(newPrefix));
                // Output is done in batches. Once the array of new prefixes reaches size BATCH_SIZE
                // they are recursively executed till final results are obtained before
                // proceeding with the extending process in this stage.
                if (newPrefixCount >= BATCH_SIZE) {
                    // Recursing to extend to the next stage with a set of prefix results
                    // equaling BATCH_SIZE.
                    this.extend(newPrefixes, stageIndex + 1, matchQueryResultType);
                    newPrefixCount = 0;
                }
            }
        }

        if (newPrefixCount > 0) {
            // Handle the last batch of extended prefixes which did not reach size of BATCH_SIZE.
            this.extend(Arrays.copyOf(newPrefixes, newPrefixCount), stageIndex + 1,
                matchQueryResultType);
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
            int extensionCount = this.graph.getSortedAdjacencyList(prefix[rule.getPrefixIndex()],
                rule.getDirection(), rule.getGraphVersion()).getSize();
            if (extensionCount < minCount) {
                minCount = extensionCount;
                minGenericJoinIntersectionRule = rule;
            }
        }
        return minGenericJoinIntersectionRule;
    }

    public static String getStringOutput(int[] result, MatchQueryResultType matchQueryResultType) {
        return Arrays.toString(result) + ", " + matchQueryResultType;
    }
}

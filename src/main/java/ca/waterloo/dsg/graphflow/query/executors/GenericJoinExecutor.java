package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.util.IntArrayList;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Executes the Generic Join algorithm encapsulated in {@code stages} on {@code graph} and appends
 * outputs to the {@code nextOperator}. Processing is done in batches using recursion.
 */
public class GenericJoinExecutor {

    private static final int BATCH_SIZE = 2;
    private static final Logger logger = LogManager.getLogger(GenericJoinExecutor.class);
    // matchQueryOutput is reused to append prefixes to the next operator.
    private MatchQueryOutput matchQueryOutput = new MatchQueryOutput();

    /**
     * Stages represents a Generic Join query plan for a query consisting of particular set of
     * relations between variables. Each stage is used to expand the result tuples to an additional
     * vertex.
     */
    private List<List<GenericJoinIntersectionRule>> stages;
    private Graph graph;
    private AbstractDBOperator nextOperator;

    public GenericJoinExecutor(List<List<GenericJoinIntersectionRule>> stages,
        Map<String, Integer> variableIndicesMap, AbstractDBOperator nextOperator) {
        this.nextOperator = nextOperator;
        if (0 == stages.size() || 0 == stages.get(0).size()) {
            throw new RuntimeException("Incomplete stages.");
        }
        this.stages = stages;
        this.matchQueryOutput.vertexIndices = variableIndicesMap;
        this.graph = Graph.getInstance();
    }

    public void execute() {
        GenericJoinIntersectionRule firstGJIntersectionRule = stages.get(0).get(0);
        // Get the initial set of edges filtered by the {@code GraphVersion}, the {@code
        // Direction}, the edge type filter and the property equality filters using the {@code
        // firstGJIntersectionRule} of the first stage.
        Iterator<int[]> iterator = graph.getEdgesIterator(firstGJIntersectionRule.getGraphVersion(),
            firstGJIntersectionRule.getDirection(), firstGJIntersectionRule.
                getFromVertexTypeFilter(), firstGJIntersectionRule.getToVertexTypeFilter(),
            firstGJIntersectionRule.getEdgeTypeFilter());
        if (!iterator.hasNext()) {
            // Obtained empty set of edges, nothing to execute.
            return;
        }

        if (GraphVersion.DIFF_PLUS == firstGJIntersectionRule.getGraphVersion()) {
            matchQueryOutput.matchQueryResultType = MatchQueryResultType.EMERGED;
        } else if (GraphVersion.DIFF_MINUS == firstGJIntersectionRule.getGraphVersion()) {
            matchQueryOutput.matchQueryResultType = MatchQueryResultType.DELETED;
        } else {
            matchQueryOutput.matchQueryResultType = MatchQueryResultType.MATCHED;
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
                // {@code GraphVersion}, the {@code Direction}, and has the types and properties of
                // the rule.
                GenericJoinIntersectionRule rule = stages.get(0).get(i);
                if (!graph.isEdgePresent(prefix[0], prefix[1], rule.getDirection(), rule.
                    getGraphVersion(), rule.getEdgeTypeFilter())) {
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
                extend(initialPrefixes, 1);
                index = 0;
            }
        }
        if (index > 0) {
            // Handle the last batch of initial prefixes which did not reach size of BATCH_SIZE.
            extend(Arrays.copyOf(initialPrefixes, index), 1);
        }
        nextOperator.done();
    }

    /**
     * Recursively extends the given prefixes according to the stage identified by the given
     * {@code stageIndex} and writes the output to the output sink.
     *
     * @param prefixes Array of prefixes to extend.
     * @param stageIndex Stage index to track progress of the execution.
     */
    private void extend(int[][] prefixes, int stageIndex) {
        if (stageIndex >= stages.size()) {
            // Write to output sink because this is the last stage.
            for (int[] result : prefixes) {
                matchQueryOutput.vertexIds = result;
                nextOperator.append(matchQueryOutput);
            }
            return;
        }
        logger.debug("Starting new recursion. Stage: " + stageIndex);
        List<GenericJoinIntersectionRule> genericJoinIntersectionRules = this.stages.
            get(stageIndex);
        int newPrefixCount = 0;
        int[][] newPrefixes = new int[BATCH_SIZE][];

        for (int[] prefix : prefixes) {
            // Gets the rule with the minimum of possible extensions for this prefix.
            GenericJoinIntersectionRule minCountRule = getMinCountIndex(prefix,
                genericJoinIntersectionRules);
            // We need the initial set of extensions to be filtered because the call to
            // {@link SortedAdjacencyList#getIntersection} below will assume the input extensions
            // are already filtered.
            IntArrayList extensions = this.graph.getSortedAdjacencyList(prefix[minCountRule.
                getPrefixIndex()], minCountRule.getDirection(), minCountRule.getGraphVersion()).
                getFilteredNeighbourIds(minCountRule.getToVertexTypeFilter(), minCountRule.
                    getEdgeTypeFilter(), this.graph.getVertexTypes());
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
                extensions = this.graph.getSortedAdjacencyList(prefix[rule.getPrefixIndex()],
                    rule.getDirection(), rule.getGraphVersion()).getIntersection(extensions,
                    rule.getEdgeTypeFilter());
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
                    this.extend(newPrefixes, stageIndex + 1);
                    newPrefixCount = 0;
                }
            }
        }

        if (newPrefixCount > 0) {
            // Handle the last batch of extended prefixes which did not reach size of BATCH_SIZE.
            this.extend(Arrays.copyOf(newPrefixes, newPrefixCount), stageIndex + 1);
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

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     * @return {@code true} if the {@code a} object values are the same as the
     * {@code b} object values, {@code false} otherwise.
     */
    @UsedOnlyByTests
    public static boolean hasSameStages(GenericJoinExecutor a, GenericJoinExecutor b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        if (a.stages.size() != b.stages.size()) {
            return false;
        }
        for (int i = 0; i < a.stages.size(); i++) {
            if (a.stages.get(i).size() != b.stages.get(i).size()) {
                return false;
            }
            for (int j = 0; j < a.stages.get(i).size(); j++) {
                if (!GenericJoinIntersectionRule.isSameAs(a.stages.get(i).get(j),
                    b.stages.get(i).get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}

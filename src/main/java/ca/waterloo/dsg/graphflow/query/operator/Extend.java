package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.genericjoin.EdgeIntersectionRule;
import ca.waterloo.dsg.graphflow.query.operator.genericjoin.StageOperator;
import ca.waterloo.dsg.graphflow.util.IntArrayList;

import java.util.List;

/**
 * Given a set of output tuples of the previous operator, it extend the output to a new variable of
 * the query graph.
 */
public class Extend extends StageOperator {

    /**
     * @param intersectionRules the {@link EdgeIntersectionRule}s the prefixes extended to need to
     * follow.
     * @param toVertexTypeFilter Filters the edges that do not have the given to vertex type. If the
     * value of {@code fromVertexTypeFilter} is {@link TypeAndPropertyKeyStore#ANY}, this parameter
     * is ignored.
     */
    public Extend(List<EdgeIntersectionRule> intersectionRules, short toVertexTypeFilter) {
        super(intersectionRules, toVertexTypeFilter);
    }

    /**
     * Appends a new batch of prefixes to the next operator.
     *
     * @param prefixes a set of output prefixes, the output of the previous operator.
     */
    public void append(int[][] prefixes) {
        int newPrefixCount = 0;
        int[][] newPrefixes = new int[BATCH_SIZE][];

        for (int[] prefix : prefixes) {
            // Gets the rule with the minimum of possible extensions for this prefix.
            EdgeIntersectionRule minCountRule;
            if (1 < intersectionRules.size()) {
                minCountRule = getMinCountIndex(prefix);
            } else {
                minCountRule = intersectionRules.get(0);
            }
            // We need the initial set of extensions to be filtered because the call to
            // {@link SortedAdjacencyList#getIntersection} below will assume the input extensions
            // are already filtered.
            IntArrayList extensions = graph.getSortedAdjacencyList(prefix[minCountRule.
                getPrefixIndex()], minCountRule.getDirection(), minCountRule.getGraphVersion()).
                getFilteredNeighbourIds(toVertexTypeFilter, minCountRule.getEdgeTypeFilter(), graph.
                    getVertexTypes());
            if (null == extensions || extensions.getSize() == 0) {
                // No extensions found for the current {@code prefix}.
                continue;
            }
            for (EdgeIntersectionRule rule : intersectionRules) {
                // Skip rule if it is the minCountRule.
                if (rule == minCountRule) {
                    continue;
                }
                // Intersect current extensions with the possible extensions obtained from
                // {@code rule}. Refer to comments for {@link SortedAdjacencyList#getIntersection}
                // to get the details of the getIntersection method.
                extensions = graph.getSortedAdjacencyList(prefix[rule.getPrefixIndex()],
                    rule.getDirection(), rule.getGraphVersion()).getIntersection(extensions,
                    rule.getEdgeTypeFilter());
            }
            for (int j = 0; j < extensions.getSize(); j++) {
                int[] newPrefix = new int[prefix.length + 1];
                System.arraycopy(prefix, 0, newPrefix, 0, prefix.length);
                newPrefix[newPrefix.length - 1] = extensions.get(j);
                newPrefixes[newPrefixCount++] = newPrefix;
                // Append the prefixes to the next operator in size StageOperator.BATCH_SIZE.
                if (BATCH_SIZE <= newPrefixCount) {
                    append(newPrefixes, BATCH_SIZE);
                    newPrefixCount = 0;
                }
            }
        }

        if (0 < newPrefixCount) {
            // Append the prefixes which did not reach the size of StageOperator.BATCH_SIZE.
            append(newPrefixes, newPrefixCount);
        }
    }

    /**
     * Returns the EdgeIntersectionRule with the lowest number of possible extensions for the
     * given prefix.
     *
     * @param prefix A list of number representing a partial solution to the query.
     *
     * @return EdgeIntersectionRule with lowest number of possible extensions.
     */
    private EdgeIntersectionRule getMinCountIndex(int[] prefix) {
        EdgeIntersectionRule minEdgeIntersectionRule = null;
        int minCount = Integer.MAX_VALUE;
        for (EdgeIntersectionRule rule : intersectionRules) {
            int extensionCount = graph.getSortedAdjacencyList(prefix[rule.getPrefixIndex()], rule.
                getDirection(), rule.getGraphVersion()).getSize();
            if (extensionCount < minCount) {
                minCount = extensionCount;
                minEdgeIntersectionRule = rule;
            }
        }
        return minEdgeIntersectionRule;
    }
}

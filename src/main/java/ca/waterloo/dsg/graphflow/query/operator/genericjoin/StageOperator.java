package ca.waterloo.dsg.graphflow.query.operator.genericjoin;

import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.operator.AbstractOperator;
import ca.waterloo.dsg.graphflow.query.operator.Extend;
import ca.waterloo.dsg.graphflow.query.operator.Scan;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput.MatchQueryResultType;

import java.util.List;
import java.util.Map;

/**
 * This operator encapsulates common functionality between the {@link Scan} and {@link Extend}
 * operators.
 */
public abstract class StageOperator extends AbstractOperator {

    protected final static int BATCH_SIZE = 1000;

    protected List<EdgeIntersectionRule> intersectionRules;
    protected short toVertexTypeFilter;
    protected MatchQueryOutput matchQueryOutput = new MatchQueryOutput();
    private long intermediateResults = 0;

    /**
     * @param intersectionRules the {@link EdgeIntersectionRule}s the edges scanned or the prefixes
     * extended to need to follow.
     * @param toVertexTypeFilter Filters the edges that do not have the given to vertex type. If the
     * value of {@code fromVertexTypeFilter} is {@link TypeAndPropertyKeyStore#ANY}, this parameter
     * is ignored.
     */
    public StageOperator(List<EdgeIntersectionRule> intersectionRules, short toVertexTypeFilter) {
        super(null /* operator not initially set */);
        this.toVertexTypeFilter = toVertexTypeFilter;
        this.intersectionRules = intersectionRules;
    }

    /**
     * @return the {@link EdgeIntersectionRule}s of the operator.
     */
    public List<EdgeIntersectionRule> getIntersectionRules() {
        return intersectionRules;
    }

    /**
     * @param matchQueryResultType the {@link MatchQueryResultType} of the output prefixes of the
     * operator.
     * @param variableIndicesMap the indices of each variable in the prefixes.
     */
    public void setMatchQueryOutput(MatchQueryResultType matchQueryResultType,
        Map<String, Integer> variableIndicesMap) {
        matchQueryOutput = new MatchQueryOutput();
        matchQueryOutput.matchQueryResultType = matchQueryResultType;
        matchQueryOutput.vertexIndices = variableIndicesMap;
    }

    /**
     * Appends a set of prefixes as output to the next operator.
     *
     * @param prefixes an array of integer prefixes as output from the operator.
     * @param count the count of the prefixes appended.
     */
    protected void append(int[][] prefixes, int count) {
        if (!(nextOperator instanceof StageOperator)) {
            for (int i = 0; i < count; ++i) {
                if (null != prefixes[i]) {
                    matchQueryOutput.vertexIds = prefixes[i];
                    nextOperator.append(matchQueryOutput);
                }
            }
        } else if (BATCH_SIZE == count) {
            ((StageOperator) nextOperator).append(prefixes);
        } else {
            int[][] lastBatchOfPrefixes = new int[count][];
            System.arraycopy(prefixes, 0, lastBatchOfPrefixes, 0, count);
            ((StageOperator) nextOperator).append(lastBatchOfPrefixes);
        }
        intermediateResults += count;
    }

    /**
     * @return the {@code short} type filter for the to vertex in the intersection rules.
     */
    public short getToVertexTypeFilter() {
        return toVertexTypeFilter;
    }

    /**
     * Appends a new set of prefixes obtained as output from the previous operator.
     *
     * @param prefixes an array of prefixes as output of the previous operator.
     */
    public void append(int[][] prefixes) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not " +
            "support the append(MatchQueryOutput matchQueryOutputs) method.");
    }
}

package ca.waterloo.dsg.graphflow.query.output;

import ca.waterloo.dsg.graphflow.query.executors.MatchQueryResultType;
import ca.waterloo.dsg.graphflow.query.operator.AbstractDBOperator;

/**
 * Represents the output of a MATCH or CONTINUOUS MATCH query in the absence of aggregations.
 * <p>
 * Note: The fields of this class are public to enable instances of {@link AbstractDBOperator} to
 * easily reuse instances of this class by directly setting these fields. Doing so avoids
 * constructing new instances.
 */
public class MatchQueryOutput {

    public int[] vertexIds;
    public long[] edgeIds;
    public MatchQueryResultType matchQueryResultType;
    public Object[] results;
}

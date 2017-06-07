package ca.waterloo.dsg.graphflow.query.output;

import ca.waterloo.dsg.graphflow.query.operator.AbstractOperator;
import ca.waterloo.dsg.graphflow.query.operator.EdgeIdResolver.SourceDestinationIndexAndType;

import java.util.List;
import java.util.Map;

/**
 * Represents the output of a MATCH or CONTINUOUS MATCH query in the absence of aggregations.
 * Note: The fields of this class are public to enable instances of {@link AbstractOperator} to
 * easily reuse instances of this class by directly setting these fields. Doing so avoids
 * constructing new instances.
 */
public class MatchQueryOutput {

    /**
     * Used to represent the type of the motifs that result from match queries. Result motifs from
     * {@code ContinuousMatchQueryExecutor} are of the type {@code EMERGED} or {@code DELETED},
     * while result motifs from {@code GenericJoinExecutor} are always of the type {@code MATCHED}.
     */
    public enum MatchQueryResultType {
        EMERGED,
        DELETED,
        MATCHED
    }

    public Map<String, Integer> vertexIndices;
    public int[] vertexIds;
    public long[] edgeIds;
    public List<SourceDestinationIndexAndType> srcDstVertexIndicesAndTypes;
    public MatchQueryResultType matchQueryResultType;
}

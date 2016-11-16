package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Executes the Delta Generic Join algorithm encapsulated in {@code stages} on {@code graph} and
 * writes output to the {@code outputSink}.
 */
public class DeltaGenericJoinExecutor {

    private static final Logger logger = LogManager.getLogger(DeltaGenericJoinExecutor.class);
    /**
     * Represents a Delta Generic Join query plan in the form of a list of {@code
     * OneTimeMatchQueryPlan}s, which together produces the complete output for the Delta Generic
     * Join query.
     * TODO: Use OneTimeMatchQueryPlan object.
     */
    private List<List<List<GenericJoinIntersectionRule>>> stages;
    private OutputSink outputSink;
    private Graph graph;

    /**
     * @param stages Encapsulates the stages of the Delta Generic Join to be executed.
     * @param outputSink Where the output is stored.
     * @param graph The {@code Graph} object on which the query is executed.
     */
    public DeltaGenericJoinExecutor(List<List<List<GenericJoinIntersectionRule>>> stages,
        OutputSink outputSink, Graph graph) {
        this.stages = stages;
        this.outputSink = outputSink;
        this.graph = graph;
    }

    public void execute() {
        for (List<List<GenericJoinIntersectionRule>> stage : stages) {
            GenericJoinExecutor genericJoinExecutor = new GenericJoinExecutor(stage, outputSink,
                graph);
            genericJoinExecutor.execute();
        }
    }
}

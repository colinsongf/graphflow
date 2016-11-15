package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.graphmodel.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.graphmodel.GraphBuilder;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@code GenericJoinExecutor}.
 */
public class GenericJoinExecutorTest {

    private Graph graph;
    private InMemoryOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("graph.json").getPath());
        graph = GraphBuilder.createInstance(file);
    }

    @Test
    public void testProcessTriangles() throws Exception {
        List<List<GenericJoinIntersectionRule>> stages = new ArrayList<>();
        List<GenericJoinIntersectionRule> firstStage = new ArrayList<>();
        firstStage.add(new GenericJoinIntersectionRule(0, EdgeDirection.FORWARD));
        //first stage is an empty ArrayList signifying that this stage is unbounded
        stages.add(firstStage);
        List<GenericJoinIntersectionRule> secondStage = new ArrayList<>();
        secondStage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        secondStage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        stages.add(secondStage);

        outputSink = new InMemoryOutputSink();
        int[][] prefixes = new int[this.graph.getVertexCount()][1];
        for (int i = 0; i < this.graph.getVertexCount(); i++) {
            prefixes[i][0] = i;
        }
        GenericJoinExecutor executor = new GenericJoinExecutor(stages, outputSink, this.graph);
        executor.extend(prefixes, 0);
        int[][] results = {{0, 1, 2}, {1, 2, 0}, {2, 0, 1}};
        Assert.assertArrayEquals(results, outputSink.getResults().toArray());
    }

    @Test
    public void testProcessSquares() throws Exception {
        List<List<GenericJoinIntersectionRule>> stages = new ArrayList<>();
        List<GenericJoinIntersectionRule> firstStage = new ArrayList<>();
        firstStage.add(new GenericJoinIntersectionRule(0, EdgeDirection.FORWARD));
        stages.add(firstStage);
        List<GenericJoinIntersectionRule> secondStage = new ArrayList<>();
        secondStage.add(new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD));
        stages.add(secondStage);
        List<GenericJoinIntersectionRule> thirdStage = new ArrayList<>();
        thirdStage.add(new GenericJoinIntersectionRule(2, EdgeDirection.FORWARD));
        thirdStage.add(new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE));
        stages.add(thirdStage);

        outputSink = new InMemoryOutputSink();
        int[][] prefixes = new int[this.graph.getVertexCount()][1];
        for (int i = 0; i < this.graph.getVertexCount(); i++) {
            prefixes[i][0] = i;
        }
        GenericJoinExecutor executor = new GenericJoinExecutor(stages, outputSink, this.graph);
        executor.extend(prefixes, 0);
        int[][] results = {{0, 1, 2, 3}, {1, 2, 3, 0}, {2, 3, 0, 1}, {3, 0, 1, 2}};
        Assert.assertArrayEquals(results, outputSink.getResults().toArray());
    }
}

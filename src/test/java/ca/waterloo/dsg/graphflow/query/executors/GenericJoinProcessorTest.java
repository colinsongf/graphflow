package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

/**
 * Tests for {@code GenericJoinProcessor}.
 */
public class GenericJoinProcessorTest {

    Graph graph;
    private GenericJoinProcessor processor;
    private InMemoryOutputSink outputSink;

    @Before
    public void setUp() throws Exception {
        File file = new File(this.getClass().getClassLoader().getResource("graph.json").getPath());
        graph = Graph.createInstance(file);
    }

    @Test
    public void testProcessTriangles() throws Exception {
        ArrayList<ArrayList<GenericJoinIntersectionRule>> stages = new ArrayList<>();
        ArrayList<GenericJoinIntersectionRule> firstStage = new ArrayList<>();
        firstStage.add(new GenericJoinIntersectionRule(0, true));
        //first stage is an empty ArrayList signifying that this stage is unbounded
        stages.add(firstStage);
        ArrayList<GenericJoinIntersectionRule> secondStage = new ArrayList<>();
        secondStage.add(new GenericJoinIntersectionRule(1, true));
        secondStage.add(new GenericJoinIntersectionRule(0, false));
        stages.add(secondStage);

        outputSink = new InMemoryOutputSink();
        processor = new GenericJoinProcessor(stages, outputSink, this.graph);
        int[][] prefixes = new int[this.graph.getVertexCount()][1];
        for (int i = 0; i < this.graph.getVertexCount(); i++) {
            prefixes[i][0] = i;
        }
        GenericJoinProcessor processor = new GenericJoinProcessor(stages, outputSink, this.graph);
        processor.extend(prefixes, 0);
        int[][] results = {{0, 1, 2}, {1, 2, 0}, {2, 0, 1}};
        Assert.assertArrayEquals(results, outputSink.getResults().toArray());
    }

    @Test
    public void testProcessSquares() throws Exception {
        ArrayList<ArrayList<GenericJoinIntersectionRule>> stages = new ArrayList<>();
        ArrayList<GenericJoinIntersectionRule> firstStage = new ArrayList<>();
        firstStage.add(new GenericJoinIntersectionRule(0, true));
        stages.add(firstStage);
        ArrayList<GenericJoinIntersectionRule> secondStage = new ArrayList<>();
        secondStage.add(new GenericJoinIntersectionRule(1, true));
        stages.add(secondStage);
        ArrayList<GenericJoinIntersectionRule> thirdStage = new ArrayList<>();
        thirdStage.add(new GenericJoinIntersectionRule(2, true));
        thirdStage.add(new GenericJoinIntersectionRule(0, false));
        stages.add(thirdStage);

        outputSink = new InMemoryOutputSink();
        int[][] prefixes = new int[this.graph.getVertexCount()][1];
        for (int i = 0; i < this.graph.getVertexCount(); i++) {
            prefixes[i][0] = i;
        }
        GenericJoinProcessor processor = new GenericJoinProcessor(stages, outputSink, this.graph);
        processor.extend(prefixes, 0);
        int[][] results = {{0, 1, 2, 3}, {1, 2, 3, 0}, {2, 3, 0, 1}, {3, 0, 1, 2}};
        Assert.assertArrayEquals(results, outputSink.getResults().toArray());
    }
}

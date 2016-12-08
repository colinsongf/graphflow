package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.outputsink.InMemoryOutputSink;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariable;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@code ContinuousMatchQueryPlanner}.
 */
public class ContinuousMatchQueryPlannerTest {

    private ContinuousMatchQueryPlanner planner;
    private Map<String, QueryEdge> queryEdges = new HashMap<>();
    private OutputSink outputSink = new InMemoryOutputSink();

    @Before
    public void setUp() throws Exception {
        queryEdges.put("a->b", new QueryEdge(new QueryVariable("a"), new QueryVariable("b")));
        queryEdges.put("a->d", new QueryEdge(new QueryVariable("a"), new QueryVariable("d")));
        queryEdges.put("b->c", new QueryEdge(new QueryVariable("b"), new QueryVariable("c")));
        queryEdges.put("d->c", new QueryEdge(new QueryVariable("d"), new QueryVariable("c")));
        queryEdges.put("c->a", new QueryEdge(new QueryVariable("c"), new QueryVariable("a")));
        StructuredQuery structuredQuery = new StructuredQuery();
        queryEdges.forEach((key, queryEdge) -> structuredQuery.addEdge(queryEdge));
        planner = new ContinuousMatchQueryPlanner(structuredQuery, outputSink);
    }

    @Test
    public void testPlanTriangleQuery() throws Exception {
        // Create a Delta Generic Join query plan for a triangle query.
        StructuredQuery complexStructuredQuery = new StructuredQuery();
        complexStructuredQuery.addEdge(queryEdges.get("a->b"));
        complexStructuredQuery.addEdge(queryEdges.get("b->c"));
        complexStructuredQuery.addEdge(queryEdges.get("c->a"));
        ContinuousMatchQueryPlan continuousMatchQueryPlanActual = (ContinuousMatchQueryPlan) new
            ContinuousMatchQueryPlanner(complexStructuredQuery, outputSink).plan();

        // Create the Delta Generic Join query plan for a triangle query manually.
        ContinuousMatchQueryPlan continuousMatchQueryPlanExpected = new ContinuousMatchQueryPlan(
            outputSink);
        OneTimeMatchQueryPlan query;
        List<GenericJoinIntersectionRule> stage;
        // Stage 1
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_PLUS,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.PERMANENT,
            TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 2
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_MINUS,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.PERMANENT,
            TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 3
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_PLUS,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 4
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_MINUS,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 5
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_PLUS,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.MERGED,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 6
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_MINUS,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            TypeStore.ANY_TYPE));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.MERGED,
            TypeStore.ANY_TYPE));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);

        Assert.assertTrue(ContinuousMatchQueryPlan.isSameAs(continuousMatchQueryPlanActual,
            continuousMatchQueryPlanExpected));
    }
}

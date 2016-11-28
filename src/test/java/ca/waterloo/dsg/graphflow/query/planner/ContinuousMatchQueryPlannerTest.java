package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tests for {@code ContinuousMatchQueryPlanner}.
 */
public class ContinuousMatchQueryPlannerTest {

    private short defaultId = TypeStore.ANY_TYPE;
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
            defaultId));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.PERMANENT,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            defaultId));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 2
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_MINUS,
            defaultId));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.PERMANENT,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            defaultId));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 3
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_PLUS,
            defaultId));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            defaultId));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 4
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_MINUS,
            defaultId));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            defaultId));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 5
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_PLUS,
            defaultId));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.MERGED,
            defaultId));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);
        // Stage 6
        query = new OneTimeMatchQueryPlan();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_MINUS,
            defaultId));
        query.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.MERGED,
            defaultId));
        query.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(query);

        Assert.assertTrue(ContinuousMatchQueryPlan.isSameAs(continuousMatchQueryPlanActual,
            continuousMatchQueryPlanExpected));
    }

    @Test
    public void testCreateSingleQueryPlan() throws Exception {
        // Create a single stage query plan for a match query.
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(queryEdges.get("a->b"));
        QueryEdge diffRelation = queryEdges.get("b->c");
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(queryEdges.get("a->d"));
        oldRelations.add(queryEdges.get("d->c"));
        oldRelations.add(queryEdges.get("c->a"));
        ArrayList<String> orderedVariables = new ArrayList<>();
        orderedVariables.add("b");
        orderedVariables.add("c");
        orderedVariables.add("a");
        orderedVariables.add("d");
        ContinuousMatchQueryPlan continuousMatchQueryPlanActual = new ContinuousMatchQueryPlan(
            outputSink);
        continuousMatchQueryPlanActual.addOneTimeMatchQueryPlan(planner.addSingleQueryPlan(
            GraphVersion.DIFF_PLUS, orderedVariables, diffRelation, oldRelations, latestRelations));
        continuousMatchQueryPlanActual.addOneTimeMatchQueryPlan(planner.addSingleQueryPlan(
            GraphVersion.DIFF_MINUS, orderedVariables, diffRelation, oldRelations,
            latestRelations));

        // Create the expected query plan manually.
        ContinuousMatchQueryPlan continuousMatchQueryPlanExpected = new ContinuousMatchQueryPlan(
            outputSink);
        OneTimeMatchQueryPlan queryDiffPlus = new OneTimeMatchQueryPlan();
        OneTimeMatchQueryPlan queryDiffMinus = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Stage 1
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_PLUS,
            defaultId));
        queryDiffPlus.addStage(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, GraphVersion.DIFF_MINUS,
            defaultId));
        queryDiffMinus.addStage(stage);
        // Stage 2
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.PERMANENT,
            defaultId));
        queryDiffPlus.addStage(stage);
        queryDiffMinus.addStage(stage);
        // Stage 3
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD, GraphVersion.PERMANENT,
            defaultId));
        stage.add(new GenericJoinIntersectionRule(2, Direction.FORWARD, GraphVersion.PERMANENT,
            defaultId));
        queryDiffPlus.addStage(stage);
        queryDiffMinus.addStage(stage);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(queryDiffPlus);
        continuousMatchQueryPlanExpected.addOneTimeMatchQueryPlan(queryDiffMinus);

        Assert.assertTrue(ContinuousMatchQueryPlan.isSameAs(continuousMatchQueryPlanExpected,
            continuousMatchQueryPlanActual));
    }

    @Test
    public void testAddRuleIfPossibleEdgeExistsWithLatest() throws Exception {
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(queryEdges.get("a->b"));
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(queryEdges.get("a->d"));
        oldRelations.add(queryEdges.get("d->c"));
        oldRelations.add(queryEdges.get("c->a"));
        List<GenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = queryEdges.get("a->b");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Graph.Direction.FORWARD, possibleEdge,
            resultStage, oldRelations, latestRelations);
        GenericJoinIntersectionRule expectedRule = new GenericJoinIntersectionRule(0, Direction
            .FORWARD, GraphVersion.MERGED, defaultId);

        Assert.assertTrue(GenericJoinIntersectionRule.isSameAs(expectedRule, resultStage.get(0)));
    }

    @Test
    public void testAddRuleIfPossibleEdgeExistsWithOld() throws Exception {
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(queryEdges.get("a->b"));
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(queryEdges.get("a->d"));
        oldRelations.add(queryEdges.get("d->c"));
        oldRelations.add(queryEdges.get("c->a"));
        List<GenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = queryEdges.get("d->c");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Graph.Direction.FORWARD, possibleEdge,
            resultStage, oldRelations, latestRelations);
        GenericJoinIntersectionRule expectedRule = new GenericJoinIntersectionRule(0, Direction
            .FORWARD, GraphVersion.PERMANENT, defaultId);

        Assert.assertTrue(GenericJoinIntersectionRule.isSameAs(expectedRule, resultStage.get(0)));
    }

    @Test
    public void testAddRuleIfPossibleEdgeExistsWithNonExistent() throws Exception {
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(queryEdges.get("a->b"));
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(queryEdges.get("a->d"));
        oldRelations.add(queryEdges.get("d->c"));
        oldRelations.add(queryEdges.get("c->a"));
        List<GenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge(new QueryVariable("d"), new QueryVariable("a"));
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Graph.Direction.FORWARD, possibleEdge,
            resultStage, oldRelations, latestRelations);

        Assert.assertEquals(0, resultStage.size());
    }
}

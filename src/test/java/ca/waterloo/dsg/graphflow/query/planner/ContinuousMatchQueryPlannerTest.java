package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQueryEdge;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@code ContinuousMatchQueryPlanner}.
 */
public class ContinuousMatchQueryPlannerTest {

    private ContinuousMatchQueryPlanner planner;

    @Before
    public void setUp() throws Exception {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        structuredQuery.addEdge(new StructuredQueryEdge("a", "d"));
        structuredQuery.addEdge(new StructuredQueryEdge("b", "c"));
        structuredQuery.addEdge(new StructuredQueryEdge("d", "c"));
        structuredQuery.addEdge(new StructuredQueryEdge("c", "a"));
        planner = new ContinuousMatchQueryPlanner(structuredQuery);
    }

    @Test
    public void testPlanTriangleQuery() throws Exception {
        // Create a Delta Generic Join query plan for a triangle query.
        StructuredQuery complexStructuredQuery = new StructuredQuery();
        complexStructuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("b", "c"));
        complexStructuredQuery.addEdge(new StructuredQueryEdge("c", "a"));
        ContinuousMatchQueryPlan continuousMatchQueryPlanActual = (ContinuousMatchQueryPlan) new
            ContinuousMatchQueryPlanner(complexStructuredQuery).plan();

        // Create the Delta Generic Join query plan for a triangle query manually.
        ContinuousMatchQueryPlan continuousMatchQueryPlanExpected = new ContinuousMatchQueryPlan();
        List<List<GenericJoinIntersectionRule>> query;
        List<GenericJoinIntersectionRule> stage;
        // Stage 1
        query = new ArrayList<>();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            GraphVersion.DIFF_PLUS));
        query.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            GraphVersion.PERMANENT));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            GraphVersion.PERMANENT));
        query.add(stage);
        continuousMatchQueryPlanExpected.addQuery(query);
        // Stage 2
        query = new ArrayList<>();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            GraphVersion.DIFF_MINUS));
        query.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD,
            GraphVersion.PERMANENT));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            GraphVersion.PERMANENT));
        query.add(stage);
        continuousMatchQueryPlanExpected.addQuery(query);
        // Stage 3
        query = new ArrayList<>();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            GraphVersion.DIFF_PLUS));
        query.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            GraphVersion.PERMANENT));
        query.add(stage);
        continuousMatchQueryPlanExpected.addQuery(query);
        // Stage 4
        query = new ArrayList<>();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            GraphVersion.DIFF_MINUS));
        query.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            GraphVersion.PERMANENT));
        query.add(stage);
        continuousMatchQueryPlanExpected.addQuery(query);
        // Stage 5
        query = new ArrayList<>();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            GraphVersion.DIFF_PLUS));
        query.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.MERGED));
        query.add(stage);
        continuousMatchQueryPlanExpected.addQuery(query);
        // Stage 6
        query = new ArrayList<>();
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            GraphVersion.DIFF_MINUS));
        query.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD, GraphVersion.MERGED));
        query.add(stage);
        continuousMatchQueryPlanExpected.addQuery(query);

        Assert.assertTrue(continuousMatchQueryPlanActual
            .isSameAs(continuousMatchQueryPlanExpected));
    }

    @Test
    public void testCreateSingleQueryPlan() throws Exception {
        // Create a single stage query plan for a match query.
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(new QueryEdge("a", "b"));
        QueryEdge diffRelation = new QueryEdge("b", "c");
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(new QueryEdge("a", "d"));
        oldRelations.add(new QueryEdge("d", "c"));
        oldRelations.add(new QueryEdge("c", "a"));
        ArrayList<String> orderedVariables = new ArrayList<>();
        orderedVariables.add("b");
        orderedVariables.add("c");
        orderedVariables.add("a");
        orderedVariables.add("d");
        ContinuousMatchQueryPlan continuousMatchQueryPlanActual = new ContinuousMatchQueryPlan();
        continuousMatchQueryPlanActual.addQuery(planner.addSingleQueryPlan(GraphVersion
            .DIFF_PLUS, diffRelation, orderedVariables, oldRelations, latestRelations));
        continuousMatchQueryPlanActual.addQuery(planner.addSingleQueryPlan(GraphVersion
            .DIFF_MINUS, diffRelation, orderedVariables, oldRelations, latestRelations));

        // Create the expected query plan manually.
        ContinuousMatchQueryPlan continuousMatchQueryPlanExpected = new ContinuousMatchQueryPlan();
        List<List<GenericJoinIntersectionRule>> queryDiffPlus = new ArrayList<>();
        List<List<GenericJoinIntersectionRule>> queryDiffMinus = new ArrayList<>();
        List<GenericJoinIntersectionRule> stage;
        // Stage 1
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            GraphVersion.DIFF_PLUS));
        queryDiffPlus.add(stage);
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD,
            GraphVersion.DIFF_MINUS));
        queryDiffMinus.add(stage);
        // Stage 2
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, Direction.BACKWARD, GraphVersion.MERGED));
        stage.add(new GenericJoinIntersectionRule(1, Direction.FORWARD,
            GraphVersion.PERMANENT));
        queryDiffPlus.add(stage);
        queryDiffMinus.add(stage);
        // Stage 3
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(1, Direction.BACKWARD,
            GraphVersion.PERMANENT));
        stage.add(new GenericJoinIntersectionRule(2, Direction.FORWARD,
            GraphVersion.PERMANENT));
        queryDiffPlus.add(stage);
        queryDiffMinus.add(stage);
        continuousMatchQueryPlanExpected.addQuery(queryDiffPlus);
        continuousMatchQueryPlanExpected.addQuery(queryDiffMinus);

        Assert.assertTrue(continuousMatchQueryPlanExpected
            .isSameAs(continuousMatchQueryPlanActual));
    }

    @Test
    public void testAddRuleIfPossibleEdgeExistsWithLatest() throws Exception {
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(new QueryEdge("a", "b"));
        QueryEdge diffRelation = new QueryEdge("b", "c");
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(new QueryEdge("a", "d"));
        oldRelations.add(new QueryEdge("d", "c"));
        oldRelations.add(new QueryEdge("c", "a"));
        List<GenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge("a", "b");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Direction.FORWARD,
            possibleEdge, diffRelation, resultStage, oldRelations, latestRelations);
        GenericJoinIntersectionRule expectedRule = new GenericJoinIntersectionRule(0,
            Direction.FORWARD, GraphVersion.MERGED);

        Assert.assertTrue(expectedRule.isSameAs(resultStage.get(0)));
    }

    @Test
    public void testAddRuleIfPossibleEdgeExistsWithOld() throws Exception {
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(new QueryEdge("a", "b"));
        QueryEdge diffRelation = new QueryEdge("b", "c");
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(new QueryEdge("a", "d"));
        oldRelations.add(new QueryEdge("d", "c"));
        oldRelations.add(new QueryEdge("c", "a"));
        List<GenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge("d", "c");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Direction.FORWARD,
            possibleEdge, diffRelation, resultStage, oldRelations, latestRelations);
        GenericJoinIntersectionRule expectedRule = new GenericJoinIntersectionRule(0,
            Direction.FORWARD, GraphVersion.PERMANENT);

        Assert.assertTrue(expectedRule.isSameAs(resultStage.get(0)));
    }

    @Test
    public void testAddRuleIfPossibleEdgeExistsWithDiff() throws Exception {
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(new QueryEdge("a", "b"));
        QueryEdge diffRelation = new QueryEdge("b", "c");
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(new QueryEdge("a", "d"));
        oldRelations.add(new QueryEdge("d", "c"));
        oldRelations.add(new QueryEdge("c", "a"));
        List<GenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge("b", "c");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Direction.FORWARD,
            possibleEdge, diffRelation, resultStage, oldRelations, latestRelations);
        GenericJoinIntersectionRule expectedRule = new GenericJoinIntersectionRule(0,
            Direction.FORWARD, GraphVersion.DIFF_PLUS);

        Assert.assertTrue(expectedRule.isSameAs(resultStage.get(0)));
    }

    @Test
    public void testAddRuleIfPossibleEdgeExistsWithNonExistent() throws Exception {
        Set<QueryEdge> latestRelations = new HashSet<>();
        latestRelations.add(new QueryEdge("a", "b"));
        QueryEdge diffRelation = new QueryEdge("b", "c");
        Set<QueryEdge> oldRelations = new HashSet<>();
        oldRelations.add(new QueryEdge("a", "d"));
        oldRelations.add(new QueryEdge("d", "c"));
        oldRelations.add(new QueryEdge("c", "a"));
        List<GenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge("d", "a");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Direction.FORWARD,
            possibleEdge, diffRelation, resultStage, oldRelations, latestRelations);
        Assert.assertEquals(0, resultStage.size());
    }
}
package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.DeltaGJMatchQueryPlan;
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
    public void testPlan() throws Exception {
        DeltaGJMatchQueryPlan plan = (DeltaGJMatchQueryPlan) planner.plan();
        Assert.assertEquals(5, plan.getQueryCount());
    }

    @Test
    public void testCreateSingleQueryPlan() throws Exception {
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
        List<List<GenericJoinIntersectionRule>> expectedSinglePlan = new ArrayList<>();
        // Extending to "a" in ordered variables.
        List<GenericJoinIntersectionRule> firstStage = new ArrayList<>();
        firstStage.add(
            new GenericJoinIntersectionRule(0, EdgeDirection.REVERSE, GraphVersion.MERGED));
        firstStage.add(
            new GenericJoinIntersectionRule(1, EdgeDirection.FORWARD, GraphVersion.CURRENT));
        expectedSinglePlan.add(firstStage);
        // Extending to "d" in ordered variables.
        List<GenericJoinIntersectionRule> secondStage = new ArrayList<>();
        secondStage.add(
            new GenericJoinIntersectionRule(1, EdgeDirection.REVERSE, GraphVersion.CURRENT));
        secondStage.add(
            new GenericJoinIntersectionRule(2, EdgeDirection.FORWARD, GraphVersion.CURRENT));
        expectedSinglePlan.add(secondStage);

        List<List<GenericJoinIntersectionRule>> resultSinglePlan = planner.createSingleQueryPlan(
            diffRelation, orderedVariables, oldRelations, latestRelations);
        Assert.assertEquals(2, resultSinglePlan.size());
        Assert.assertEquals(2, resultSinglePlan.get(0).size());
        Assert.assertEquals(2, resultSinglePlan.get(1).size());
        for (int i = 0; i < resultSinglePlan.size(); i++) {
            for (int j = 0; j < resultSinglePlan.get(i).size(); j++) {
                Assert.assertTrue((resultSinglePlan.get(i).get(j).isSameAs(expectedSinglePlan.
                    get(i).get(j))));
            }
        }
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

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Graph.EdgeDirection.FORWARD, possibleEdge,
            diffRelation, resultStage, oldRelations, latestRelations);
        GenericJoinIntersectionRule expectedRule = new GenericJoinIntersectionRule(0,
            EdgeDirection.FORWARD, GraphVersion.MERGED);

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

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Graph.EdgeDirection.FORWARD, possibleEdge,
            diffRelation, resultStage, oldRelations, latestRelations);
        GenericJoinIntersectionRule expectedRule = new GenericJoinIntersectionRule(0,
            EdgeDirection.FORWARD, GraphVersion.CURRENT);

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

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Graph.EdgeDirection.FORWARD, possibleEdge,
            diffRelation, resultStage, oldRelations, latestRelations);
        GenericJoinIntersectionRule expectedRule = new GenericJoinIntersectionRule(0,
            EdgeDirection.FORWARD, GraphVersion.DIFF_PLUS);

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

        planner.addRuleIfPossibleEdgeExists(prefixIndex, Graph.EdgeDirection.FORWARD, possibleEdge,
            diffRelation, resultStage, oldRelations, latestRelations);
        Assert.assertEquals(0, resultStage.size());
    }
}
package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graphmodel.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.executors.DeltaGenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.DeltaGenericJoinQueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariableAdjList;
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
    private StructuredQuery structuredQuery;

    @Before
    public void setUp() throws Exception {
        structuredQuery = new StructuredQuery();
        structuredQuery.addEdge(new StructuredQueryEdge("a", "b"));
        structuredQuery.addEdge(new StructuredQueryEdge("a", "d"));
        structuredQuery.addEdge(new StructuredQueryEdge("b", "c"));
        structuredQuery.addEdge(new StructuredQueryEdge("d", "c"));
        structuredQuery.addEdge(new StructuredQueryEdge("c", "a"));
        planner = new ContinuousMatchQueryPlanner(structuredQuery);
    }

    @Test
    public void testPlan() throws Exception {
        DeltaGenericJoinQueryPlan plan = (DeltaGenericJoinQueryPlan) planner.plan();
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
        List<List<DeltaGenericJoinIntersectionRule>> expectedSinglePlan = new ArrayList<>();
        // Extending to "a" in ordered variables.
        List<DeltaGenericJoinIntersectionRule> firstStage = new ArrayList<>();
        firstStage.add(new DeltaGenericJoinIntersectionRule(0, GraphVersion.LATEST, false));
        firstStage.add(new DeltaGenericJoinIntersectionRule(1, GraphVersion.OLD, true));
        expectedSinglePlan.add(firstStage);
        // Extending to "d" in ordered variables.
        List<DeltaGenericJoinIntersectionRule> secondStage = new ArrayList<>();
        secondStage.add(new DeltaGenericJoinIntersectionRule(1, GraphVersion.OLD, false));
        secondStage.add(new DeltaGenericJoinIntersectionRule(2, GraphVersion.OLD, true));
        expectedSinglePlan.add(secondStage);

        List<List<DeltaGenericJoinIntersectionRule>> resultSinglePlan = planner
            .createSingleQueryPlan(diffRelation, orderedVariables, oldRelations, latestRelations);
        Assert.assertEquals(2, resultSinglePlan.size());
        Assert.assertEquals(2, resultSinglePlan.get(0).size());
        Assert.assertEquals(2, resultSinglePlan.get(1).size());
        for (int i = 0; i < resultSinglePlan.size(); i++) {
            for (int j = 0; j < resultSinglePlan.get(i).size(); j++) {
                Assert.assertTrue((resultSinglePlan.get(i).get(j).equalsTo(expectedSinglePlan.
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
        List<DeltaGenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge("a", "b");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, QueryVariableAdjList.Direction.FORWARD,
            possibleEdge, diffRelation, resultStage, oldRelations, latestRelations);
        DeltaGenericJoinIntersectionRule expectedRule = new DeltaGenericJoinIntersectionRule(0,
            GraphVersion.LATEST, true);

        Assert.assertTrue(expectedRule.equalsTo(resultStage.get(0)));
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
        List<DeltaGenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge("d", "c");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, QueryVariableAdjList.Direction.FORWARD,
            possibleEdge, diffRelation, resultStage, oldRelations, latestRelations);
        DeltaGenericJoinIntersectionRule expectedRule = new DeltaGenericJoinIntersectionRule(0,
            GraphVersion.OLD, true);

        Assert.assertTrue(expectedRule.equalsTo(resultStage.get(0)));
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
        List<DeltaGenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge("b", "c");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, QueryVariableAdjList.Direction.FORWARD,
            possibleEdge, diffRelation, resultStage, oldRelations, latestRelations);
        DeltaGenericJoinIntersectionRule expectedRule = new DeltaGenericJoinIntersectionRule(0,
            GraphVersion.DIFF, true);

        Assert.assertTrue(expectedRule.equalsTo(resultStage.get(0)));
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
        List<DeltaGenericJoinIntersectionRule> resultStage = new ArrayList<>();
        QueryEdge possibleEdge = new QueryEdge("d", "a");
        int prefixIndex = 0;

        planner.addRuleIfPossibleEdgeExists(prefixIndex, QueryVariableAdjList.Direction.FORWARD,
            possibleEdge, diffRelation, resultStage, oldRelations, latestRelations);
        Assert.assertEquals(0, resultStage.size());
    }
}
package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graphmodel.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.executors.DeltaGenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.DeltaGenericJoinQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariableAdjList;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a {@code DeltaGenericJoinQueryPlan} to continuously find changes to MATCH query
 * results specified in the limited Cypher language Graphflow supports.
 */
public class ContinuousMatchQueryPlanner extends MatchQueryPlanner {

    public ContinuousMatchQueryPlanner(StructuredQuery structuredQuery) {
        super(structuredQuery);
    }

    @Override
    public QueryPlan plan() {
        DeltaGenericJoinQueryPlan deltaPlan = new DeltaGenericJoinQueryPlan();
        // We construct as many delta queries as there are edges in the query graph. Let n be the
        // number of edges in the query graph. Then we have dQ1, dQ2, ..., dQn. Delta query dQi
        // consists of the following: (1) i-1 relations that contains all of the LATEST graph
        // (newly added edges + the old edges); (2) one relation that consists only of the DIFF
        // graph (the newly edges in the graph). We refer to this relation as the diffRelation
        // below; (3) n-i relations that consists of the OLD graph.
        Set<QueryEdge> latestRelations = new HashSet<>();
        Set<QueryEdge> oldRelations = queryGraph.getEdges();
        for (QueryEdge diffRelation : queryGraph.getEdges()) {
            // The first two variables considered in each round will be the variables from the
            // delta edge.
            oldRelations.remove(diffRelation);
            List<String> orderedVariables = new ArrayList<>();
            orderedVariables.add(diffRelation.fromVariable);
            orderedVariables.add(diffRelation.toVariable);
            super.orderRemainingVariables(orderedVariables);
            // Create query plan using the ordering created above.
            deltaPlan.addQuery(createSingleQueryPlan(diffRelation, orderedVariables, oldRelations,
                latestRelations));
            latestRelations.add(diffRelation);
        }
        return deltaPlan;
    }

    /**
     * Returns the query plan for a single delta query in the {@code DeltaGenericJoinQueryPlan}.
     *
     * @param diffRelation The relation which will use the diff graph for a single delta query in
     * the {@code DeltaGenericJoinQueryPlan}.
     * @param orderedVariables The order in which variables will be covered in the plan.
     * @param oldRelations The set of relations that uses the old version of the graph.
     * @param latestRelations The set of relations that will use the latest version of the graph.
     * @return List<List<DeltaGenericJoinIntersectionRule>> A set of stages representing a single
     * generic join query plan.
     */
    @VisibleForTesting
    public List<List<DeltaGenericJoinIntersectionRule>> createSingleQueryPlan(
        QueryEdge diffRelation, List<String> orderedVariables, Set<QueryEdge> oldRelations,
        Set <QueryEdge> latestRelations) {
        List<List<DeltaGenericJoinIntersectionRule>> singleRoundPlan = new ArrayList<>();
        // We use 2 here because the first two ordered variables will not be matched using
        // generic join. Instead, we use the two variables from the edge of {@code diffRelation}.
        for (int i = 2; i < orderedVariables.size(); i++) {
            List<DeltaGenericJoinIntersectionRule> stage = new ArrayList<>();
            String variable = orderedVariables.get(i);
            for (int j = 0; j < i; j++) {
                String coveredVariable = orderedVariables.get(j);
                // We add a stage where there is a relation from the {@code queryVariable}
                // currently being considered to previously considered query variables.
                // Direction should be considered from covered variable to variable because
                // in the intersection rule, it is the covered variable's adjacency list
                // which will be used.
                QueryEdge possibleEdge = new QueryEdge(coveredVariable, variable);
                this.addRuleIfPossibleEdgeExists(j, QueryVariableAdjList.Direction.FORWARD,
                    possibleEdge, diffRelation, stage, oldRelations, latestRelations);
                possibleEdge = new QueryEdge(variable, coveredVariable);
                this.addRuleIfPossibleEdgeExists(j, QueryVariableAdjList.Direction.REVERSE,
                    possibleEdge, diffRelation, stage, oldRelations, latestRelations);
            }
            singleRoundPlan.add(stage);
        }
        return singleRoundPlan;
    }

    /**
     * Adds a {@code DeltaGenericJoinIntersectionRule} to the given stage with the given {@code
     * prefixIndex} and given {@code direction} if {@code possibleEdge} is either the
     * {\code diffRelation} or exists in {code oldRelations} or {code latestRelations}
     * @param prefixIndex Prefix index of the {@code DeltaGenericJoinIntersectionRule} to be
     * created.
     * @param direction Direction from the covered variable to the variable under consideration.
     * @param possibleEdge The edge whose existence is checked.
     * @param diffRelation The relation which will use the diff graph for this iteration of
     * {@code DeltaGenericJoinQueryPlan}.
     * @param stage The generic join stage to which the intersection rule will be added.
     * @param oldRelations The set of relations that uses the old version of the graph.
     * @param latestRelations The set of relations that will use the latest version of the graph.
     */
    @VisibleForTesting
    public void addRuleIfPossibleEdgeExists(int prefixIndex,
        QueryVariableAdjList.Direction direction, QueryEdge possibleEdge, QueryEdge diffRelation,
        List<DeltaGenericJoinIntersectionRule> stage, Set<QueryEdge> oldRelations,
        Set<QueryEdge> latestRelations) {
        // Check for the existence of the edge in the given direction.
        GraphVersion version = null;
        if (possibleEdge.equals(diffRelation)) {
            version = GraphVersion.DIFF;
        } else if (latestRelations.contains(possibleEdge)) {
            version = GraphVersion.LATEST;
        } else if (oldRelations.contains(possibleEdge)){
            version = GraphVersion.OLD;
        }

        if (version != null) {
            stage.add(new DeltaGenericJoinIntersectionRule(prefixIndex, version,
                direction == QueryVariableAdjList.Direction.FORWARD));
        }
    }
}

package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.EdgeDirection;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.VisibleForTesting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a {@code ContinuousMatchQueryPlan} to continuously find changes to MATCH query
 * results specified in the limited Cypher language Graphflow supports.
 */
public class ContinuousMatchQueryPlanner extends OneTimeMatchQueryPlanner {

    public ContinuousMatchQueryPlanner(StructuredQuery structuredQuery) {
        super(structuredQuery);
    }

    @Override
    public QueryPlan plan() {
        ContinuousMatchQueryPlan deltaPlan = new ContinuousMatchQueryPlan();
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
            deltaPlan.addQuery(addSingleQueryPlan(GraphVersion.DIFF_PLUS, diffRelation,
                orderedVariables, oldRelations, latestRelations));
            deltaPlan.addQuery(addSingleQueryPlan(GraphVersion.DIFF_MINUS, diffRelation,
                orderedVariables, oldRelations, latestRelations));
            latestRelations.add(diffRelation);
        }
        return deltaPlan;
    }

    /**
     * Returns the query plan for a single delta query in the {@code ContinuousMatchQueryPlan}.
     *
     * @param diffRelation The relation which will use the diff graph for a single delta query in
     * the {@code ContinuousMatchQueryPlan}.
     * @param orderedVariables The order in which variables will be covered in the plan.
     * @param oldRelations The set of relations that uses the old version of the graph.
     * @param latestRelations The set of relations that will use the latest version of the graph.
     *
     * @return List<List<GenericJoinIntersectionRule>> A set of stages representing a single generic
     * join query plan.
     */
    @VisibleForTesting
    public List<List<GenericJoinIntersectionRule>> addSingleQueryPlan(GraphVersion graphVersion,
        QueryEdge diffRelation, List<String> orderedVariables, Set<QueryEdge> oldRelations,
        Set<QueryEdge> latestRelations) {
        List<List<GenericJoinIntersectionRule>> singleRoundPlan = new ArrayList<>();
        List<GenericJoinIntersectionRule> stage;
        // Add the first stage.
        stage = new ArrayList<>();
        stage.add(new GenericJoinIntersectionRule(0, EdgeDirection.FORWARD, graphVersion));
        singleRoundPlan.add(stage);
        // Add the rest of the stages.
        for (int i = 2; i < orderedVariables.size(); i++) {
            stage = new ArrayList<>();
            String variable = orderedVariables.get(i);
            for (int j = 0; j < i; j++) {
                String coveredVariable = orderedVariables.get(j);
                // We add a stage where there is a relation from the {@code queryVariable}
                // currently being considered to previously considered query variables.
                // Direction should be considered from covered variable to variable because
                // in the intersection rule, it is the covered variable's adjacency list
                // which will be used.
                QueryEdge possibleEdge = new QueryEdge(coveredVariable, variable);
                this.addRuleIfPossibleEdgeExists(j, Graph.EdgeDirection.FORWARD, possibleEdge,
                    diffRelation, stage, oldRelations, latestRelations);
                possibleEdge = new QueryEdge(variable, coveredVariable);
                this.addRuleIfPossibleEdgeExists(j, Graph.EdgeDirection.BACKWARD, possibleEdge,
                    diffRelation, stage, oldRelations, latestRelations);
            }
            singleRoundPlan.add(stage);
        }
        return singleRoundPlan;
    }

    /**
     * Adds a {@code GenericJoinIntersectionRule} to the given stage with the given {@code
     * prefixIndex} and given {@code edgeDirection} if {@code possibleEdge} is either the
     * {\code diffRelation} or exists in {code oldRelations} or {code latestRelations}
     *
     * @param prefixIndex Prefix index of the {@code GenericJoinIntersectionRule} to be created.
     * @param edgeDirection Direction from the covered variable to the variable under
     * consideration.
     * @param possibleEdge The edge whose existence is checked.
     * @param diffRelation The relation which will use the diff graph for this iteration of {@code
     * ContinuousMatchQueryPlan}.
     * @param stage The generic join stage to which the intersection rule will be added.
     * @param oldRelations The set of relations that uses the old version of the graph.
     * @param latestRelations The set of relations that will use the latest version of the graph.
     */
    @VisibleForTesting
    public void addRuleIfPossibleEdgeExists(int prefixIndex, Graph.EdgeDirection edgeDirection,
        QueryEdge possibleEdge, QueryEdge diffRelation, List<GenericJoinIntersectionRule> stage,
        Set<QueryEdge> oldRelations, Set<QueryEdge> latestRelations) {
        // Check for the existence of the edge in the given edgeDirection.
        GraphVersion version = null;
        if (possibleEdge.equals(diffRelation)) {
            version = GraphVersion.DIFF_PLUS;
        } else if (latestRelations.contains(possibleEdge)) {
            version = GraphVersion.MERGED;
        } else if (oldRelations.contains(possibleEdge)) {
            version = GraphVersion.PERMANENT;
        }

        if (version != null) {
            stage.add(new GenericJoinIntersectionRule(prefixIndex, edgeDirection, version));
        }
    }
}

package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.graph.TypeStore;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.PackagePrivateForTesting;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Creates a {@code ContinuousMatchQueryPlan} to continuously find changes to MATCH query
 * results specified in the limited Cypher language Graphflow supports.
 */
public class ContinuousMatchQueryPlanner extends OneTimeMatchQueryPlanner {

    private OutputSink outputSink;

    public ContinuousMatchQueryPlanner(StructuredQuery structuredQuery, OutputSink outputSink) {
        super(structuredQuery);
        this.outputSink = outputSink;
    }

    @Override
    public QueryPlan plan() {
        ContinuousMatchQueryPlan deltaPlan = new ContinuousMatchQueryPlan(outputSink);
        // We construct as many delta queries as there are edges in the query graph. Let n be the
        // number of edges in the query graph. Then we have dQ1, dQ2, ..., dQn. Delta query dQi
        // consists of the following: (1) i-1 relations that contains all of the LATEST graph
        // (newly added edges + the old edges); (2) one relation that consists only of the DIFF
        // graph (the newly edges in the graph). We refer to this relation as the diffRelation
        // below; (3) n-i relations that consists of the OLD graph.
        Set<QueryEdge> latestRelations = new HashSet<>();
        Set<QueryEdge> oldRelations = new HashSet<>(structuredQuery.getQueryEdges());
        for (QueryEdge diffRelation : structuredQuery.getQueryEdges()) {
            // The first two variables considered in each round will be the variables from the
            // delta edge.
            oldRelations.remove(diffRelation);
            List<String> orderedVariables = new ArrayList<>();
            orderedVariables.add(diffRelation.getFromQueryVariable().getVariableId());
            orderedVariables.add(diffRelation.getToQueryVariable().getVariableId());
            super.orderRemainingVariables(orderedVariables);
            // Create query plan using the ordering created above.
            deltaPlan.addOneTimeMatchQueryPlan(addSingleQueryPlan(GraphVersion.DIFF_PLUS,
                orderedVariables, diffRelation, oldRelations, latestRelations));
            deltaPlan.addOneTimeMatchQueryPlan(addSingleQueryPlan(GraphVersion.DIFF_MINUS,
                orderedVariables, diffRelation, oldRelations, latestRelations));
            latestRelations.add(diffRelation);
        }
        return deltaPlan;
    }

    /**
     * Returns the query plan for a single delta query in the {@code ContinuousMatchQueryPlan}.
     *
     * @param orderedVariables The order in which variables will be covered in the plan.
     * @param diffRelation The relation which will use the diff graph for a single delta query in
     * the {@code ContinuousMatchQueryPlan}.
     * @param oldRelations The set of relations that uses the old version of the graph.
     * @param latestRelations The set of relations that will use the latest version of the graph.
     * @return OneTimeMatchQueryPlan A set of stages representing a single generic join query plan.
     */
    @PackagePrivateForTesting
    OneTimeMatchQueryPlan addSingleQueryPlan(GraphVersion graphVersion,
        List<String> orderedVariables, QueryEdge diffRelation, Set<QueryEdge> oldRelations,
        Set<QueryEdge> latestRelations) {
        OneTimeMatchQueryPlan singleRoundPlan = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Add the first stage. The first stage always starts with extending the diffRelation's
        // fromVariable to toVariable with the type on the relation.
        stage = new ArrayList<>();
        short id = TypeStore.ANY_TYPE;
        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, graphVersion, id));
        singleRoundPlan.addStage(stage);
        // Add the rest of the stages.
        for (int i = 2; i < orderedVariables.size(); i++) {
            String nextVariable = orderedVariables.get(i);
            // We add a new stage that consists of the following intersection rules. For each
            // relation that is between the {@code nextVariable} and one of the previously
            // {@code coveredVariable}, we add a new intersection rule. If the relation is
            // (nextVariable, coveredVariable), the direction of the intersection rule is
            // BACKWARD. Otherwise, the direction of the intersection rules is FORWARD. This is
            // because we essentially extend prefixes from {@code coveredVariable}s to the
            // {@code nextVariable}s. The type of the intersection rule is the type on the relation.
            stage = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                String coveredVariable = orderedVariables.get(j);
                if (queryGraph.containsEdge(coveredVariable, nextVariable)) {
                    for (QueryEdge queryEdge : queryGraph.getAdjacentEdges(coveredVariable,
                        nextVariable)) {
                        addRuleIfPossibleEdgeExists(j, queryEdge.getDirection(), queryEdge, stage,
                            oldRelations, latestRelations);
                    }
                }
            }
            singleRoundPlan.addStage(stage);
        }
        return singleRoundPlan;
    }

    /**
     * Adds a {@code GenericJoinIntersectionRule} to the given stage with the given {@code
     * prefixIndex} and given {@code direction} if {@code possibleEdge} is either the
     * {\code diffRelation} or exists in {code oldRelations} or {code latestRelations}
     *
     * @param prefixIndex Prefix index of the {@code GenericJoinIntersectionRule} to be created.
     * @param direction Direction from the covered variable to the variable under
     * consideration.
     * @param possibleEdge The edge whose existence is checked.
     * @param stage The generic join stage to which the intersection rule will be added.
     * @param oldRelations The set of relations that uses the old version of the graph.
     * @param latestRelations The set of relations that will use the latest version of the graph.
     */
    @PackagePrivateForTesting
    void addRuleIfPossibleEdgeExists(int prefixIndex, Direction direction,
        QueryEdge possibleEdge, List<GenericJoinIntersectionRule> stage, Set<QueryEdge>
        oldRelations, Set<QueryEdge> latestRelations) {
        // Check for the existence of the edge in the given direction.
        GraphVersion version = null;
        if (isEdgePresentInSet(latestRelations, possibleEdge)) {
            version = GraphVersion.MERGED;
        } else if (isEdgePresentInSet(oldRelations, possibleEdge)) {
            version = GraphVersion.PERMANENT;
        }
        if (version != null) {
            short id = TypeStore.ANY_TYPE;
            stage.add(new GenericJoinIntersectionRule(prefixIndex, direction, version, id));
        }
    }

    private boolean isEdgePresentInSet(Set<QueryEdge> queryEdges, QueryEdge edgeToCheck) {
        for (QueryEdge queryEdge : queryEdges) {

            String fromVariable;
            String toVariable;
            if (Direction.FORWARD == edgeToCheck.getDirection()) {
                fromVariable = edgeToCheck.getFromQueryVariable().getVariableId();
                toVariable = edgeToCheck.getToQueryVariable().getVariableId();
            } else {
                fromVariable = edgeToCheck.getToQueryVariable().getVariableId();
                toVariable = edgeToCheck.getFromQueryVariable().getVariableId();
            }
            if (Objects.equals(queryEdge.getFromQueryVariable().getVariableId(), fromVariable) &&
                Objects.equals(queryEdge.getToQueryVariable().getVariableId(), toVariable)) {
                return true;
            }
        }
        return false;
    }
}

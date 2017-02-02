package ca.waterloo.dsg.graphflow.query.planner;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.outputsink.OutputSink;
import ca.waterloo.dsg.graphflow.query.executors.GenericJoinIntersectionRule;
import ca.waterloo.dsg.graphflow.query.plans.ContinuousMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.OneTimeMatchQueryPlan;
import ca.waterloo.dsg.graphflow.query.plans.QueryPlan;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    /**
     * Creates a continuous {@code MATCH} query plan for the given {@code structuredQuery}.
     *
     * @return A {@link QueryPlan} encapsulating a {@link ContinuousMatchQueryPlan}.
     */
    @Override
    public QueryPlan plan() {
        ContinuousMatchQueryPlan continuousMatchQueryPlan = new ContinuousMatchQueryPlan(
            outputSink);
        // We construct as many delta queries as there are relations in the query graph. Let n be
        // the number of relations in the query graph. Then we have dQ1, dQ2, ..., dQn. Delta query
        // dQi consists of the following: (1) i-1 relations that use the {@code MERGED} version
        // of the graph (newly added edges + the permanent edges); (2) one relation that use only
        // the {@code DIFF_PLUS} or {@code DIFF_MINUS} versions of the graph (the newly added or
        // deleted edges). We refer to this relation as the diffRelation below; (3) n-i relations
        // that use the {@code PERMANENT} version of the graph.
        Set<QueryRelation> mergedRelations = new HashSet<>();
        Set<QueryRelation> permanentRelations = new HashSet<>(structuredQuery.getQueryRelations());
        for (QueryRelation diffRelation : structuredQuery.getQueryRelations()) {
            // The first two variables considered in each round will be the variables from the
            // delta relation.
            permanentRelations.remove(diffRelation);
            List<String> orderedVariables = new ArrayList<>();
            orderedVariables.add(diffRelation.getFromQueryVariable().getVariableId());
            orderedVariables.add(diffRelation.getToQueryVariable().getVariableId());
            super.orderRemainingVariables(orderedVariables);
            // Create the query plan using the ordering determined above.
            continuousMatchQueryPlan.addOneTimeMatchQueryPlan(addSingleQueryPlan(
                GraphVersion.DIFF_PLUS, orderedVariables, diffRelation, permanentRelations,
                mergedRelations));
            continuousMatchQueryPlan.addOneTimeMatchQueryPlan(addSingleQueryPlan(
                GraphVersion.DIFF_MINUS, orderedVariables, diffRelation, permanentRelations,
                mergedRelations));
            mergedRelations.add(diffRelation);
        }
        return continuousMatchQueryPlan;
    }

    /**
     * Returns the query plan for a single delta query in the {@code ContinuousMatchQueryPlan}.
     *
     * @param orderedVariables The order in which variables will be covered in the plan.
     * @param diffRelation The relation which will use the diff graph for a single delta query in
     * the {@code ContinuousMatchQueryPlan}.
     * @param permanentRelations The set of relations that uses the {@link GraphVersion#PERMANENT}
     * version of the graph.
     * @param mergedRelations The set of relations that uses the {@link GraphVersion#MERGED}
     * version of the graph.
     * @return OneTimeMatchQueryPlan A set of stages representing a single generic join query plan.
     */
    private OneTimeMatchQueryPlan addSingleQueryPlan(GraphVersion graphVersion,
        List<String> orderedVariables, QueryRelation diffRelation,
        Set<QueryRelation> permanentRelations, Set<QueryRelation> mergedRelations) {
        OneTimeMatchQueryPlan oneTimeMatchQueryPlan = new OneTimeMatchQueryPlan();
        List<GenericJoinIntersectionRule> stage;
        // Add the first stage. The first stage always starts with extending the diffRelation's
        // {@code fromVariable} to {@code toVariable} with the type on the relation.
        stage = new ArrayList<>();

        stage.add(new GenericJoinIntersectionRule(0, Direction.FORWARD, graphVersion,
            TypeAndPropertyKeyStore.getInstance().mapStringTypeToShort(diffRelation.
                getRelationType()), TypeAndPropertyKeyStore.getInstance().
            mapStringPropertiesToShortAndDataType((diffRelation.getRelationProperties()))));
        oneTimeMatchQueryPlan.addStage(stage);
        // Add the other relations that are present between the diffRelation's
        // {@code fromVariable} to {@code toVariable}.
        String fromVariable = orderedVariables.get(0);
        String toVariable = orderedVariables.get(1);
        for (QueryRelation queryRelation : queryGraph.getAdjacentRelations(fromVariable,
            toVariable)) {
            if (QueryRelation.isSameAs(diffRelation, queryRelation)) {
                // This relation has been added as the {@code diffRelation}.
                continue;
            }
            addGenericJoinIntersectionRule(0,
                // The {@code Direction} of the rule is {@code FORWARD} if {@code queryRelation} is
                // an edge from {@code fromVariable} to {@code toVariable}, else {@code BACKWARD}.
                queryRelation.getFromQueryVariable().getVariableId().equals(fromVariable) ?
                    Direction.FORWARD : Direction.BACKWARD,
                queryRelation, stage, permanentRelations, mergedRelations);
        }
        // Add the rest of the stages.
        for (int i = 2; i < orderedVariables.size(); i++) {
            String nextVariable = orderedVariables.get(i);
            // We add a new stage that consists of the following intersection rules. For each
            // relation that is between the {@code nextVariable} and one of the previously
            // {@code coveredVariable}, we add a new intersection rule. The direction of the
            // intersection rule is {@code FORWARD} if the relation is from {@code coveredVariable}
            // to {@code nextVariable), otherwise the direction is {@code BACKWARD}. This
            // is because we essentially extend prefixes from the {@code coveredVariable}s to the
            // {@code nextVariable}s. The type of the intersection rule is the type on the relation.
            stage = new ArrayList<>();
            for (int j = 0; j < i; j++) {
                String coveredVariable = orderedVariables.get(j);
                if (queryGraph.containsRelation(coveredVariable, nextVariable)) {
                    for (QueryRelation queryRelation : queryGraph.getAdjacentRelations(
                        coveredVariable, nextVariable)) {
                        addGenericJoinIntersectionRule(j,
                            // The {@code Direction} of the rule is {@code FORWARD} if
                            // {@code queryRelation} is an edge from {@code coveredVariable} to
                            // {@code nextVariable}, else {@code BACKWARD}.
                            queryRelation.getFromQueryVariable().getVariableId().equals(
                                coveredVariable) ? Direction.FORWARD : Direction.BACKWARD,
                            queryRelation, stage, permanentRelations, mergedRelations);
                    }
                }
            }
            oneTimeMatchQueryPlan.addStage(stage);
        }
        return oneTimeMatchQueryPlan;
    }

    /**
     * Adds a {@code GenericJoinIntersectionRule} to the given stage with the given
     * {@code prefixIndex}, {@code direction} and the relation and variable type IDs, if the
     * {@code newRelation} exists in either {@code permanentRelations} or {@code mergedRelations}.
     *
     * @param prefixIndex Prefix index of the {@code GenericJoinIntersectionRule} to be created.
     * @param direction Direction from the covered variable to the variable under
     * consideration.
     * @param newRelation The relation for which the rule is being added.
     * @param stage The generic join stage to which the intersection rule will be added.
     * @param permanentRelations The set of relations that uses the {@link GraphVersion#PERMANENT}
     * version of the graph.
     * @param mergedRelations The set of relations that uses the {@link GraphVersion#MERGED}
     * version of the graph.
     */
    private void addGenericJoinIntersectionRule(int prefixIndex, Direction direction,
        QueryRelation newRelation, List<GenericJoinIntersectionRule> stage,
        Set<QueryRelation> permanentRelations, Set<QueryRelation> mergedRelations) {
        // Select the appropriate {@code GraphVersion} by checking for the existence of
        // {@code newRelation} in either {@code mergedRelations} or {@code mergedRelations}.
        GraphVersion version;
        if (isRelationPresentInSet(newRelation, mergedRelations)) {
            version = GraphVersion.MERGED;
        } else if (isRelationPresentInSet(newRelation, permanentRelations)) {
            version = GraphVersion.PERMANENT;
        } else {
            throw new IllegalStateException("The new relation is not present in either " +
                "mergedRelations or permanentRelations");
        }
        stage.add(new GenericJoinIntersectionRule(prefixIndex, direction, version,
            TypeAndPropertyKeyStore.getInstance().mapStringTypeToShort(newRelation.
                getRelationType()), TypeAndPropertyKeyStore.getInstance().
            mapStringPropertiesToShortAndDataType(newRelation.getRelationProperties())));
    }

    /**
     * @param queryRelationToCheck The {@link QueryRelation} to be searched.
     * @param queryRelations A set of {@link QueryRelation}s.
     * @return {@code true} if {@code fromVariable} and {@code toVariable} match the
     * corresponding values of any of the {@link QueryRelation} present in {@code queryRelations}.
     */
    private boolean isRelationPresentInSet(QueryRelation queryRelationToCheck,
        Set<QueryRelation> queryRelations) {
        for (QueryRelation queryRelation : queryRelations) {
            if (QueryRelation.isSameAs(queryRelationToCheck, queryRelation)) {
                return true;
            }
        }
        return false;
    }
}

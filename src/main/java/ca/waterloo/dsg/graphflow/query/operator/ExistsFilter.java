package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ExistsFilter extends AbstractDBOperator {

    private List<NotExistsPredicateRule> notExistsPredicateRules = new ArrayList<>();
    private List<ExistsPredicateRule> existsPredicateRules = new ArrayList<>();

    public ExistsFilter(AbstractDBOperator nextOperator) {
        super(nextOperator);
    }

    public void addNotExistsPredicateRules(NotExistsPredicateRule notExistsPredicateRule) {
        this.notExistsPredicateRules.add(notExistsPredicateRule);
    }

    public void addExistsPredicateRules(ExistsPredicateRule existsPredicateRule) {
        this.existsPredicateRules.add(existsPredicateRule);
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        for (NotExistsPredicateRule notExistsPredicateRule : notExistsPredicateRules) {
            int fromVertexId = matchQueryOutput.vertexIds[notExistsPredicateRule.
                getFromPrefixIndex()];
            int toVertexId = matchQueryOutput.vertexIds[notExistsPredicateRule.
                getToPrefixIndex()];
            short fromVertexType = notExistsPredicateRule.getFromVertexTypeFilter();
            short toVertexType = notExistsPredicateRule.getToVertexTypeFilter();
            if (fromVertexType != TypeAndPropertyKeyStore.ANY &&
                Graph.getInstance().getVertexTypes().get(fromVertexId) != fromVertexType) {
                continue;
            }
            if (toVertexType != TypeAndPropertyKeyStore.ANY &&
                Graph.getInstance().getVertexTypes().get(toVertexId) != toVertexType) {
                continue;
            }
            if (Graph.getInstance().isEdgePresent(fromVertexId, toVertexId, Direction.FORWARD,
                GraphVersion.PERMANENT, notExistsPredicateRule.getEdgeTypeFilter())) {
                return;
            }
        }
        for (ExistsPredicateRule existsPredicateRule : existsPredicateRules) {
            int vertexId = matchQueryOutput.vertexIds[existsPredicateRule.
                getPrefixIndex()];
            short fromVertexType = existsPredicateRule.getFromVertexTypeFilter();
            if (fromVertexType != TypeAndPropertyKeyStore.ANY &&
                Graph.getInstance().getVertexTypes().get(vertexId) != fromVertexType) {
                return;
            }
            if (!Graph.getInstance().isEdgePresentOfType(vertexId, existsPredicateRule.
                getDirection(), existsPredicateRule.getEdgeTypeFilter())) {
                return;
            }
        }
        nextOperator.append(matchQueryOutput);
    }

    @Override
    public void done() {
        super.done();
    }

    @Override
    public JsonObject toJson() {
        return null;
    }

    @Override
    protected String getHumanReadableOperator() {
        return null;
    }
}

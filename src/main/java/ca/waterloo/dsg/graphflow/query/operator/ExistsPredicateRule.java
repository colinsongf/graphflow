package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.graph.Graph.Direction;

public class ExistsPredicateRule {

    private int prefixIndex;
    private Direction direction;
    private short fromVertexTypeFilter;
    private short edgeTypeFilter;

    public ExistsPredicateRule(int prefixIndex, Direction direction, short fromVertexTypeFilter,
        short edgeTypeFilter) {
        this.prefixIndex = prefixIndex;
        this.direction = direction;
        this.fromVertexTypeFilter = fromVertexTypeFilter;
        this.edgeTypeFilter = edgeTypeFilter;
    }

    public int getPrefixIndex() {
        return prefixIndex;
    }

    public Direction getDirection() {
        return direction;
    }

    public short getFromVertexTypeFilter() {
        return fromVertexTypeFilter;
    }

    public short getEdgeTypeFilter() {
        return edgeTypeFilter;
    }
}

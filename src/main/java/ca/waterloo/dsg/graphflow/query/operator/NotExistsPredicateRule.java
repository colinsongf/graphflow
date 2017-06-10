package ca.waterloo.dsg.graphflow.query.operator;

public class NotExistsPredicateRule {

    private int fromPrefixIndex;
    private int toPrefixIndex;
    private short fromVertexTypeFilter;
    private short toVertexTypeFilter;
    private short edgeTypeFilter;

    public NotExistsPredicateRule(int fromPrefixIndex, int toPrefixIndex, short
        fromVertexTypeFilter, short toVertexTypeFilter, short edgeTypeFilter) {
        this.fromPrefixIndex = fromPrefixIndex;
        this.toPrefixIndex = toPrefixIndex;
        this.fromVertexTypeFilter = fromVertexTypeFilter;
        this.toVertexTypeFilter = toVertexTypeFilter;
        this.edgeTypeFilter = edgeTypeFilter;
    }

    public int getFromPrefixIndex() {
        return fromPrefixIndex;
    }

    public int getToPrefixIndex() {
        return toPrefixIndex;
    }

    public short getFromVertexTypeFilter() {
        return fromVertexTypeFilter;
    }

    public short getToVertexTypeFilter() {
        return toVertexTypeFilter;
    }

    public short getEdgeTypeFilter() {
        return edgeTypeFilter;
    }
}

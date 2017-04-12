package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;

/**
 * Represents a Generic Join rule. A Generic Join rule consists of the following: (1) A {@code
 * prefixIndex} that indicates the vertex {@code u} in the prefix that is being extended. (2) A
 * {@code direction} which indicates whether to extend {@code u} in the {@link
 * Graph.Direction#FORWARD} or {@link Graph.Direction#BACKWARD} direction. (3) A {@code
 * graphVersion} that indicates which graph the adjacency list of {@code u} should be used when
 * extending. (4) An {@code edgeTypeFilter} that filters the edges of {@code u} that do not
 * contain the given type. (5) {@code edgePropertyEqualityFilters} that filters the edges of {@code
 * u} that do not match the given properties.
 */
public class GenericJoinIntersectionRule {

    private int prefixIndex;
    private Direction direction;
    private GraphVersion graphVersion;
    private short fromVertexTypeFilter;
    private short toVertexTypeFilter;
    private short edgeTypeFilter;

    /**
     * Constructor for extending prefixes in the {@code graphVersion} by filtering edges that
     * do not match the given {@code fromVertexTypeFilter}, {@code toVertexTypeFilter}, and {@code
     * edgeTypeFilter}.
     *
     * @see #GenericJoinIntersectionRule(int, Direction, GraphVersion, short, short, short)
     */
    public GenericJoinIntersectionRule(int prefixIndex, Direction direction,
        short fromVertexTypeFilter, short toVertexTypeFilter, short edgeTypeFilter) {
        this(prefixIndex, direction, GraphVersion.PERMANENT, fromVertexTypeFilter,
            toVertexTypeFilter, edgeTypeFilter);
    }

    /**
     * Constructor for extending prefixes in the {@code graphVersion} by filtering edges that
     * do not match the given {@code fromVertexTypeFilter}, {@code toVertexTypeFilter}, and {@code
     * edgeTypeFilter}. Each prefix has an index indicating the vertex {@code v} from which the
     * rule extends.
     *
     * @param prefixIndex The index of the prefix indicating the vertex {@code v} from which this
     * rule will extend.
     * @param direction The direction of extension. Either {@link Graph.Direction#FORWARD} from
     * {@code v} or {@link Graph.Direction#BACKWARD} towards {@code v}.
     * @param graphVersion The version of the graph to be used for this rule.
     * @param fromVertexTypeFilter Filters {@code v}'s edges that do not have the given from vertex
     * type. If the value of {@code fromVertexTypeFilter} is {@link TypeAndPropertyKeyStore#ANY},
     * this parameter is ignored.
     * @param toVertexTypeFilter Filters {@code v}'s edges that do not have the given to vertex
     * type. If the value of {@code fromVertexTypeFilter} is {@link TypeAndPropertyKeyStore#ANY},
     * this parameter is ignored.
     * @param edgeTypeFilter Filters {@code v}'s edges that do not have the given type. If the
     * value of {@code edgeTypeFilter} is {@link TypeAndPropertyKeyStore#ANY}, this parameter is
     * ignored.
     */
    public GenericJoinIntersectionRule(int prefixIndex, Direction direction,
        GraphVersion graphVersion, short fromVertexTypeFilter, short toVertexTypeFilter,
        short edgeTypeFilter) {
        this.prefixIndex = prefixIndex;
        this.direction = direction;
        this.graphVersion = graphVersion;
        this.fromVertexTypeFilter = fromVertexTypeFilter;
        this.toVertexTypeFilter = toVertexTypeFilter;
        this.edgeTypeFilter = edgeTypeFilter;
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

    public int getPrefixIndex() {
        return prefixIndex;
    }

    public Direction getDirection() {
        return direction;
    }

    public GraphVersion getGraphVersion() {
        return graphVersion;
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     * @return {@code true} if the {@code a} object values are the same as the
     * {@code b} object values, {@code false} otherwise.
     */
    @UsedOnlyByTests
    public static boolean isSameAs(GenericJoinIntersectionRule a, GenericJoinIntersectionRule b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }
        return a.prefixIndex == b.prefixIndex && a.direction == b.direction &&
            a.graphVersion == b.graphVersion && a.edgeTypeFilter == b.edgeTypeFilter;
    }

    @Override
    public String toString() {
        return new StringBuilder()
            .append("prefixIndex: " + prefixIndex)
            .append(", direction: " + direction.name())
            .append(", graph-version: " + graphVersion.name())
            .append(", fromVertexTypeFilter: " + fromVertexTypeFilter)
            .append(", toVertexTypeFilter: " + toVertexTypeFilter)
            .append(", edgeTypeFilter: " + edgeTypeFilter)
            .toString();
    }
}

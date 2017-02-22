package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graph.Graph;
import ca.waterloo.dsg.graphflow.graph.Graph.Direction;
import ca.waterloo.dsg.graphflow.graph.Graph.GraphVersion;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.UsedOnlyByTests;
import org.antlr.v4.runtime.misc.Pair;

import java.util.Map;
import java.util.Map.Entry;

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
    private short edgeTypeFilter;
    private Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters;

    /**
     * Constructor for extending prefixes in the {@code graphVersion} by filtering edges that
     * do not match the given {@code edgeTypeFilter} and {@code edgePropertyEqualityFilters}.
     *
     * @see #GenericJoinIntersectionRule(int, Direction, GraphVersion, short, Map)
     */
    public GenericJoinIntersectionRule(int prefixIndex, Direction direction, short edgeTypeFilter,
        Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters) {
        this(prefixIndex, direction, GraphVersion.PERMANENT, edgeTypeFilter,
            edgePropertyEqualityFilters);
    }

    /**
     * Constructor for extending prefixes in the {@code graphVersion} by filtering edges that
     * do not match the given {@code edgeTypeFilter} and {@code edgePropertyEqualityFilters}.
     *
     * @param prefixIndex The index of the prefix indicating the vertex {@code u} from which this
     * rule will extend.
     * @param direction The direction of extension. Either {@link Graph.Direction#FORWARD} from
     * {@code u} or {@link Graph.Direction#BACKWARD} towards {@code u}.
     * @param graphVersion The version of the graph to be used for this rule.
     * @param edgeTypeFilter Filters {@code u}'s edges that do not have the given type. If the
     * value of {@code edgeTypeFilter} is {@link TypeAndPropertyKeyStore#ANY}, this parameter is
     * ignored.
     * @param edgePropertyEqualityFilters Filters {@code u}'s edges that do not contain these
     * properties. If the {@code propertyEqualityFilters} is {at code null} or empty, this
     * parameter is ignored.
     */
    public GenericJoinIntersectionRule(int prefixIndex, Direction direction,
        GraphVersion graphVersion, short edgeTypeFilter,
        Map<Short, Pair<DataType, String>> edgePropertyEqualityFilters) {
        this.prefixIndex = prefixIndex;
        this.direction = direction;
        this.graphVersion = graphVersion;
        this.edgeTypeFilter = edgeTypeFilter;
        this.edgePropertyEqualityFilters = edgePropertyEqualityFilters;
    }

    public short getEdgeTypeFilter() {
        return edgeTypeFilter;
    }

    public Map<Short, Pair<DataType, String>> getEdgePropertyEqualityFilters() {
        return edgePropertyEqualityFilters;
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
        if (a.prefixIndex != b.prefixIndex || a.direction != b.direction ||
            a.graphVersion != b.graphVersion || a.edgeTypeFilter != b.edgeTypeFilter) {
            return false;
        }
        if ((null != a.edgePropertyEqualityFilters && null == b.edgePropertyEqualityFilters) ||
            (null == a.edgePropertyEqualityFilters && null != b.edgePropertyEqualityFilters)) {
            return false;
        }
        if (null == a.edgePropertyEqualityFilters) {
            return true;
        }
        if (a.edgePropertyEqualityFilters.size() != b.edgePropertyEqualityFilters.size()) {
            return false;
        }
        Pair<DataType, String> aDataTypeStringPair;
        Pair<DataType, String> bDataTypeStringPair;
        for (Short key : a.edgePropertyEqualityFilters.keySet()) {
            aDataTypeStringPair = a.edgePropertyEqualityFilters.get(key);
            bDataTypeStringPair = b.edgePropertyEqualityFilters.get(key);
            if (null == bDataTypeStringPair || aDataTypeStringPair.a != bDataTypeStringPair.a ||
                !aDataTypeStringPair.b.equals(bDataTypeStringPair.b)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefixIndex: " + prefixIndex);
        stringBuilder.append(", direction: " + direction.name());
        stringBuilder.append(", graph-version: " + graphVersion.name());
        stringBuilder.append(", edgeTypeFilter: " + edgeTypeFilter);
        stringBuilder.append(", edgePropertyEqualityFilters: {");
        boolean isFirst = true;
        if (null != edgePropertyEqualityFilters) {
            for (Entry<Short, Pair<DataType, String>> equalityFilter
                : edgePropertyEqualityFilters.entrySet()) {
                if (!isFirst) {
                    stringBuilder.append(",");
                } else {
                    isFirst = false;
                }
                Short key = equalityFilter.getKey();
                DataType dataType = equalityFilter.getValue().a;
                String value = equalityFilter.getValue().b;
                stringBuilder.append("(key: " + key + ", dataType: " + (dataType == null ? "null"
                    : dataType.name()) + ", value: " + value + ")");
            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}

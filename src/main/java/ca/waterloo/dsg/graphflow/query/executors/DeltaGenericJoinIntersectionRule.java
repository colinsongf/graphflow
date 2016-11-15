package ca.waterloo.dsg.graphflow.query.executors;

import ca.waterloo.dsg.graphflow.graphmodel.Graph.GraphVersion;

/**
 * Represents a delta generic join rule. In addition to the standard intersection rule,
 * {code DeltaGenericJoinIntersectionRule} contains a parameter for the version of the graph to be
 * used.
 */
public class DeltaGenericJoinIntersectionRule extends GenericJoinIntersectionRule {

    private GraphVersion graphVersion;

    public DeltaGenericJoinIntersectionRule(int prefixIndex, GraphVersion graphVersion,
        boolean isForward) {
        super(prefixIndex, isForward);
        this.graphVersion = graphVersion;
    }

    public GraphVersion getVersion() {
        return graphVersion;
    }

    public void setVersion(GraphVersion version) {
        this.graphVersion = version;
    }

    @Override
    public String toString() {
        return "index: " + getPrefixIndex() + "isForward: " + isForward();
    }

    public boolean equalsTo(DeltaGenericJoinIntersectionRule that) {
        return super.isSameAs(that) && (this.graphVersion == that.graphVersion);
    }
}

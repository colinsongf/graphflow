package ca.waterloo.dsg.graphflow.queryprocessor;

/**
 * Represents a generic join rule consisting of a prefix index and a direction. The rule
 * will be part of a Generic Join stage and will be used to find possible extensions (new outgoing
 * or incoming edge) to a given element ({@code prefixIndex}) of the intermediate
 * result tuple (prefix).
 */
public class GenericJoinIntersectionRule {
    private int prefixIndex;
    private boolean isForward;

    public GenericJoinIntersectionRule(int prefixIndex, boolean isForward) {
        this.prefixIndex = prefixIndex;
        this.isForward = isForward;
    }

    public int getPrefixIndex() {
        return prefixIndex;
    }

    public void setPrefixIndex(int prefixIndex) {
        this.prefixIndex = prefixIndex;
    }

    public boolean isForward() {
        return isForward;
    }

    public void setForward(boolean isForward) {
        isForward = this.isForward;
    }
}

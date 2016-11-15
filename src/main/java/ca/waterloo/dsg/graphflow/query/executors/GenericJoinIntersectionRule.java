package ca.waterloo.dsg.graphflow.query.executors;

/**
 * Represents a generic join rule consisting of a prefix index and a direction. The rule will be
 * part of a Generic Join stage and will be used to find possible extensions (new outgoing or
 * incoming edge) to a given element ({@code prefixIndex}) of the intermediate result tuple prefix).
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

    public boolean isForward() {
        return isForward;
    }

    /**
     * Used in unit tests to assert the equality of the actual and expected objects.
     *
     * @param that The expected object.
     * @return {@code true} if the current object values match perfectly with the expected object
     * values, {@code false} otherwise.
     */
    public boolean isSameAs(GenericJoinIntersectionRule that) {
        if (that == null) {
            return false;
        }
        if (this == that) {
            return true;
        }
        return (this.prefixIndex == that.prefixIndex && this.isForward == that.isForward);
    }
}

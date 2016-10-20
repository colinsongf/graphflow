package ca.waterloo.dsg.graphflow.queryprocessor;

/**
 * Represents a join rule consisting of a prefix index and a direction.
 */
public class JoinRule {
  private int prefixIndex;
  private boolean isForward;

  public JoinRule() {}

  public JoinRule(int prefixIndex, boolean isForward) {
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

  public void setForward(boolean forward) {
    isForward = forward;
  }
}

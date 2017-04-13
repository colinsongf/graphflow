package ca.waterloo.dsg.graphflow.query.operator.aggregator;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;

/**
 * Aggregator that keeps running sums of different keys.
 */
public class Sum extends AbstractLongDoubleAggregator {

    @Override
    protected void aggregateInt(int index, int intValue) {
        this.longValues = ArrayUtils.resizeIfNecessary(longValues, index + 1,
            0 /* value to fill new cells if resizing */);
        this.longValues[index] += intValue;
    }

    @Override
    protected void aggregateDouble(int index, double doubleValue) {
        this.doubleValues = ArrayUtils.resizeIfNecessary(doubleValues, index + 1,
            0 /* value to fill new cells if resizing */);
        this.doubleValues[index] += doubleValue;
    }
}

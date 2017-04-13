package ca.waterloo.dsg.graphflow.query.operator.aggregator;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;

/**
 * Aggregator that keeps running minimums of different keys.
 */
public class Min extends AbstractLongDoubleAggregator {

    @Override
    protected void aggregateInt(int index, int intValue) {
        this.longValues = ArrayUtils.resizeIfNecessary(longValues, index + 1,
            Long.MAX_VALUE /* value to fill new cells if resizing */);
        this.longValues[index] = Long.min(this.longValues[index], intValue);
    }

    @Override
    protected void aggregateDouble(int index, double doubleValue) {
        this.doubleValues = ArrayUtils.resizeIfNecessary(doubleValues, index + 1,
            Double.MAX_VALUE /* value to fill new cells if resizing */);
        this.doubleValues[index] = Double.min(this.doubleValues[index], doubleValue);
    }
}

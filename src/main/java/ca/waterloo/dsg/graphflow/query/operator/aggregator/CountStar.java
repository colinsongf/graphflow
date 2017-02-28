package ca.waterloo.dsg.graphflow.query.operator.aggregator;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;

/**
 * Aggregator that counts the number of times different keys have been incremented.
 */
public class CountStar extends AbstractAggregator {

    @Override
    protected void aggregateInt(int index, int intValue) {
        this.longValues = ArrayUtils.resizeIfNecessary(longValues, index + 1,
            0 /* value to fill new cells if resizing */);
        longValues[index]++;
    }
}

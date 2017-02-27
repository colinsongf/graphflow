package ca.waterloo.dsg.graphflow.query.operator.aggregator;

import ca.waterloo.dsg.graphflow.util.ArrayUtils;

/**
 * Aggregator that keeps running averages of different keys.
 */
public class Average extends AbstractLongDoubleAggregator {

    protected long[] counts;
    
    /**
     * Default constructor.
     */
    public Average() {
        super();
        this.counts = new long[0];
    }

    @Override
    protected void aggregateInt(int index, int intValue) {
        System.out.println("Average Aggregator. aggregating index: " + index + " intValue: "
            + intValue);
        adjustSizeAndIncrementCount(index);
        this.longValues = ArrayUtils.resizeIfNecessary(this.longValues, index + 1, 
            0 /* value to fill new cells if resizing */);
        this.longValues[index] += intValue;
    }

    @Override
    protected void aggregateDouble(int index, double doubleValue) {
        System.out.println("Average Aggregator. aggregating index: " + index + " double: "
            + doubleValue);
        adjustSizeAndIncrementCount(index);
        this.doubleValues = ArrayUtils.resizeIfNecessary(doubleValues, index + 1,
            0.0 /* value to fill new cells if resizing */);
        this.doubleValues[index] += doubleValue;
    }

    private void adjustSizeAndIncrementCount(int index) {
        this.counts = ArrayUtils.resizeIfNecessary(this.counts, index + 1,
            0 /* value to fill new cells if resizing */);
        this.counts[index] += 1;
    }

    @Override
    public String getStringValue(int index) {
        if (this.doubleValues.length > 0) {
            return "" + (this.doubleValues[index] / (double) this.counts[index]);
        }
        return "" + ((double) this.longValues[index] / (double) this.counts[index]);
    }
}

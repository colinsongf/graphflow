package ca.waterloo.dsg.graphflow.query.operator.aggregator;

/**
 * Abstract class for aggregators. Aggregators maintain arrays of values where each index in the
 * array corresponds to a running aggregated value for a groupBy key. Therefore each index can be
 * seen as a different groupBy key.
 */
public abstract class AbstractAggregator {

    protected long[] longValues;

    /**
     * Default constructor.
     */
    protected AbstractAggregator() {
        longValues = new long[0];
    }

    /**
     * Aggregates the given {@link Object} value into the running aggregate value of the given
     * index. This method reads the class of the given {@link Object} value and calls
     * the {@link #aggregateDouble(int, double)} or {@link #aggregateInt(int, int)} method which
     * should be implemented by classes extending this class.
     *
     * @param index of the running aggregate.
     * @param value value to aggregate into the running aggregate for index.
     */
    public void aggregate(int index, Object value) {
        if (null == value) {
            throw new IllegalArgumentException("Cannot aggregate null value. index: " + index +
                " object: null");
        }
        if (value instanceof Double) {
            aggregateDouble(index, (double) value);
        } else if (value instanceof Integer) {
            aggregateInt(index, (int) value);
        } else {
            throw new UnsupportedOperationException("Aggregating instances of " +
                value.getClass().getSimpleName() + " is not supported by " +
                this.getClass().getSimpleName() + ".");
        }
    }

    /**
     * Aggregates the given integer value into the running aggregate value of the given
     * index.
     *
     * @param index of the running aggregate.
     * @param intValue integer value to aggregate into the running aggregate for index.
     */
    protected void aggregateInt(int index, int intValue) {
        throw new UnsupportedOperationException("Aggregating int values is not supported by " +
            this.getClass().getSimpleName() + ".");
    }

    /**
     * Aggregates the given double value into the running aggregate value of the given
     * index.
     *
     * @param index of the running aggregate.
     * @param doubleValue double value to aggregate into the running aggregate for index.
     */
    protected void aggregateDouble(int index, double doubleValue) {
        throw new UnsupportedOperationException("Aggregating double values is not supported by " +
            this.getClass().getSimpleName() + ".");
    }

    /**
     * @param index index of an aggregate.
     *
     * @return {@link String} representation of the aggregate value at the given index.
     */
    public String getStringValue(int index) {
        return "" + longValues[index];
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}

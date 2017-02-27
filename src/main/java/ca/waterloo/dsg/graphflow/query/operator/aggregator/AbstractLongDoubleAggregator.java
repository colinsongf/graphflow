package ca.waterloo.dsg.graphflow.query.operator.aggregator;

/**
 * Base class for aggregators that may need to maintain both a long array and a double array.
 * For example the {@link Average} aggregator needs to keep a running long sum when it is
 * aggregated with integer values and a double sum when it is aggregated with double values.
 */
public abstract class AbstractLongDoubleAggregator extends AbstractAggregator {

    protected double[] doubleValues;

    /**
     * Default constructor.
     */
    protected AbstractLongDoubleAggregator() {
        super();
        doubleValues = new double[0];
    }

    @Override
    public String getStringValue(int index) {
        if (doubleValues.length > 0) {
            return "" + doubleValues[index];
        }
        return "" + longValues[index];
    }
}

package ca.waterloo.dsg.graphflow.query.structuredquery;

import org.antlr.v4.runtime.misc.Pair;

import java.util.Objects;

/**
 * Represents an aggregation function specified in the RETURN statement. This class is used by
 * {@link StructuredQuery}.
 */
public class QueryAggregation {

    public enum AggregationFunction {
        AVG,
        COUNT_STAR,
        MAX,
        MIN,
        SUM
    }

    /**
     * Singleton object for referring to the count(*) aggregations in the query.
     */
    public static final QueryAggregation COUNT_STAR = new QueryAggregation();

    private AggregationFunction aggregationFunction;
    private String variable;
    private Pair<String, String> variablePropertyPair;

    /**
     * Constructor used only for the singleton {@link #COUNT_STAR} object.
     */
    private QueryAggregation() {
        this.aggregationFunction = AggregationFunction.COUNT_STAR;
        this.variable = null;
        this.variablePropertyPair = null;
    }

    /**
     * Constructor used for aggregation functions that aggregate a vertex or edge variable.
     *
     * @param aggregationFunction aggregation function in the RETURN statement.
     * @param variable variable inside the aggregation function.
     */
    public QueryAggregation(AggregationFunction aggregationFunction, String variable) {
        this.aggregationFunction = aggregationFunction;
        this.variable = variable;
        this.variablePropertyPair = null;
    }

    /**
     * Constructor used for aggregation functions that aggregate a vertex or edge variable
     * property.
     *
     * @param aggregationFunction aggregation function in the RETURN statement.
     * @param variableWithProperty variable.property inside the aggregation function.
     */
    public QueryAggregation(AggregationFunction aggregationFunction,
        Pair<String, String> variableWithProperty) {
        this.aggregationFunction = aggregationFunction;
        this.variablePropertyPair = variableWithProperty;
        this.variable = null;
    }

    public AggregationFunction getAggregationFunction() {
        return aggregationFunction;
    }

    public String getVariable() {
        return variable;
    }

    public Pair<String, String> getVariablePropertyPair() {
        return variablePropertyPair;
    }

    @Override
    public String toString() {
        if (COUNT_STAR == this) {
            return "count(*)";
        }
        String tmpString = aggregationFunction.name() + "(";
        tmpString += (null != variable) ? variable : variablePropertyPair.a + "." +
            variablePropertyPair.b;
        return tmpString + ")";
    }

    /**
     * Used during unit testing to check the equality of objects. This is used instead of
     * overriding the standard {@code equals()} and {@code hashCode()} methods.
     *
     * @param a One of the objects.
     * @param b The other object.
     *
     * @return {@code true} if the {@code a} object values are the same as the {@code b} object
     * values, {@code false} otherwise.
     */
    public static boolean isSameAs(QueryAggregation a, QueryAggregation b) {
        if (a == b) {
            return true;
        }
        if (null == a || null == b) {
            return false;
        }

        return Objects.equals(a.aggregationFunction, b.aggregationFunction) &&
            Objects.equals(a.variable, b.variable) &&
            Objects.equals(a.variablePropertyPair, b.variablePropertyPair);
    }
}

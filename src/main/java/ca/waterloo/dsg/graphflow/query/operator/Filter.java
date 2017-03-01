package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Operator for filtering the output from a MATCH query based on a set of comparison predicates.
 * The comparisons are specified in the WHERE clause of the MATCH query and their conjunction is
 * used for filtering. The comparisons from the query are used to form a {@link Predicate} which
 * tests each {@link MatchQueryOutput}.
 */
public class Filter extends PropertyReadingOperator {

    private final Predicate<String[]> filterPredicate;
    private final List<QueryPropertyPredicate> queryPropertyPredicates;
    private static final String FILTER_DELIMITER = "%%";

    /**
     * Default Constructor.
     * @param nextOperator the next operator to be called with the results of {@code Filter}.
     * @param filterPredicate a composite {@link Predicate<String[]>} representing all the filter
     * predicates for a MATCH query ANDed together.
     * @param edgeOrVertexPropertyDescriptors a {@link EdgeOrVertexPropertyDescriptor} list
     * specifying parameters for retrieving the list of properties used by the {@link Predicate}s.
     * The ordering of {@link EdgeOrVertexPropertyDescriptor} is also the ordering of properties
     * in the array submitted to the {@code filterPredicate}.
     * @param queryPropertyPredicates
     */
    public Filter(AbstractDBOperator nextOperator, Predicate<String[]> filterPredicate,
        List<EdgeOrVertexPropertyDescriptor> edgeOrVertexPropertyDescriptors,
        List<QueryPropertyPredicate> queryPropertyPredicates) {
        super(nextOperator, edgeOrVertexPropertyDescriptors);
        this.filterPredicate = filterPredicate;
        this.queryPropertyPredicates = queryPropertyPredicates;
    }

    @Override
    public void append(MatchQueryOutput matchQueryOutput) {
        clearAndFillStringBuilder(matchQueryOutput, FILTER_DELIMITER);
        String[] properties = stringBuilder.toString().split(FILTER_DELIMITER);
        System.out.println(Arrays.toString(properties));
        if (filterPredicate.test(properties)) {
            nextOperator.append(matchQueryOutput);
        }
    }

    @Override
    protected String getHumanReadableOperator() {
        StringBuilder stringBuilder = new StringBuilder("Filter:\n");
        appendListAsCommaSeparatedString(stringBuilder, queryPropertyPredicates,
            "filterPredicates");
        return stringBuilder.toString();
    }
}

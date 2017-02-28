package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;

import java.util.List;
import java.util.function.Predicate;

public class Filter extends AbstractDBOperator {

    private final Predicate<MatchQueryOutput> filterPredicate;
    private final List<QueryPropertyPredicate> queryPropertyPredicates;

    public Filter(AbstractDBOperator nextOperator, Predicate<MatchQueryOutput> filterPredicate,
        List<QueryPropertyPredicate> queryPropertyPredicates) {
        super(nextOperator);
        this.filterPredicate = filterPredicate;
        this.queryPropertyPredicates = queryPropertyPredicates;
    }

    public void append(MatchQueryOutput matchQueryOutput) {
        if (filterPredicate.test(matchQueryOutput)) {
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

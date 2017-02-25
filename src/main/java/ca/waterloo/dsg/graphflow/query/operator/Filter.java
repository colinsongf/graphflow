package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;

import java.util.function.Predicate;

public class Filter extends AbstractDBOperator {

    private final Predicate<MatchQueryOutput> filterPredicate;

    public Filter(AbstractDBOperator nextOperator, Predicate<MatchQueryOutput> filterPredicate) {
        super(nextOperator);
        this.filterPredicate = filterPredicate;
    }

    public void append(MatchQueryOutput matchQueryOutput) {
        if (filterPredicate.test(matchQueryOutput)) {
            nextOperator.append(matchQueryOutput);
        }
    }

    @Override
    protected String getHumanReadableOperator() {
        return null;
    }
}

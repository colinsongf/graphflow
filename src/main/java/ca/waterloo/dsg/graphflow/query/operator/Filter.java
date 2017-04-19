package ca.waterloo.dsg.graphflow.query.operator;

import ca.waterloo.dsg.graphflow.query.output.MatchQueryOutput;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.util.JsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Operator for filtering the output from a MATCH query based on a set of comparison predicates.
 * The comparisons are specified in the WHERE clause of the MATCH query and their conjunction is
 * used for filtering. The comparisons from the query are used to construct a {@link Predicate}
 * which tests each {@link MatchQueryOutput}.
 */
public class Filter extends PropertyReadingOperator {

    private static final Logger logger = LogManager.getLogger(Filter.class);
    private static final String FILTER_DELIMITER = "%%";
    private final Predicate<String[]> filterPredicate;
    private final List<QueryPropertyPredicate> queryPropertyPredicates;

    /**
     * Default constructor.
     *
     * @param nextOperator Next operator to append outputs to.
     * @param filterPredicate A composite {@link Predicate<String[]>} representing all the filter
     * predicates for a MATCH query ANDed together.
     * @param edgeOrVertexPropertyDescriptors A {@link EdgeOrVertexPropertyDescriptor} list
     * specifying parameters for retrieving the list of properties used by the {@link Predicate}s.
     * @param queryPropertyPredicates The predicates used for filtering the MATCH output.
     */
    public Filter(AbstractDBOperator nextOperator, Predicate<String[]> filterPredicate,
        List<EdgeOrVertexPropertyDescriptor> edgeOrVertexPropertyDescriptors,
        List<QueryPropertyPredicate> queryPropertyPredicates) {
        super(nextOperator, edgeOrVertexPropertyDescriptors);
        this.filterPredicate = filterPredicate;
        this.queryPropertyPredicates = queryPropertyPredicates;
        logger.info(this.queryPropertyPredicates.toString());
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

    @Override
    public JsonObject toJson() {
        JsonObject jsonFilter = new JsonObject();

        JsonArray jsonPropertyPredicates = new JsonArray();
        for (int i = 0; i < queryPropertyPredicates.size(); i++) {
            jsonPropertyPredicates.add(queryPropertyPredicates.get(i).toJson());
        }
        JsonArray jsonArguments = new JsonArray();
        JsonObject jsonArgument = new JsonObject();
        jsonArgument.addProperty(JsonUtils.NAME, "Vertex Indices");
        jsonArgument.add(JsonUtils.VALUE, jsonPropertyPredicates);
        jsonArguments.add(jsonArgument);
        jsonFilter.addProperty(JsonUtils.NAME, "Filter (&sigma;)");
        jsonFilter.add(JsonUtils.ARGS, jsonArguments);

        return jsonFilter;
    }
}

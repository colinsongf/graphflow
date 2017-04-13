package ca.waterloo.dsg.graphflow.query.validators;

import ca.waterloo.dsg.graphflow.exceptions.IncorrectDataTypeException;
import ca.waterloo.dsg.graphflow.exceptions.IncorrectVertexTypeException;
import ca.waterloo.dsg.graphflow.exceptions.MalformedMatchQueryException;
import ca.waterloo.dsg.graphflow.exceptions.MalformedReturnClauseException;
import ca.waterloo.dsg.graphflow.exceptions.NoSuchPropertyKeyException;
import ca.waterloo.dsg.graphflow.graph.TypeAndPropertyKeyStore;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryAggregation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryGraph;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate.PredicateType;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.util.DataType;
import org.antlr.v4.runtime.misc.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Given a MATCH {@link StructuredQuery}, the class constructs a {@link QueryGraph} for the MATCH
 * Query Planners and validates the syntax and semantics of the query.
 */
public class MatchQueryValidator {

    private static final String UNDEFINED_VARIABLE_ERROR_MESSAGE = "RETURN clause contains " +
        "variables that are not defined in the MATCH clause.";

    QueryGraph queryGraph = new QueryGraph();
    StructuredQuery structuredQuery;

    public MatchQueryValidator(StructuredQuery structuredQuery) {
        this.structuredQuery = structuredQuery;
    }

    public QueryGraph validateQueryAndGetQueryGraph() {
        Map<String, String> variableTypeMap = checkQueryVariableTypesAreConsistent();
        setMissingVariableTypes(variableTypeMap);
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortAndAssertTypeExists(
                queryRelation.getRelationType());
            TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortAndAssertTypeExists(
                queryRelation.getFromQueryVariable().getVariableType());
            TypeAndPropertyKeyStore.getInstance().mapStringTypeToShortAndAssertTypeExists(
                queryRelation.getToQueryVariable().getVariableType());
            queryGraph.addRelation(queryRelation);
        }
        checkReturnVariablesAndPropertiesAreWellFormed();
        checkEdgeVariablesAreDistinctFromVertexVariables();
        checkPredicateVariablesAndPropertiesAreWellFormed();
        return queryGraph;
    }

    private void checkEdgeVariablesAreDistinctFromVertexVariables() {
        Set<String> variableNames = queryGraph.getAllVariableNames();
        for (String relationName : queryGraph.getAllRelationNames()) {
            if (variableNames.contains(relationName)) {
                throw new MalformedMatchQueryException("Edge variable: " + relationName +
                    " has also been defined as a vertex variable in the query.");
            }
        }
    }

    private void checkReturnVariablesAndPropertiesAreWellFormed() {
        for (String variable : structuredQuery.getReturnVariables()) {
            checkVariableIsDefined(variable);
        }
        for (Pair<String, String> variablePropertyPair :
            structuredQuery.getReturnVariablePropertyPairs()) {
            checkVariableIsDefinedAndPropertyExists(variablePropertyPair);
        }
        for (QueryAggregation queryAggregation : structuredQuery.getQueryAggregations()) {
            if (null != queryAggregation.getVariable()) {
                checkVariableIsDefined(queryAggregation.getVariable());
            } else if (null != queryAggregation.getVariablePropertyPair()) {
                checkVariableIsDefinedAndPropertyExists(queryAggregation.getVariablePropertyPair());
            }
        }
    }

    private void checkVariableIsDefinedAndPropertyExists(Pair<String, String>
        variablePropertyPair) {
        String variable = variablePropertyPair.a;
        checkVariableIsDefined(variable);
        String propertyKey = variablePropertyPair.b;
        if (!TypeAndPropertyKeyStore.getInstance().isPropertyDefined(propertyKey)) {
            throw new NoSuchPropertyKeyException(propertyKey);
        }
    }

    private void checkVariableIsDefined(String variable) {
        if (!queryGraph.getAllVariableNames().contains(variable) &&
            !queryGraph.getAllRelationNames().contains(variable)) {
            throw new MalformedReturnClauseException(UNDEFINED_VARIABLE_ERROR_MESSAGE);
        }
    }

    private void checkPredicateVariablesAndPropertiesAreWellFormed() {
        Pair<Short, DataType> leftOperandKeyAndDataType, rightOperandKeyAndDataType;
        // GraphflowVisitor ensures that the leftOperand is always a variableWithProperty and not
        // a literal.
        for (QueryPropertyPredicate predicate : structuredQuery.getQueryPropertyPredicates()) {
            checkVariableIsDefined(predicate.getVariable1().a);
            leftOperandKeyAndDataType = getKeyAndDataTypePair(predicate.getVariable1().b);
            if (PredicateType.TWO_VARIABLES == predicate.getPredicateType()) {
                checkVariableIsDefined(predicate.getVariable2().a);
                rightOperandKeyAndDataType = getKeyAndDataTypePair(predicate.getVariable2().b);
                if ((!isNumeric(leftOperandKeyAndDataType.b) ||
                    !isNumeric(rightOperandKeyAndDataType.b)) &&
                    leftOperandKeyAndDataType.b != rightOperandKeyAndDataType.b) {
                    throw new IncorrectDataTypeException("DataType Mismatch - The left " +
                        "operand " + predicate.getVariable1().a + "." + predicate.
                        getVariable1().b + " is of data type " + leftOperandKeyAndDataType.b +
                        " and the right operand " + predicate.getVariable2().a + "." +
                        predicate.getVariable2().b + " is of data type " +
                        rightOperandKeyAndDataType.b + ".");
                }
            } else {
                DataType.assertValueCanBeCastToDataType(leftOperandKeyAndDataType.b, predicate.
                    getLiteral());
            }
        }
    }

    private boolean isNumeric(DataType operandDataType) {
        return (operandDataType == DataType.INTEGER || operandDataType == DataType.DOUBLE);
    }

    private Map<String, String> checkQueryVariableTypesAreConsistent() {
        Map<String, String> variableTypeMap = new HashMap<>();
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            QueryVariable fromQueryVariable = queryRelation.getFromQueryVariable();
            QueryVariable toQueryVariable = queryRelation.getToQueryVariable();
            assetQueryVariableTypeIsConsistent(fromQueryVariable.getVariableName(),
                fromQueryVariable.getVariableType(), variableTypeMap);
            assetQueryVariableTypeIsConsistent(toQueryVariable.getVariableName(),
                toQueryVariable.getVariableType(), variableTypeMap);
        }
        return variableTypeMap;
    }

    private void setMissingVariableTypes(Map<String, String> variableTypeMap) {
        for (QueryRelation queryRelation : structuredQuery.getQueryRelations()) {
            String fromQueryVariable = queryRelation.getFromQueryVariable().getVariableName();
            String toQueryVariable = queryRelation.getToQueryVariable().getVariableName();
            String fromQueryType = variableTypeMap.get(fromQueryVariable);
            String toQueryType = variableTypeMap.get(toQueryVariable);
            queryRelation.getFromQueryVariable().setVariableType(fromQueryType);
            queryRelation.getToQueryVariable().setVariableType(toQueryType);
        }
    }

    private void assetQueryVariableTypeIsConsistent(String variableName, String variableType,
        Map<String, String> variableTypeMap) {
        if (!variableTypeMap.containsKey(variableName) ||
            null == variableTypeMap.get(variableName)) {
            variableTypeMap.put(variableName, variableType);
        } else if (null != variableType &&
            !variableType.equals(variableTypeMap.get(variableName))) {
            throw new IncorrectVertexTypeException("Incorrect type usage - The query variable '" +
                variableName + "' in the MATCH clause is used with two different types: '" +
                variableType + "' and '" + variableTypeMap.get(variableName) + "'.");
        }
    }

    private Pair<Short, DataType> getKeyAndDataTypePair(String key) {
        Short operandKey = TypeAndPropertyKeyStore.getInstance().mapStringPropertyKeyToShort(key);
        if (null == operandKey) {
            throw new NoSuchPropertyKeyException(key);
        }
        DataType leftOperandDataType = TypeAndPropertyKeyStore.getInstance().getPropertyDataType(
            operandKey);
        return new Pair<>(operandKey, leftOperandDataType);
    }
}

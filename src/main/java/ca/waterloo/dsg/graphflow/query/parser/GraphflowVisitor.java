package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.exceptions.MalformedWhereClauseException;
import ca.waterloo.dsg.graphflow.grammar.GraphflowBaseVisitor;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.*;
import ca.waterloo.dsg.graphflow.query.structuredquery.AbstractStructuredQuery;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryAggregation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryAggregation.AggregationFunction;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryPropertyPredicate.PredicateType;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery.QueryOperation;
import ca.waterloo.dsg.graphflow.util.DataType;
import ca.waterloo.dsg.graphflow.util.RuntimeTypeBasedComparator.ComparisonOperator;
import org.antlr.v4.runtime.misc.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the ANTLR4 methods used to traverse the parse tree.
 * Query structure is encapsulated within a {@code StructuredQuery} object.
 */
public class GraphflowVisitor extends GraphflowBaseVisitor<AbstractStructuredQuery> {

    @Override
    public AbstractStructuredQuery visitGraphflow(GraphflowContext ctx) {
        return visit(ctx.statement());
    }

    @Override
    public AbstractStructuredQuery visitMatchQuery(MatchQueryContext ctx) {
        StructuredQuery structuredQuery = (StructuredQuery) visit(ctx.matchPattern());
        structuredQuery.setQueryOperation(QueryOperation.MATCH);
        if (null != ctx.returnClause()) {
            visitReturnClauseAndAggregations(structuredQuery, ctx.returnClause());
        }
        if (null != ctx.whereClause()) {
            visitWhereClause(structuredQuery, ctx.whereClause());
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitContinuousMatchQuery(ContinuousMatchQueryContext ctx) {
        StructuredQuery structuredQuery = (StructuredQuery) visit(ctx.matchPattern());
        structuredQuery.setQueryOperation(QueryOperation.CONTINUOUS_MATCH);
        if (null != ctx.whereClause()) {
            visitWhereClause(structuredQuery, ctx.whereClause());
        }
        if (null != ctx.udfCall()) {
            UdfCallContext udfCtx = ctx.udfCall();
            structuredQuery.setContinuousMatchAction(udfCtx.functionName().getText());
            structuredQuery.setContinuousMatchOutputLocation(udfCtx.filePath().getText() + ".jar");
        } else {
            FileSinkContext fileSinkCtx = ctx.fileSink();
            structuredQuery.setFilePath(fileSinkCtx.filePath().getText());
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitExplainMatchQuery(ExplainMatchQueryContext ctx) {
        StructuredQuery structuredQuery = (StructuredQuery) visit(ctx.matchQuery());
        structuredQuery.setQueryOperation(QueryOperation.EXPLAIN);
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitExplainContinuousMatchQuery
        (ExplainContinuousMatchQueryContext ctx) {
        StructuredQuery structuredQuery = (StructuredQuery) visit(ctx.continuousMatchQuery());
        structuredQuery.setQueryOperation(QueryOperation.CONTINUOUS_EXPLAIN);
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitShortestPathQuery(ShortestPathQueryContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.SHORTEST_PATH);
        structuredQuery.addRelation((QueryRelation) visit(ctx.pathPattern()));
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitDurabilityQuery(DurabilityQueryContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        if (null != ctx.LOAD()) {
            structuredQuery.setQueryOperation(QueryOperation.LOAD_GRAPH);
        } else {
            structuredQuery.setQueryOperation(QueryOperation.SAVE_GRAPH);
        }
        structuredQuery.setFilePath(ctx.filePath().getText());
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitMatchPattern(MatchPatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        for (int i = 0; i < ctx.variableEdge().size(); i++) {
            visitVariableEdge(ctx.variableEdge(i), structuredQuery);
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitDeletePattern(DeletePatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.DELETE);
        for (int i = 0; i < ctx.digitsEdgeWithOptionalType().size(); i++) {
            structuredQuery.addRelation((QueryRelation) visit(ctx.digitsEdgeWithOptionalType(i)));
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitCreateEdgePattern(CreateEdgePatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.CREATE);
        for (int i = 0; i < ctx.digitsEdgeWithTypeAndProperties().size(); i++) {
            structuredQuery.addRelation((QueryRelation) visit(ctx.digitsEdgeWithTypeAndProperties(
                i)));
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitCreateVertexPattern(CreateVertexPatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.CREATE);
        for (int i = 0; i < ctx.digitsVertexWithTypeAndProperties().size(); i++) {
            structuredQuery.addVariable((QueryVariable) visit(ctx.digitsVertexWithTypeAndProperties(
                i)));
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitPathPattern(PathPatternContext ctx) {
        return new QueryRelation(new QueryVariable(ctx.Digits(0).getText()), new QueryVariable(ctx.
            Digits(1).getText()));
    }

    @Override
    public AbstractStructuredQuery visitDigitsEdgeWithOptionalType(
        DigitsEdgeWithOptionalTypeContext ctx) {
        QueryRelation queryRelation = new QueryRelation((QueryVariable) visit(ctx.digitsVertex(0)),
            (QueryVariable) visit(ctx.digitsVertex(1)));
        if (null != ctx.edgeType()) {
            queryRelation.setRelationType(ctx.edgeType().type().getText());
        }
        return queryRelation;
    }

    @Override
    public AbstractStructuredQuery visitDigitsEdgeWithTypeAndProperties(
        DigitsEdgeWithTypeAndPropertiesContext ctx) {
        QueryRelation queryRelation = new QueryRelation((QueryVariable) visit(ctx.
            digitsVertexWithTypeAndProperties(0)), (QueryVariable) visit(ctx.
            digitsVertexWithTypeAndProperties(1)));
        if (null != ctx.edgeTypeAndOptionalProperties().type()) {
            queryRelation.setRelationType(ctx.edgeTypeAndOptionalProperties().type().getText());
        }
        if (null != ctx.edgeTypeAndOptionalProperties().properties()) {
            queryRelation.setRelationProperties(parseProperties(ctx.edgeTypeAndOptionalProperties().
                properties()));
        }
        return queryRelation;
    }

    @Override
    public AbstractStructuredQuery visitDigitsVertex(DigitsVertexContext ctx) {
        return new QueryVariable(ctx.Digits().getText());
    }

    @Override
    public AbstractStructuredQuery visitDigitsVertexWithTypeAndProperties(
        DigitsVertexWithTypeAndPropertiesContext ctx) {
        QueryVariable queryVariable = new QueryVariable(ctx.Digits().getText());
        if (null != ctx.type()) {
            queryVariable.setVariableType(ctx.type().getText());
        }
        if (null != ctx.properties()) {
            queryVariable.setVariableProperties(parseProperties(ctx.properties()));
        }
        return queryVariable;
    }

    private void visitReturnClauseAndAggregations(StructuredQuery structuredQuery,
        ReturnClauseContext returnClauseCtx) {
        for (VariableContext variableContext : returnClauseCtx.variable()) {
            structuredQuery.addReturnVariable(variableContext.getText());
        }
        for (VariableWithPropertyContext variableWithPropertyCtx :
            returnClauseCtx.variableWithProperty()) {
            structuredQuery.addReturnVariablePropertyPair(new Pair<>(
                variableWithPropertyCtx.variable().getText(),
                variableWithPropertyCtx.key().getText()));
        }

        for (AggregationPatternContext aggregationCtx :
            returnClauseCtx.aggregationPattern()) {
            if (null != aggregationCtx.countStarPattern()) {
                structuredQuery.addQueryAggregation(QueryAggregation.COUNT_STAR);
                continue;
            }
            AggregationFunction aggregationFunction = AggregationFunction.valueOf(
                aggregationCtx.aggregationFunction().getText().toUpperCase());
            if (null != aggregationCtx.variable()) {
                structuredQuery.addQueryAggregation(new QueryAggregation(aggregationFunction,
                    aggregationCtx.variable().getText()));
            } else {
                structuredQuery.addQueryAggregation(new QueryAggregation(aggregationFunction,
                    new Pair<>(aggregationCtx.variableWithProperty().variable().getText(),
                        aggregationCtx.variableWithProperty().key().getText())));
            }
        }
    }

    private void visitWhereClause(StructuredQuery structuredQuery, WhereClauseContext ctx) {
        for (int i = 0; i < ctx.predicates().predicate().size(); i++) {
            QueryPropertyPredicate queryPropertyPredicate = new QueryPropertyPredicate();
            OperandContext leftOperandCtx = ctx.predicates().predicate(i).operand(0);
            OperandContext rightOperandCtx = ctx.predicates().predicate(i).operand(1);

            if (null != leftOperandCtx.literal() && null != rightOperandCtx.literal()) {
                throw new MalformedWhereClauseException("Both operands of a where clause can't be" +
                    " literals.");
            } else if (null != leftOperandCtx.variableWithProperty() && null != rightOperandCtx.
                variableWithProperty()) {
                queryPropertyPredicate.setVariable1(new Pair<>(leftOperandCtx.
                    variableWithProperty().variable().getText(), leftOperandCtx.
                    variableWithProperty().key().getText()));
                queryPropertyPredicate.setVariable2(new Pair<>(rightOperandCtx.
                    variableWithProperty().variable().getText(), rightOperandCtx.
                    variableWithProperty().key().getText()));
            } else if (null != leftOperandCtx.variableWithProperty() && null != rightOperandCtx.
                literal()) {
                queryPropertyPredicate.setVariable1(new Pair<>(leftOperandCtx.
                    variableWithProperty().variable().getText(), leftOperandCtx.
                    variableWithProperty().key().getText()));
                queryPropertyPredicate.setLiteral(rightOperandCtx.literal().getText().
                    replaceAll("\'", ""));
            } else {
                queryPropertyPredicate.setVariable1(new Pair<>(rightOperandCtx.
                    variableWithProperty().variable().getText(), rightOperandCtx.
                    variableWithProperty().key().getText()));
                queryPropertyPredicate.setLiteral(leftOperandCtx.literal().getText().
                    replaceAll("\'", ""));
            }

            if (null == rightOperandCtx.literal() && null == leftOperandCtx.literal()) {
                queryPropertyPredicate.setPredicateType(PredicateType.TWO_VARIABLES);
            } else {
                queryPropertyPredicate.setPredicateType(PredicateType.VARIABLE_AND_LITERAL);
            }

            queryPropertyPredicate.setComparisonOperator(ComparisonOperator.
                mapStringToComparisonOperator(ctx.predicates().predicate(i).operator().getText()));
            if (null != leftOperandCtx.literal()) {
                queryPropertyPredicate.invertComparisonOperator();
            }

            structuredQuery.addQueryPropertyPredicate(queryPropertyPredicate);
        }
    }

    private void visitVariableEdge(VariableEdgeContext ctx, StructuredQuery structuredQuery) {
        QueryRelation queryRelation = new QueryRelation((QueryVariable) visitVariableVertex(
            structuredQuery, ctx.variableVertex(0)), (QueryVariable) visitVariableVertex(
            structuredQuery, ctx.variableVertex(1)));
        if (null != ctx.edgeVariable()) {
            if (null != ctx.edgeVariable().variable()) {
                queryRelation.setRelationName(ctx.edgeVariable().variable().getText());
            }
            if (null != ctx.edgeVariable().type()) {
                queryRelation.setRelationType(ctx.edgeVariable().type().getText());
            }
            if (null != ctx.edgeVariable().propertyFilters()) {
                Map<String, String> relationPropertyFilters = parsePropertyFilters(ctx.
                    edgeVariable().propertyFilters());
                QueryPropertyPredicate queryPropertyPredicate;
                for (String key : relationPropertyFilters.keySet()) {
                    queryPropertyPredicate = new QueryPropertyPredicate();
                    queryPropertyPredicate.setVariable1(new Pair<>(ctx.edgeVariable().variable().
                        getText(), key));
                    queryPropertyPredicate.setLiteral(relationPropertyFilters.get(key));
                    queryPropertyPredicate.setComparisonOperator(ComparisonOperator.EQUALS);
                    queryPropertyPredicate.setPredicateType(PredicateType.VARIABLE_AND_LITERAL);
                    structuredQuery.addQueryPropertyPredicate(queryPropertyPredicate);
                }
            }
        }
        structuredQuery.addRelation(queryRelation);
    }

    private AbstractStructuredQuery visitVariableVertex(StructuredQuery structuredQuery,
        VariableVertexContext ctx) {
        QueryVariable queryVariable = new QueryVariable(ctx.variable().getText());
        if (null != ctx.type()) {
            queryVariable.setVariableType(ctx.type().variable().getText());
        }
        if (null != ctx.propertyFilters()) {
            Map<String, String> variablePropertyFilters = parsePropertyFilters(ctx.
                propertyFilters());
            QueryPropertyPredicate queryPropertyPredicate;
            for (String key : variablePropertyFilters.keySet()) {
                queryPropertyPredicate = new QueryPropertyPredicate();
                queryPropertyPredicate.setVariable1(new Pair<>(ctx.variable().getText(), key));
                queryPropertyPredicate.setLiteral(variablePropertyFilters.get(key));
                queryPropertyPredicate.setComparisonOperator(ComparisonOperator.EQUALS);
                queryPropertyPredicate.setPredicateType(PredicateType.VARIABLE_AND_LITERAL);
                structuredQuery.addQueryPropertyPredicate(queryPropertyPredicate);
            }
        }
        return queryVariable;
    }

    private Map<String, Pair<String, String>> parseProperties(PropertiesContext ctx) {
        Map<String, Pair<String, String>> properties = new HashMap<>();
        for (int i = 0; i < ctx.property().size(); ++i) {
            String dataType = ctx.property(i).dataType().getText();
            LiteralContext literal = ctx.property(i).literal();
            String value;
            if (null != literal.stringLiteral()) {
                value = literal.stringLiteral().value().getText();
            } else {
                value = literal.getText();
            }
            DataType.assertValueCanBeCastToDataType(dataType, value);
            properties.put(ctx.property(i).key().getText(), new Pair<>(dataType, value));
        }
        return properties;
    }

    private Map<String, String> parsePropertyFilters(PropertyFiltersContext ctx) {
        Map<String, String> properties = new HashMap<>();
        for (int i = 0; i < ctx.propertyFilter().size(); ++i) {
            LiteralContext literal = ctx.propertyFilter(i).literal();
            String value;
            if (null != literal.stringLiteral()) {
                value = literal.stringLiteral().value().getText();
            } else {
                value = literal.getText();
            }
            properties.put(ctx.propertyFilter(i).key().getText(), value);
        }
        return properties;
    }
}

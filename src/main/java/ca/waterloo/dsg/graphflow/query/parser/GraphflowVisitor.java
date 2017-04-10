package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowBaseVisitor;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.*;
import ca.waterloo.dsg.graphflow.query.structuredquery.AbstractStructuredQuery;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryAggregation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryAggregation.AggregationFunction;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery.QueryOperation;
import ca.waterloo.dsg.graphflow.util.DataType;
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
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.MATCH);
        MatchPatternContext matchPatternCtx = ctx.matchPattern();
        for (int i = 0; i < matchPatternCtx.variableEdge().size(); i++) {
            structuredQuery.addRelation((QueryRelation) visit(matchPatternCtx.variableEdge(i)));
        }

        ReturnClauseContext returnClauseCtx = ctx.returnClause();
        if (null != returnClauseCtx) {
            setReturnVariablesAndAggregations(structuredQuery, returnClauseCtx);
        }
        return structuredQuery;
    }
    
    private void setReturnVariablesAndAggregations(StructuredQuery structuredQuery,
        ReturnClauseContext returnClauseCtx) {
        for (VariableContext variableContext : returnClauseCtx.variable()) {
            structuredQuery.addReturnVariable(variableContext.getText());
        }
        for (VariableWithPropertyContext variableWithPropertyCtx :
            returnClauseCtx.variableWithProperty()) {
            structuredQuery.addReturnVariablePropertyPair(new Pair<String, String>(
                variableWithPropertyCtx.variable(0).getText(),
                variableWithPropertyCtx.variable(1).getText()));
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
                    new Pair<String, String>(
                        aggregationCtx.variableWithProperty().variable(0).getText(),
                        aggregationCtx.variableWithProperty().variable(1).getText())));
            }
        }
    }

    @Override
    public AbstractStructuredQuery visitContinuousMatchQuery(ContinuousMatchQueryContext ctx) {
        StructuredQuery structuredQuery = (StructuredQuery) visit(ctx.matchQuery());
        structuredQuery.setQueryOperation(QueryOperation.CONTINUOUS_MATCH);
        structuredQuery.setContinuousMatchAction(ctx.FILE().getText());
        structuredQuery.setFilePath(ctx.filePath().getText());
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
    public AbstractStructuredQuery visitVariableEdge(VariableEdgeContext ctx) {
        QueryRelation queryRelation = new QueryRelation((QueryVariable) visit(ctx.
            variableVertex(0)), (QueryVariable) visit(ctx.variableVertex(1)));
        if (null != ctx.edgeOptionalTypeAndOptionalProperties()) {
            EdgeOptionalTypeAndOptionalPropertiesContext ctxEdge = ctx.
                edgeOptionalTypeAndOptionalProperties();
            if (null != ctxEdge.variable()) {
                queryRelation.setRelationName(ctxEdge.variable().getText());
            }
            if (null != ctxEdge.type()) {
                queryRelation.setRelationType(ctxEdge.type().getText());
            }
            if (null != ctxEdge.properties()) {
                queryRelation.setRelationProperties(parseProperties(ctxEdge.properties()));
            }
        }
        return queryRelation;
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

    @Override
    public AbstractStructuredQuery visitVariableVertex(VariableVertexContext ctx) {
        QueryVariable queryVariable = new QueryVariable(ctx.variable().getText());
        return queryVariable;
    }

    private Map<String, Pair<String, String>> parseProperties(PropertiesContext ctx) {
        Map<String, Pair<String, String>> properties = new HashMap<>();
        for (int i = 0; i < ctx.property().size(); ++i) {
            String dataType = ctx.property(i).dataType().getText();
            String value = ctx.property(i).value().getText();
            DataType.assertValueCanBeCastToDataType(dataType, value);
            properties.put(ctx.property(i).key().getText(), new Pair<>(dataType, value));
        }
        return properties;
    }
}

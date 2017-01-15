package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowBaseVisitor;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.ContinuousMatchQueryContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.CreatePatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DeletePatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsEdgeWithOptionalTypeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsEdgeWithTypeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsVertexContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsVertexWithTypeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.EdgeTypeAndPropertiesContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.GraphflowContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.MatchPatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.PathPatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.PropertiesContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.ShortestPathQueryContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.VariableEdgeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.VariableVertexContext;
import ca.waterloo.dsg.graphflow.query.structuredquery.AbstractStructuredQuery;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryRelation;
import ca.waterloo.dsg.graphflow.query.structuredquery.QueryVariable;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.structuredquery.StructuredQuery.QueryOperation;
import ca.waterloo.dsg.graphflow.util.Type;

import java.util.HashMap;

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
    public AbstractStructuredQuery visitContinuousMatchQuery(ContinuousMatchQueryContext ctx) {
        StructuredQuery structuredQuery = (StructuredQuery) visit(ctx.matchQuery());
        structuredQuery.setQueryOperation(QueryOperation.CONTINUOUS_MATCH);
        structuredQuery.setContinuousMatchAction(ctx.userOperation().getText());
        structuredQuery.setContinuousMatchOutputLocation(ctx.operationLocation().getText());
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
    public AbstractStructuredQuery visitMatchPattern(MatchPatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.MATCH);
        for (int i = 0; i < ctx.variableEdge().size(); i++) {
            structuredQuery.addRelation((QueryRelation) visit(ctx.variableEdge(i)));
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
    public AbstractStructuredQuery visitCreatePattern(CreatePatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.CREATE);
        for (int i = 0; i < ctx.digitsEdgeWithType().size(); i++) {
            structuredQuery.addRelation((QueryRelation) visit(ctx.digitsEdgeWithType(i)));
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
            queryRelation.setRelationType(ctx.edgeType().userDefinedType().variable().getText());
        }
        return queryRelation;
    }

    @Override
    public AbstractStructuredQuery visitDigitsEdgeWithType(DigitsEdgeWithTypeContext ctx) {
        QueryRelation queryRelation = new QueryRelation((QueryVariable) visit(ctx.
            digitsVertexWithType(0)), (QueryVariable) visit(ctx.digitsVertexWithType(1)));
        EdgeTypeAndPropertiesContext ctxEdge = ctx.edgeTypeAndProperties();
        if (null != ctxEdge.userDefinedType()) {
            queryRelation.setRelationType(ctxEdge.userDefinedType().variable().getText());
        }
        if (null != ctxEdge.properties()) {
            queryRelation.setRelationProperties(parseProperties(ctxEdge.properties()));
        }
        return queryRelation;
    }

    @Override
    public AbstractStructuredQuery visitVariableEdge(VariableEdgeContext ctx) {
        QueryRelation queryRelation = new QueryRelation((QueryVariable) visit(ctx.
            variableVertex(0)), (QueryVariable) visit(ctx.variableVertex(1)));
        if (null != ctx.edgeTypeAndProperties()) {
            EdgeTypeAndPropertiesContext ctxEdge = ctx.edgeTypeAndProperties();
            if (null != ctxEdge.userDefinedType()) {
                queryRelation.setRelationType(ctxEdge.userDefinedType().variable().getText());
            }
            if (null != ctxEdge.properties()) {
                queryRelation.setRelationProperties(parseProperties(ctxEdge.properties()));
            }
        }
        return queryRelation;
    }

    @Override
    public AbstractStructuredQuery visitDigitsVertex(DigitsVertexContext ctx) {
        return new QueryVariable(ctx.Digits().getText());
    }

    @Override
    public AbstractStructuredQuery visitDigitsVertexWithType(DigitsVertexWithTypeContext ctx) {
        QueryVariable queryVariable = new QueryVariable(ctx.Digits().getText(), ctx.
            userDefinedType().variable().getText());
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

    private HashMap<String, String[]> parseProperties(PropertiesContext ctx) {
        HashMap<String, String[]> properties = new HashMap<>();
        for (int i = 0; i < ctx.property().size(); ++i) {
            String type = ctx.property(i).systemType().getText();
            String value = ctx.property(i).value().getText();
            Type.assertValueCanBeParsedAsGivenType(type, value);
            properties.put(ctx.property(i).key().getText(), new String[] { type, value });
        }
        return properties;
    }
}

package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowBaseVisitor;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.ContinuousMatchQueryContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.CreatePatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DeletePatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsEdgeWithOptionalTypeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsEdgeWithTypeAndPropertiesContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsVertexContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsVertexWithTypeAndPropertiesContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.EdgeOptionalTypeAndOptionalPropertiesContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.GraphflowContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.MatchPatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.MatchQueryContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.PathPatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.PropertiesContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.ReturnClauseContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.ShortestPathQueryContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.VariableContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.VariableEdgeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.VariableVertexContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.VariableWithPropertyContext;
import ca.waterloo.dsg.graphflow.query.structuredquery.AbstractStructuredQuery;
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
    public AbstractStructuredQuery visitMatchQuery(MatchQueryContext ctx) {
      StructuredQuery structuredQuery = new StructuredQuery();
      structuredQuery.setQueryOperation(QueryOperation.MATCH);
      MatchPatternContext matchPatternContext = ctx.matchPattern();
      for (int i = 0; i < matchPatternContext.variableEdge().size(); i++) {
          structuredQuery.addRelation((QueryRelation) visit(matchPatternContext.variableEdge(i)));
      }
      
      ReturnClauseContext returnClauseContext = ctx.returnClause();
      if (null != returnClauseContext) {
          for (VariableContext variableContext : returnClauseContext.variable()) {
              structuredQuery.addReturnVariable(variableContext.getText());
          }
          for (VariableWithPropertyContext variableWithPropertyContext :
              returnClauseContext.variableWithProperty()) {
              String[] split = variableWithPropertyContext.getText().split("\\.");
              structuredQuery.addReturnVariablePropertyPair(
                  new Pair<String, String>(split[0], split[1]));
        }
      }
      return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitReturnClause(GraphflowParser.ReturnClauseContext ctx) {
      // TODO(semih): Fill
      return null;
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
        for (int i = 0; i < ctx.digitsEdgeWithTypeAndProperties().size(); i++) {
            structuredQuery.addRelation((QueryRelation) visit(ctx.digitsEdgeWithTypeAndProperties(
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

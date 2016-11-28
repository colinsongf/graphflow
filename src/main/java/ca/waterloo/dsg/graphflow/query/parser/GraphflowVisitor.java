package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowBaseVisitor;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.ContinuousMatchQueryContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.CreatePatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DeletePatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsEdgeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsEdgeWithTypeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.DigitsVertexContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.GraphflowContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.MatchPatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.PathPatternContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.ShortestPathQueryContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.VariableEdgeContext;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser.VariableVertexContext;
import ca.waterloo.dsg.graphflow.query.utils.AbstractStructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.QueryEdge;
import ca.waterloo.dsg.graphflow.query.utils.QueryVariable;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery;
import ca.waterloo.dsg.graphflow.query.utils.StructuredQuery.QueryOperation;

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
        structuredQuery.addEdge((QueryEdge) visit(ctx.pathPattern()));
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitMatchPattern(MatchPatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.MATCH);
        for (int i = 0; i < ctx.variableEdge().size(); i++) {
            structuredQuery.addEdge((QueryEdge) visit(ctx.variableEdge(i)));
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitDeletePattern(DeletePatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.DELETE);
        for (int i = 0; i < ctx.digitsEdge().size(); i++) {
            structuredQuery.addEdge((QueryEdge) visit(ctx.digitsEdge(i)));
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitCreatePattern(CreatePatternContext ctx) {
        StructuredQuery structuredQuery = new StructuredQuery();
        structuredQuery.setQueryOperation(QueryOperation.CREATE);
        for (int i = 0; i < ctx.digitsEdgeWithType().size(); i++) {
            structuredQuery.addEdge((QueryEdge) visit(ctx.digitsEdgeWithType(i)));
        }
        return structuredQuery;
    }

    @Override
    public AbstractStructuredQuery visitPathPattern(PathPatternContext ctx) {
        QueryVariable fromVertex = new QueryVariable(ctx.Digits(0).getText());
        QueryVariable toVertex = new QueryVariable(ctx.Digits(1).getText());
        return new QueryEdge(fromVertex, toVertex);
    }

    @Override
    public AbstractStructuredQuery visitDigitsEdge(DigitsEdgeContext ctx) {
        return new QueryEdge((QueryVariable) visit(ctx.digitsVertex(0)),
            (QueryVariable) visit(ctx.digitsVertex(1)));
    }

    @Override
    public AbstractStructuredQuery visitDigitsEdgeWithType(DigitsEdgeWithTypeContext ctx) {
        QueryEdge queryEdge = new QueryEdge((QueryVariable) visit(ctx.digitsVertex(0)),
            (QueryVariable) visit(ctx.digitsVertex(1)));
        if (ctx.edgeType() != null) {
            queryEdge.setEdgeType(ctx.edgeType().type().getText());
        }
        return queryEdge;
    }

    @Override
    public AbstractStructuredQuery visitVariableEdge(VariableEdgeContext ctx) {
        QueryEdge queryEdge = new QueryEdge((QueryVariable) visit(ctx.variableVertex(0)),
            (QueryVariable) visit(ctx.variableVertex(1)));
        if (ctx.edgeType() != null) {
            queryEdge.setEdgeType(ctx.edgeType().type().getText());
        }
        return queryEdge;
    }

    @Override
    public AbstractStructuredQuery visitDigitsVertex(DigitsVertexContext ctx) {
        QueryVariable queryVariable = new QueryVariable(ctx.Digits().getText());
        if (ctx.type() != null) {
            queryVariable.setVariableType(ctx.type().getText());
        }
        return queryVariable;
    }

    @Override
    public AbstractStructuredQuery visitVariableVertex(VariableVertexContext ctx) {
        QueryVariable queryVariable = new QueryVariable(ctx.variable().getText());
        if (ctx.type() != null) {
            queryVariable.setVariableType(ctx.type().getText());
        }
        return queryVariable;
    }
}

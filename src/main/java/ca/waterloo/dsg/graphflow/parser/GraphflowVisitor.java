package ca.waterloo.dsg.graphflow.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowBaseVisitor;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser;

/**
 * This class is used to traverse the parse tree, and encapsulate the query structure within the
 * StructuredQuery object.
 */
public class GraphflowVisitor extends GraphflowBaseVisitor<StructuredQuery> {

    @Override
    public StructuredQuery visitGraphflow(GraphflowParser.GraphflowContext ctx) {
        StructuredQuery operations = visit(ctx.statement());
        return  operations;
    }

    @Override
    public StructuredQuery visitDeletePattern(GraphflowParser.DeletePatternContext ctx) {
        StructuredQuery edges = visit(ctx.variableExpression(0));
        edges.setOperation(StructuredQuery.Operation.DELETE);
        for (int i = 1; i < ctx.variableExpression().size(); i++) {
            StructuredQuery tmpedge = visit(ctx.variableExpression(i));
            edges.addEdge(tmpedge.getEdges().get(0));
        }
        return edges;
    }

    @Override
    public StructuredQuery visitMatchPattern(GraphflowParser.MatchPatternContext ctx) {
        StructuredQuery edges = visit(ctx.variableExpression(0));
        edges.setOperation(StructuredQuery.Operation.MATCH);
        for (int i = 1; i < ctx.variableExpression().size(); i++) {
            StructuredQuery tmpedge = visit(ctx.variableExpression(i));
            edges.addEdge(tmpedge.getEdges().get(0));
        }
        return edges;
    }

    @Override
    public StructuredQuery visitCreatePattern(GraphflowParser.CreatePatternContext ctx) {
        StructuredQuery edges = visit(ctx.digitsExpression(0));
        edges.setOperation(StructuredQuery.Operation.CREATE);
        for (int i = 1; i < ctx.digitsExpression().size(); i++) {
            StructuredQuery tmpedge = visit(ctx.digitsExpression(i));
            edges.addEdge(tmpedge.getEdges().get(0));
        }
        return edges;
    }

    @Override
    public StructuredQuery visitVariableExpression(GraphflowParser.VariableExpressionContext ctx) {
        StructuredQuery edge = new StructuredQuery();
        String[] vertex = {"", ""};
        if(ctx.leftVariable() != null) {
            vertex[0] = ctx.leftVariable().getText();
        }
        if(ctx.rightVariable() != null) {
            vertex[1] = ctx.rightVariable().getText();
        }
        edge.addEdge(vertex);
        return edge;
    }

    @Override
    public StructuredQuery visitDigitsExpression(GraphflowParser.DigitsExpressionContext ctx) {
        StructuredQuery edge = new StructuredQuery();
        String[] vertex = {"", ""};
        vertex[0] = ctx.leftDigit().getText();
        if(ctx.rightDigit() != null) {
            vertex[1] = ctx.rightDigit().getText();
        }
        edge.addEdge(vertex);
        return edge;
    }
}

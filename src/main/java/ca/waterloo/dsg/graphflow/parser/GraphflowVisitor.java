package ca.waterloo.dsg.graphflow.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowBaseVisitor;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser;

/**
 * This class is used to traverse the parse tree, and encapsulate the query structure within the
 * InflightData object.
 */
public class GraphflowVisitor extends GraphflowBaseVisitor<InflightData> {

    @Override
    public InflightData visitGraphflow(GraphflowParser.GraphflowContext ctx) {
        InflightData operations = new InflightData();
        for(int i = 0; i < ctx.statement().size(); i++) {
            operations.addToAllOperations(visit(ctx.statement(i)));
        }
        return  operations;
    }

    @Override
    public InflightData visitDeletePattern(GraphflowParser.DeletePatternContext ctx) {
        InflightData edges = visit(ctx.variableExpression(0));
        edges.setOperation(InflightData.Operation.DELETE);
        for (int i = 1; i < ctx.variableExpression().size(); i++) {
            InflightData tmpedge = visit(ctx.variableExpression(i));
            edges.addVertex(tmpedge.getVertices().get(0));
        }
        return edges;
    }

    @Override
    public InflightData visitMatchPattern(GraphflowParser.MatchPatternContext ctx) {
        InflightData edges = visit(ctx.variableExpression(0));
        edges.setOperation(InflightData.Operation.MATCH);
        for (int i = 1; i < ctx.variableExpression().size(); i++) {
            InflightData tmpedge = visit(ctx.variableExpression(i));
            edges.addVertex(tmpedge.getVertices().get(0));
        }
        return edges;
    }

    @Override
    public InflightData visitCreatePattern(GraphflowParser.CreatePatternContext ctx) {
        InflightData edges = visit(ctx.digitsExpression(0));
        edges.setOperation(InflightData.Operation.CREATE);
        for (int i = 1; i < ctx.digitsExpression().size(); i++) {
            InflightData tmpedge = visit(ctx.digitsExpression(i));
            edges.addVertex(tmpedge.getVertices().get(0));
        }
        return edges;
    }

    @Override
    public InflightData visitVariableExpression(GraphflowParser.VariableExpressionContext ctx) {
        InflightData edge = new InflightData();
        String[] vertex = {"", ""};
        if(ctx.leftVariable() != null) {
            vertex[0] = ctx.leftVariable().getText();
        }
        if(ctx.rightVariable() != null) {
            vertex[1] = ctx.rightVariable().getText();
        }
        edge.addVertex(vertex);
        return edge;
    }

    @Override
    public InflightData visitDigitsExpression(GraphflowParser.DigitsExpressionContext ctx) {
        InflightData edge = new InflightData();
        String[] vertex = {"", ""};
        vertex[0] = ctx.leftDigit().getText();
        if(ctx.rightDigit() != null) {
            vertex[1] = ctx.rightDigit().getText();
        }
        edge.addVertex(vertex);
        return edge;
    }
}

package ca.waterloo.dsg.graphflow.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowBaseVisitor;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser;

/**
 * This class is used to traverse the parse tree, and encapsulate the query structure within the
 * InflightData object.
 */
public class GraphflowVisitor extends GraphflowBaseVisitor<InflightData> {

    @Override
    public InflightData visitCypher(GraphflowParser.CypherContext ctx) {
        InflightData operations = new InflightData();
        for(int i = 0; i < ctx.statement().size(); i++) {
            operations.addToAllOperations(visit(ctx.statement(i)));
        }
        return  operations;
    }

    @Override
    public InflightData visitDeletepattern(GraphflowParser.DeletepatternContext ctx) {
        InflightData edges = visit(ctx.variableexpression(0));
        edges.setOperation(InflightData.Operation.DELETE);
        for (int i = 1; i < ctx.variableexpression().size(); i++) {
            InflightData tmpedge = visit(ctx.variableexpression(i));
            edges.addVertex(tmpedge.getVertices().get(0));
        }
        return edges;
    }

    @Override
    public InflightData visitMatchpattern(GraphflowParser.MatchpatternContext ctx) {
        InflightData edges = visit(ctx.variableexpression(0));
        edges.setOperation(InflightData.Operation.MATCH);
        for (int i = 1; i < ctx.variableexpression().size(); i++) {
            InflightData tmpedge = visit(ctx.variableexpression(i));
            edges.addVertex(tmpedge.getVertices().get(0));
        }
        return edges;
    }

    @Override
    public InflightData visitCreatepattern(GraphflowParser.CreatepatternContext ctx) {
        InflightData edges = visit(ctx.digitsexpression(0));
        edges.setOperation(InflightData.Operation.CREATE);
        for (int i = 1; i < ctx.digitsexpression().size(); i++) {
            InflightData tmpedge = visit(ctx.digitsexpression(i));
            edges.addVertex(tmpedge.getVertices().get(0));
        }
        return edges;
    }

    @Override
    public InflightData visitVariableexpression(GraphflowParser.VariableexpressionContext ctx) {
        InflightData edge = new InflightData();
        String[] vertex = {"", ""};
        if(ctx.leftvariable() != null) {
            vertex[0] = ctx.leftvariable().getText();
        }
        if(ctx.rightvariable() != null) {
            vertex[1] = ctx.rightvariable().getText();
        }
        edge.addVertex(vertex);
        return edge;
    }

    @Override
    public InflightData visitDigitsexpression(GraphflowParser.DigitsexpressionContext ctx) {
        InflightData edge = new InflightData();
        String[] vertex = {"", ""};
        vertex[0] = ctx.leftdigit().getText();
        if(ctx.rightdigit() != null) {
            vertex[1] = ctx.rightdigit().getText();
        }
        edge.addVertex(vertex);
        return edge;
    }
}

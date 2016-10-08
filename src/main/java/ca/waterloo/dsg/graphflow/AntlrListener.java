package ca.waterloo.dsg.graphflow;

import ca.waterloo.dsg.graphflow.grammar.*;
import ca.waterloo.dsg.graphflow.graphmodel.Graph;
import org.antlr.v4.runtime.tree.ParseTree;

public class AntlrListener extends GraphflowBaseListener {

    Graph g;

    public AntlrListener(Graph g) {
        this.g = g;
    }

    private void printTree(ParseTree es) {
        System.out.println(es.getClass().getName());
        System.out.println(es.toString());
        System.out.println("***");
        if(es.getChildCount() > 0) {
            for (int i = 0; i < es.getChildCount(); i++) {
                printTree(es.getChild(i));
            }
        }
    }

    public void enterCypherQuery(GraphflowParser.CypherQueryContext ctx) {
        /*System.out.println( "Found cypher query : " + ctx.getText() );
        for (ParseTree e: ctx.children) {
            printTree(e);
        }*/
    }

    @Override
    public void exitMatchquery(GraphflowParser.MatchqueryContext ctx) {
        //Vertex fromVertex = new Vertex(ctx.vertex(0).variable().getText());
        //Vertex toVertex = new Vertex(ctx.vertex(1).variable().getText());
        String label = ctx.edge().getText();
        //Edge e = new Edge(fromVertex, toVertex, label);
        //System.out.println(label);
        for (GraphflowParser.VertexContext v: ctx.vertex()) {
            System.out.println(v.getText());
        }
        System.out.println(ctx.edge().getText());
        //g.searchEdges(label);
    }
}

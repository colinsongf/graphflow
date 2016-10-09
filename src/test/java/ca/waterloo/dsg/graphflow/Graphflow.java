package ca.waterloo.dsg.graphflow;

//
//import ca.waterloo.dsg.graphflow.grammar.*;
//import ca.waterloo.dsg.graphflow.graphmodel.Graph;
//import org.antlr.v4.runtime.*;
//import org.antlr.v4.runtime.tree.*;
//
public class Graphflow {
//    public static void main( String[] args) throws Exception {
//        GraphflowLexer lexer = new GraphflowLexer( new ANTLRFileStream(args[0]));
//        lexer.removeErrorListeners();
//        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
//
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//
//        GraphflowParser parser = new GraphflowParser(tokens);
//        parser.removeErrorListeners();
//        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
//
//        Graph g = new Graph();
//
////        g.addEdge(new Edge(new Vertex("A"),new Vertex("B"),"follows"));
////        g.addEdge(new Edge(new Vertex("A"),new Vertex("C"),"fdsf"));
////        g.addEdge(new Edge(new Vertex("C"),new Vertex("D"),"follows"));
////        g.addEdge(new Edge(new Vertex("B"),new Vertex("D"),"fdsf"));
////        g.addEdge(new Edge(new Vertex("B"),new Vertex("E"),"follows"));
//
//        System.out.println("The graph:");
//        Graph.printGraph(g);
//
//        ParseTree tree = parser.cypherQuery();
//        ParseTreeWalker walker = new ParseTreeWalker();
//        walker.walk( new AntlrListener(g), tree);
//
//    }
}

package ca.waterloo.dsg.graphflow.parser;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import ca.waterloo.dsg.graphflow.grammar.GraphflowLexer;

/**
 * This is a test class for the cypher parser.
 */
public class GraphflowParser {
    public static void main( String[] args) throws Exception {
        GraphflowLexer lexer = new GraphflowLexer(new ANTLRFileStream(
            GraphflowParser.class.getResource("/graphflow_sample_input.txt").getPath()));
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ca.waterloo.dsg.graphflow.grammar.GraphflowParser parser =
            new ca.waterloo.dsg.graphflow.grammar.GraphflowParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        ParseTree tree = parser.cypher();

        GraphflowVisitor visitor = new GraphflowVisitor();
        InflightData operations = visitor.visit(tree);

        for (InflightData operation : operations.getAllOperations()) {
            System.out.println("\nOperation: " + operation.getOperation());
            System.out.println("Vertices:");
            for (String[] vertex : operation.getVertices()) {
                System.out.println("From: '" + vertex[0] + "', To: '" + vertex[1] + "'");
            }
        }
    }
}

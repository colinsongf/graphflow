package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowLexer;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Converts raw query strings into StructuredQuery objects.
 */
public class StructuredQueryParser {

    public StructuredQuery parse(String query) {
        StructuredQuery structuredQuery = new StructuredQuery();

        GraphflowLexer lexer = new GraphflowLexer(new ANTLRInputStream(query));
        lexer.removeErrorListeners();
        lexer.addErrorListener(ErrorListener.INSTANCE);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        ca.waterloo.dsg.graphflow.grammar.GraphflowParser parser =
            new ca.waterloo.dsg.graphflow.grammar.GraphflowParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ErrorListener.INSTANCE);

        try {
            ParseTree tree = parser.graphflow();
            GraphflowVisitor visitor = new GraphflowVisitor();
            structuredQuery = visitor.visit(tree);
        } catch (Exception e) {
            structuredQuery.setOperation(StructuredQuery.Operation.ERROR);
            structuredQuery.setErrorMessage(e.getMessage());
        }
        return structuredQuery;
    }
}

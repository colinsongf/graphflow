package ca.waterloo.dsg.graphflow.query.parser;

import ca.waterloo.dsg.graphflow.grammar.GraphflowLexer;
import ca.waterloo.dsg.graphflow.grammar.GraphflowParser;
import ca.waterloo.dsg.graphflow.query.StructuredQuery;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Converts a raw query string into a {@code StructuredQuery} object.
 */
public class StructuredQueryParser {

    public StructuredQuery parse(String query) throws ParseCancellationException {
        StructuredQuery structuredQuery = new StructuredQuery();

        GraphflowLexer lexer = new GraphflowLexer(new ANTLRInputStream(query));
        lexer.removeErrorListeners();   // remove default listeners first
        lexer.addErrorListener(ErrorListener.INSTANCE);

        GraphflowParser parser = new GraphflowParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();   // remove default listeners first
        parser.addErrorListener(ErrorListener.INSTANCE);

        try {
            ParseTree tree = parser.graphflow();
            GraphflowVisitor visitor = new GraphflowVisitor();
            structuredQuery = visitor.visit(tree);
        } catch (Exception e) {
            throw new ParseCancellationException(e.getMessage());
        }
        return structuredQuery;
    }
}

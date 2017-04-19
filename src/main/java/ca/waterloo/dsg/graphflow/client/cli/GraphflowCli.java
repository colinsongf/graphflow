package ca.waterloo.dsg.graphflow.client.cli;

import ca.waterloo.dsg.graphflow.client.GraphflowClient;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.TerminalBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Client side CLI implementation, which communicates with the {@code GraphflowServer} using gRPC.
 */
public class GraphflowCli extends GraphflowClient {

    private static final String PRIMARY_PROMPT = "graphflow> ";
    private static final String SECONDARY_PROMPT = "... ";

    private LineReader lineReader;

    /**
     * Construct a client connecting to a server at {@code host:port}.
     */
    public GraphflowCli(String host, int port) throws IOException {
        super(host, port);
        lineReader = LineReaderBuilder.builder().terminal(TerminalBuilder.builder().build()).
            build();
    }

    /**
     * Start the CLI interface.
     */
    public void startCLI() throws FileNotFoundException {
        String prompt = PRIMARY_PROMPT;
        String singleLineQuery;
        String fullQuery = "";
        while (true) {
            try {
                singleLineQuery = lineReader.readLine(prompt);
            } catch (UserInterruptException e) {
                prompt = PRIMARY_PROMPT; // Reset prompt.
                continue;
            } catch (EndOfFileException e) {
                break;
            }
            if (singleLineQuery.isEmpty()) {
                continue;
            }
            fullQuery += singleLineQuery;
            if (!singleLineQuery.contains(";")) {
                // The line does not contain semicolon. Change prompt and wait for more input
                // before processing it.
                prompt = SECONDARY_PROMPT;
                continue;
            }
            prompt = PRIMARY_PROMPT; // Reset prompt.
            if (fullQuery.contains("exit") || fullQuery.contains("quit")) {
                break;
            }
            String result = queryServer(fullQuery);
            System.out.println("\nResult:\n" + result);
            fullQuery = "";
        }
        System.out.println("May the flow be with you!");
    }

    /**
     * Safely terminate the connection to the server.
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}

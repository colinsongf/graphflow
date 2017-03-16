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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client side CLI implementation, which communicates with the {@code GraphflowServer} using gRPC.
 */
public class GraphflowCli extends GraphflowClient {

    private static final String PRIMARY_PROMPT = "graphflow> ";
    private static final String SECONDARY_PROMPT = "... ";
    private static final char START_OF_COMMENT_LINE = '#';

    private LineReader lineReader;

    /**
     * Constructs a client that connects to the Graphflow server at {@code host:port}.
     *
     * @param host The gRPC server hostname.
     * @param port The gRPC server port.
     */
    public GraphflowCli(String host, int port) throws IOException {
        super(host, port);
        Logger.getLogger("org.jline").setLevel(Level.OFF);
        lineReader = LineReaderBuilder.builder().terminal(TerminalBuilder.builder().build()).
            build();
    }

    /**
     * Starts the CLI interface.
     */
    public void startCLI() throws FileNotFoundException {
        String prompt = PRIMARY_PROMPT;
        String singleLineQuery;
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            try {
                singleLineQuery = lineReader.readLine(prompt);
            } catch (UserInterruptException e) {
                prompt = PRIMARY_PROMPT;
                continue;
            } catch (EndOfFileException e) {
                break;
            }
            if (null == singleLineQuery) {  // EOF.
                break;
            }
            singleLineQuery = singleLineQuery.trim();
            if (singleLineQuery.isEmpty() || START_OF_COMMENT_LINE == singleLineQuery.charAt(0)) {
                continue;
            }
            stringBuilder.append(" ").append(singleLineQuery);
            if (!singleLineQuery.contains(";")) {
                prompt = SECONDARY_PROMPT;
                continue;
            }
            prompt = PRIMARY_PROMPT;
            String fullQuery = stringBuilder.toString();
            stringBuilder.setLength(0);
            if (fullQuery.contains("exit") || fullQuery.contains("quit")) {
                break;
            }
            String result = queryServer(fullQuery);
            System.out.println("\nResult:\n" + result);
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

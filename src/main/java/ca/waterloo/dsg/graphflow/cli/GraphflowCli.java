package ca.waterloo.dsg.graphflow.cli;

import ca.waterloo.dsg.graphflow.server.GraphflowServerQueryGrpc;
import ca.waterloo.dsg.graphflow.server.ServerQueryResult;
import ca.waterloo.dsg.graphflow.server.ServerQueryString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
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
public class GraphflowCli {

    private static final String PRIMARY_PROMPT = "graphflow> ";
    private static final String SECONDARY_PROMPT = "... ";
    /**
     * Stores the gRPC channel to the server.
     */
    private final ManagedChannel channel;

    /**
     * Used to perform blocking gRPC calls.
     */
    private final GraphflowServerQueryGrpc.GraphflowServerQueryBlockingStub blockingStub;
    private LineReader lineReader;

    /**
     * Construct a client connecting to a server at {@code host:port}.
     */
    public GraphflowCli(String host, int port) throws IOException {
        // Turn off logs to suppress debug messages from netty.
        Logger.getLogger("io.grpc.internal").setLevel(Level.OFF);
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        blockingStub = GraphflowServerQueryGrpc.newBlockingStub(channel);
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
                prompt = "... ";
                continue;
            }
            prompt = PRIMARY_PROMPT; // Reset prompt.
            if (fullQuery.contains("exit") || fullQuery.contains("quit")) {
                break;
            }
            System.out.println("Your query: " + fullQuery);
            String result = queryServer(fullQuery);
            System.out.println("Result:\n" + result);
            fullQuery = "";
        }
        System.out.println("May the flow be with you!");
    }

    /**
     * Send the input {@code query} string to the server.
     */
    private String queryServer(String query) {
        ServerQueryString request = ServerQueryString.newBuilder().setMessage(query).build();
        ServerQueryResult result;
        try {
            result = blockingStub.executeQuery(request);
        } catch (StatusRuntimeException e) {
            return "ERROR: " + e.getMessage();
        }
        return result.getMessage();
    }

    /**
     * Safely terminate the connection to the server.
     */
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
}

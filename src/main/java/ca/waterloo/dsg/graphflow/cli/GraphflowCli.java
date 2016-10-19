package ca.waterloo.dsg.graphflow.cli;

import ca.waterloo.dsg.graphflow.server.GraphflowQueryGrpc;
import ca.waterloo.dsg.graphflow.server.QueryResult;
import ca.waterloo.dsg.graphflow.server.QueryString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client side command line interface implementation.
 * It uses grpc to communicate with the server.
 */
public class GraphflowCli {

    private final ManagedChannel channel;
    private final GraphflowQueryGrpc.GraphflowQueryBlockingStub blockingStub;

    // Construct client connecting to server at {@code host:port}.
    public GraphflowCli(String host, int port) {
        // Turn off logs to supress debug messages from netty.
        Logger.getLogger("io.grpc.internal").setLevel(Level.OFF);
        channel = ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext(true)
            .build();
        blockingStub = GraphflowQueryGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) throws Exception {
        GraphflowCli cli = new GraphflowCli("localhost", 8080);
        try {
            cli.startCLI(args);
        } finally {
            cli.shutdown();
        }
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void startCLI(String[] args) throws FileNotFoundException {
        Scanner cliInput;
        if (args.length > 0) {
            File file = new File(args[0]);
            cliInput = new Scanner(file);
        } else {
            cliInput = new Scanner(System.in);
        }
        while (true) {
            System.out.print("graphflow> ");
            if (!cliInput.hasNext()) {
                break;
            }
            String query = cliInput.nextLine().trim();
            if (query.isEmpty()) {
                continue;
            }
            if (query.charAt(query.length() - 1) != ';') {
                System.out.println("ERROR: Needs a semicolon at the end.");
                continue;
            }
            System.out.println("Your query: " + query);
            String result = this.queryServer(query);
            System.out.println("Result:\n" + result);
            if (query == "exit;") {
                break;
            }
        }
        System.out.println("May the flow be with you!");
    }

    public String queryServer(String query) {
        QueryString request = QueryString.newBuilder().setMessage(query).build();
        QueryResult result;
        try {
            result = blockingStub.executeQuery(request);
        } catch (StatusRuntimeException e) {
            return "ERROR: " + e.getMessage();
        }
        return result.getMessage();
    }
}

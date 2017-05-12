package ca.waterloo.dsg.graphflow.client;

import ca.waterloo.dsg.graphflow.server.GraphflowServerQueryGrpc;
import ca.waterloo.dsg.graphflow.server.GraphflowServerQueryGrpc.GraphflowServerQueryBlockingStub;
import ca.waterloo.dsg.graphflow.server.ServerQueryResult;
import ca.waterloo.dsg.graphflow.server.ServerQueryString;
import ca.waterloo.dsg.graphflow.server.ServerQueryString.ReturnType;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client class which communicates with the {@code GraphflowServer} using gRPC.
 */
public class GraphflowClient {

    /**
     * Stores the gRPC channel to the server.
     */
    protected final ManagedChannel channel;

    /**
     * Used to perform blocking gRPC calls.
     */
    private final GraphflowServerQueryBlockingStub blockingStub;

    /**
     * Constructs a client of connecting at {@code host:port} to {@code GraphflowServer}.
     */
    public GraphflowClient(String host, int port) {
        // Turn off logs to suppress debug messages from netty.
        Logger.getLogger("io.grpc.internal").setLevel(Level.OFF);
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build();
        blockingStub = GraphflowServerQueryGrpc.newBlockingStub(channel);
    }

    /**
     * Send the input {@code query} string to Graphflow server.
     */
    protected String queryServer(String query) {
        ServerQueryString request;
        switch (this.getClass().getSimpleName()) {
            case "GraphflowCli":
                request = ServerQueryString.newBuilder().setMessage(query)
                    .setReturnType(ReturnType.TEXT).build();
                break;
            case "PlanViewerHttpServer":
                request = ServerQueryString.newBuilder().setMessage(query)
                    .setReturnType(ReturnType.JSON).build();
                break;
            default:
                return "ERROR: Unrecognized Graphflow client";
        }
        ServerQueryResult result;
        try {
            result = blockingStub.executeQuery(request);
        } catch (StatusRuntimeException e) {
            return "ERROR: " + e.getMessage();
        }
        return result.getMessage();
    }
}

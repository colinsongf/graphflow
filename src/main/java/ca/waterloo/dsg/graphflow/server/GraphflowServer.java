package ca.waterloo.dsg.graphflow.server;

import ca.waterloo.dsg.graphflow.client.httpserver.PlanViewerHttpServer;
import ca.waterloo.dsg.graphflow.query.QueryProcessor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * gRPC server to handle incoming queries and pass them on to the {@code QueryProcessor}.
 */
public class GraphflowServer {

    private static final Logger logger = LogManager.getLogger(GraphflowServer.class);

    private static String GRPC_HOST = "localhost";
    private static int GRPC_PORT = 8080;
    private Server grpcServer;

    public void start() throws IOException {
        System.err.println("*** starting gRPC server...");
        grpcServer = ServerBuilder.forPort(GRPC_PORT).addService(new GraphflowQueryImpl()).build().
            start();
        System.err.println("*** gRPC server running.");

        System.err.println("*** starting http server...");
        final PlanViewerHttpServer planViewerHttpServer = new PlanViewerHttpServer(GRPC_HOST,
            GRPC_PORT);
        planViewerHttpServer.start();
        System.err.println("*** http server running.");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC grpcServer...");
                GraphflowServer.this.stop();
                System.err.println("*** grpcServer shut down.");
            }
        });
    }

    private void stop() {
        grpcServer.shutdown();
    }

    /**
     * Await termination on the main thread since the gRPC library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        grpcServer.awaitTermination();
    }

    /**
     * Getter for {@code GRPC_HOST}.
     */
    public static String getGrpcHost() {
        return GRPC_HOST;
    }

    /**
     * Getter for {@code GRPC_PORT}.
     */
    public static int getGrpcPort() {
        return GRPC_PORT;
    }

    private class GraphflowQueryImpl extends GraphflowServerQueryGrpc.GraphflowServerQueryImplBase {

        private QueryProcessor processor = new QueryProcessor();

        @Override
        public void executeQuery(ServerQueryString request, StreamObserver<ServerQueryResult>
            responseObserver) {
            String result;
            try {
                result = processor.process(request);
            } catch (Exception e) {
                logger.error("Unknown error when executing the query '" + request.getMessage() +
                    "'. Exception stack trace:", e);
                result = "ERROR: " + e.getMessage();
            }
            ServerQueryResult queryResult = ServerQueryResult.newBuilder().setMessage(result).
                build();
            responseObserver.onNext(queryResult);   // get next {@code queryResult} from the stream.
            responseObserver.onCompleted();         // mark the stream as done.
        }
    }
}

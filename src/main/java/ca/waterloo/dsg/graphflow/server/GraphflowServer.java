package ca.waterloo.dsg.graphflow.server;

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

    private static int PORT = 8080;
    private Server grpcServer;

    public void start() throws IOException {
        System.err.println("*** starting gRPC server...");
        grpcServer = ServerBuilder.forPort(PORT).addService(new GraphflowQueryImpl()).build().
            start();
        System.err.println("*** gRPC server running.");
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

    private class GraphflowQueryImpl extends GraphflowServerQueryGrpc.GraphflowServerQueryImplBase {

        private QueryProcessor processor = new QueryProcessor();

        @Override
        public void executeQuery(ServerQueryString request, StreamObserver<ServerQueryResult>
            responseObserver) {
            String result;
            try {
                result = processor.process(request.getMessage());
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

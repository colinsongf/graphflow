package ca.waterloo.dsg.graphflow.server;

import ca.waterloo.dsg.graphflow.query.planner.QueryProcessor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GraphflowServer {

    private static int PORT = 8080;
    private Server grpcServer;

    /**
     * Main launches the {@code grpcServer} from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final GraphflowServer graphflowServer = new GraphflowServer();
        graphflowServer.start();
        graphflowServer.blockUntilShutdown();
    }

    private void start() throws IOException {
        grpcServer = ServerBuilder
            .forPort(PORT)
            .addService(new GraphflowQueryImpl())
            .build()
            .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC grpcServer since JVM is shutting down");
                GraphflowServer.this.stop();
                System.err.println("*** grpcServer shut down");
            }
        });
    }

    private void stop() {
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the gRPC library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.awaitTermination();
        }
    }

    private class GraphflowQueryImpl extends GraphflowServerQueryGrpc.GraphflowServerQueryImplBase {

        private QueryProcessor processor = new QueryProcessor();

        @Override
        public void executeQuery(ServerQueryString request,
                                 StreamObserver<ServerQueryResult> responseObserver) {
            String result = processor.process(request.getMessage());
            ServerQueryResult queryResult = ServerQueryResult
                .newBuilder()
                .setMessage(result)
                .build();
            responseObserver.onNext(queryResult);   // get next {@code queryResult} from the stream.
            responseObserver.onCompleted();         // mark the stream as done.
        }
    }
}

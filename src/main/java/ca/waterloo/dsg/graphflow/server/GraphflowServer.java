package ca.waterloo.dsg.graphflow.server;

import ca.waterloo.dsg.graphflow.queryplanner.QueryProcessor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GraphflowServer {

    private int port = 8080;
    private Server server;

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final GraphflowServer server = new GraphflowServer();
        server.start();
        server.blockUntilShutdown();
    }

    private void start() throws IOException {
        server = ServerBuilder.forPort(port)
            .addService(new GraphflowQueryImpl())
            .build()
            .start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                GraphflowServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private class GraphflowQueryImpl extends GraphflowQueryGrpc.GraphflowQueryImplBase {

        QueryProcessor processor = new QueryProcessor();

        @Override
        public void executeQuery(QueryString request, StreamObserver<QueryResult> responseObserver) {
            String result = processor.process(request.getMessage());
            QueryResult queryResult = QueryResult.newBuilder().setMessage(result).build();
            responseObserver.onNext(queryResult);
            responseObserver.onCompleted();
        }
    }
}

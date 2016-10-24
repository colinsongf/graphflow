package ca.waterloo.dsg.graphflow.server;

import java.io.IOException;

/**
 * Instantiates {@code GraphflowServer}.
 */
public class GraphflowServerRunner {

    public static void main(String[] args) throws IOException, InterruptedException {
        final GraphflowServer graphflowServer = new GraphflowServer();
        graphflowServer.start();
        graphflowServer.blockUntilShutdown();
    }
}

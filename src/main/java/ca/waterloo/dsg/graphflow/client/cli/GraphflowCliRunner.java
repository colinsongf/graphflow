package ca.waterloo.dsg.graphflow.client.cli;

import ca.waterloo.dsg.graphflow.server.GraphflowServer;

import java.io.IOException;

/**
 * Instantiates {@code GraphflowCli}.
 */
public class GraphflowCliRunner {

    public static void main(String[] args) throws InterruptedException, IOException {
        GraphflowCli cli = new GraphflowCli(GraphflowServer.getGrpcHost(),
            GraphflowServer.getGrpcPort());
        try {
            cli.startCLI();
        } finally {
            cli.shutdown();
        }
    }
}

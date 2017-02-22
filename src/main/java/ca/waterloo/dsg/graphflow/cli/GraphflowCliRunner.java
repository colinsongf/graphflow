package ca.waterloo.dsg.graphflow.cli;

import java.io.IOException;

/**
 * Instantiates {@code GraphflowCli}.
 */
public class GraphflowCliRunner {

    public static void main(String[] args) throws InterruptedException, IOException {
        GraphflowCli cli = new GraphflowCli("localhost", 8080);
        try {
            cli.startCLI();
        } finally {
            cli.shutdown();
        }
    }
}

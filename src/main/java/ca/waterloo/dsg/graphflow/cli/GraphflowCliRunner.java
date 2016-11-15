package ca.waterloo.dsg.graphflow.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Instantiates {@code GraphflowCli}.
 */
public class GraphflowCliRunner {

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {
        GraphflowCli cli = new GraphflowCli("localhost", 8080);
        try {
            Scanner cliInput;
            if (args.length > 0) {
                // Read queries from a file if provided in the {@code args}, for automated testing.
                File file = new File(args[0]);
                cliInput = new Scanner(file);
            } else {
                // Read from stdin.
                cliInput = new Scanner(System.in);
            }
            cli.startCLI(cliInput);
        } finally {
            cli.shutdown();
        }
    }
}

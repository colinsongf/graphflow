package ca.waterloo.dsg.graphflow.client.httpserver;

import ca.waterloo.dsg.graphflow.client.GraphflowClient;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Http server to handle queries from PlanViewer web UI, which communicates with the
 * {@code GraphflowServer} using gRPC.
 */
public class PlanViewerHttpServer extends GraphflowClient implements Runnable {

    private static final Logger logger = LogManager.getLogger(PlanViewerHttpServer.class);
    private final String PLAN_VIEWER_HTML_PATH = "src/main/planviewer/plan_viewer.html";
    private String query;
    private Thread thread;

    /**
     * Constructs a client of {@code GraphflowServer} connecting at {@code host:port}.
     */
    public PlanViewerHttpServer(String host, int port) {
        super(host, port);
    }

    /**
     * Starts the http server for PlanViewer web UI.
     */
    @Override
    public void run() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            // Create a route for input query
            server.createContext("/query", new PlanViewerHttpInputHandler());
            // Create a route for output {@code QueryPlan} in JSON format
            server.createContext("/json", new PlanViewerHttpOutputHandler());
            // Create a default executor
            server.setExecutor(null);
            server.start();
            File webViewer = new File(PLAN_VIEWER_HTML_PATH);
            logger.info("Please open the PlanViewer (link below) in a browser:");
            logger.info("file://" + webViewer.getAbsolutePath());
        } catch (IOException exception) {
            logger.error("PlanViewerHttpServer: failed to start");
        }
    }

    /**
     * Starts a new thread for PlanViewer web UI.
     */
    public void start() {
        if (null == thread) {
            thread = new Thread(this);
            thread.start();
        }
    }

    /**
     * Handler class to receive input query from PlanViewer web UI
     */
    public class PlanViewerHttpInputHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream inputStream = exchange.getRequestBody();
            String query = null;
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                query = scanner.useDelimiter("\\A").next();
            }
            logger.info("Query: " + query);
            inputStream.close();
            PlanViewerHttpServer.this.query = query;
        }
    }

    /**
     * Handler class to process query and output the result to PlanViewer web UI
     */
    public class PlanViewerHttpOutputHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = queryServer(query);
            if (query.toUpperCase().startsWith("EXPLAIN")) {
                logger.info("Result: please see the Plan Viewer for the result.");
            } else {
                logger.info("Result: " + response);
            }

            // Add the header to avoid error:
            // “No 'Access-Control-Allow-Origin' header is present on the requested resource”
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }
}

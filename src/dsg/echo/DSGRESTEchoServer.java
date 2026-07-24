package dsg.echo;

import java.io.IOException;

import dsg.http.DSGHTTPServer;
import dsg.rest.DSGRESTSkeleton;

public class DSGRESTEchoServer extends DSGHTTPServer {
    /**
     * Initialize an echo server.
     *
     * @param port    the port to listen for new connections.
     * @param threads the number of threads used to process requests.
     * @throws IOException If an error occurs while opening the server socket.
     */
    public DSGRESTEchoServer(int port, int threads) throws IOException {
        super(port, threads, createHandler());
    }

    private static DSGRESTSkeleton createHandler() {
        DSGRESTSkeleton restHandler = new DSGRESTSkeleton();
        restHandler.exportResource("/", new DSGRESTEchoResource());
        return restHandler;
    }

    /**
     * Start an HTTP server that serves the echo service at its root path.
     *
     * @param args an optional port to listen on.
     */
    public static void main(String[] args) {
        if (args.length >= 2) {
            printUsage();
            System.exit(1);
        }

        // Create server
        DSGRESTEchoServer server;
        try {
            int port = 8000;
            if (args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }
            server = new DSGRESTEchoServer(port, 10);
        } catch (NumberFormatException nfe) {
            System.err.println(args[0] + " is not a valid port");
            printUsage();
            System.exit(1);
            return;
        } catch (IOException ioe) {
            System.err.println("Failed to create server: " + ioe.getMessage());
            ioe.printStackTrace();
            System.exit(1);
            return;
        }

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
            }
        });

        // Process incoming messages
        server.serve();
    }

    private static void printUsage() {
        System.err.println("Usage: java dsg.echo.DSGRESTEchoServer [port]");
    }
}

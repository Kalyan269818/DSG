package dsg.microblog;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import dsg.activitypub.DSGActivityPubFederator;
import dsg.http.DSGHTTPClient;
import dsg.http.DSGHTTPServer;
import dsg.rest.DSGRESTSkeleton;

/**
 * The micro blog server application.
 */
public class DSGMicroBlogServer extends DSGHTTPServer {
    private DSGActivityPubFederator federator;

    /**
     * Initialize the micro blog server.
     *
     * @param port      the port to listen on for incoming HTTP connections.
     * @param handler   the HTTP handler for the micro blog service.
     * @param federator the federator used to send outbound activities.
     * @throws IOException if creating the server socket fails.
     */
    public DSGMicroBlogServer(int port, DSGMicroBlogHandler handler, DSGActivityPubFederator federator)
            throws IOException {
        super(port, 10, handler);
        this.federator = federator;
        federator.start();
    }

    @Override
    public void shutdown() {
        try {
            super.shutdown();
            federator.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the micro blog server.
     *
     * @param args storage-directory, static-files-directory, and port
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }

        // Create server
        DSGMicroBlogServer server;
        try {
            String staticFilesDirectory = "static";
            int port = 8001;
            if (args.length >= 2) {
                staticFilesDirectory = args[1];
            }
            if (args.length >= 3) {
                port = Integer.parseInt(args[2]);
            }

            URI baseURI;
            try {
                String hostname;
                if (args.length == 4) {
                    hostname = args[3];
                } else {
                    hostname = InetAddress.getLocalHost().getCanonicalHostName();
                }
                baseURI = new URI("http", null, hostname, port, "/", null, null);
                System.err.println("[SERVER] Base URI = " + baseURI.toASCIIString());
            } catch (URISyntaxException use) {
                // Should never happen.
                throw new IllegalStateException(use);
            }
            DSGMicroBlogStorage storage = new DSGMicroBlogStorage(args[0]);
            DSGMicroBlogAuthenticator authenticator = new DSGMicroBlogAuthenticator(baseURI, storage);
            DSGActivityPubFederator federator = new DSGActivityPubFederator(baseURI, new DSGHTTPClient());

            DSGRESTSkeleton router = new DSGRESTSkeleton();
            DSGMicroBlogHandler handler = new DSGMicroBlogHandler(staticFilesDirectory, authenticator, router);
            DSGMicroBlogService service = new DSGMicroBlogService(baseURI, storage, authenticator, router, federator);
            router.exportResource("/user", new DSGMicroBlogServiceSkeleton(service, authenticator));

            // Process incoming messages
            server = new DSGMicroBlogServer(port, handler, federator);
            server.serve();
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
    }

    private static void printUsage() {
        System.err.println(
                "Usage: java dsg.microblog.DSGMicroBlogServer <storage-directory> [static-file-directory] [port] [hostname]");
    }

}

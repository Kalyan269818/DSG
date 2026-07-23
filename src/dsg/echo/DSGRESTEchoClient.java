package dsg.echo;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import dsg.http.DSGHTTPClient;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGStandardMediaTypes;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRemoteException;
import dsg.rest.DSGRESTRepresentation;
import dsg.rest.DSGRESTResource;
import dsg.rest.DSGRESTStub;

public class DSGRESTEchoClient {
    private DSGHTTPClient client;
    private DSGRESTResource echoService;

    /**
     * Initializes an echo client.
     *
     * @param serverAddress the URI identifying the the echo service.
     * @param client        an HTTP client that will be used for issuing requests to
     *                      the server.
     */
    public DSGRESTEchoClient(URI serverAddress, DSGHTTPClient client) {
        this.client = client;
        this.echoService = new DSGRESTStub(serverAddress, client);
    }

    /**
     * Send a {@code message} to the echo service.
     *
     * @param message the message to send to the echo service.
     * @return the echo service's response.
     * @throws IOException if an error occurs when interacting with the echo server.
     */
    public String echo(String message) throws IOException {
        DSGRESTContext ctx = new DSGRESTContext();
        ctx.addAcceptedRepresentation(DSGStandardMediaTypes.PLAINTEXT);
        try {
            DSGRESTRepresentation response;
            if (message == null || message.isBlank()) {
                response = echoService.get(ctx, new DSGHTTPHeader());
            } else {
                DSGRESTRepresentation representation = new DSGRESTRepresentation(message);
                response = echoService.post(ctx, representation);
            }
            return response.readAllAsString();
        } catch (DSGRESTException e) {
            switch (e.getStatus()) {
            case NOT_ACCEPTABLE:
                throw new IOException("Echo service does not support requested output format", e);
            case UNSUPPORTED_MEDIA_TYPE:
                throw new IOException("Echo service cannot handle message format", e);
            default:
                throw new IOException("Echo service encountered an unknown error (" + e.getStatus().toString() + ")",
                        e);
            }
        } catch (DSGRESTRemoteException e) {
            throw new IOException("RPC system encountered an unexpected error", e);
        }
    }

    /**
     * Shut down this echo client.
     */
    public void shutdown() {
        client.shutdown();
    }

    /**
     * Sends a request to echo back a message to the server and prints the response.
     *
     * @param args the server address and an optional message.
     */
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            printUsage();
            System.exit(1);
        }

        try {
            URI serverAddress = new URI(args[0]);
            DSGHTTPClient httpClient = new DSGHTTPClient();
            DSGRESTEchoClient client = new DSGRESTEchoClient(serverAddress, httpClient);
            String message = null;
            if (args.length == 2 && !args[1].isBlank()) {
                message = args[1];
            }
            String response = client.echo(message);
            System.out.println(response);
            client.shutdown();
        } catch (URISyntaxException e) {
            System.err.println(args[0] + " is not a valid server address");
            printUsage();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printUsage() {
        System.err.println("Usage: java dsg.echo.DSGRESTEchoClient <server-address> [message]");
    }
}

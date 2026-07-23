package dsg.http;

import java.io.IOException;

import dsg.network.DSGServer;

/**
 * Server running the HTTP protocol.
 */
public class DSGHTTPServer extends DSGServer {

    // ##################
    // # INITIALIZATION #
    // ##################

    /**
     * Initialize an HTTP server to listen on the named {@code port}, with the given
     * number {@code threads}, and using {@code handler} to process incoming
     * requests.
     *
     * @param port    the port to listen for new connections.
     * @param threads the number of threads used to process requests.
     * @param handler the handler that processes incoming HTTP requests.
     * @throws IOException If an error occurs while opening the server socket.
     */
    public DSGHTTPServer(int port, int threads, DSGHTTPHandler handler) throws IOException {
        super(port, threads);
        // TODO Implement constructor
    }

    // ##########
    // # EVENTS #
    // ##########

    // TODO Implement event handling.
}

package dsg.http;

import java.io.IOException;
import dsg.network.DSGClient;

/**
 * Client issuing HTTP requests and retrieving responses from a server.
 */
public class DSGHTTPClient extends DSGClient {
    private boolean followRedirects;
    private boolean keepAlive;

    // ##################
    // # INITIALIZATION #
    // ##################

    /**
     * Create an HTTP client that does neither automatically follow redirects, nor
     * keeps connections alive.
     */
    public DSGHTTPClient() {
        this(false, false);
    }

    /**
     * Create an HTTP client.
     *
     * Using {@code followRedirects} it is possible to control, whether the client
     * automatically follows redirect responses.
     *
     * @param followRedirects whether the client should follow redirects
     *                        automatically.
     * @param keepAlive       whether connections are kept open.
     */
    public DSGHTTPClient(boolean followRedirects, boolean keepAlive) {
        super();
        this.followRedirects = followRedirects;
        this.keepAlive = keepAlive;
    }

    // #################
    // # COMMUNICATION #
    // #################

    /**
     * Send an HTTP {@code request} and return the corresponding response.
     *
     * @param request the HTTP request to send.
     * @return the response for the {@code request}.
     * @throws IOException if an error occurs on the network.
     */
    public DSGHTTPResponse communicate(DSGHTTPRequest request) throws IOException {
        // TODO Implement method
        return null;
    }
}

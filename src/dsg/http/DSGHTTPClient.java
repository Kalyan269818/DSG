package dsg.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

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
        while (true) {
            byte[] bodyBytes = null;
            if (request.getBody() != null) {
                bodyBytes = request.getBody().readAllBytes();
                request.setBody(new ByteArrayInputStream(bodyBytes));
            }

            SocketAddress address = addressFor(request.getTarget());
            request.getHeader().set("Connection", this.keepAlive ? "keep-alive" : "close");

            if (!this.keepAlive || !isConnected(address)) {
                connect(address);
            }
            send(address, request);

            DSGHTTPResponse response = new DSGHTTPResponse();
            receive(address, response);

            if (this.followRedirects && isRedirect(response.getStatus()) && response.getHeader().get("Location") != null) {
                close(address);
                request = buildRedirectRequest(request, response, bodyBytes);
                continue;
            }

            if (!this.keepAlive || isConnectionClose(response)) {
                close(address);
            }
            return response;
        }
    }

    // #############
    // # REDIRECTS #
    // #############

    /**
     * Return the socket address that requests to {@code target} must be sent to.
     */
    private SocketAddress addressFor(URI target) {
        int port = target.getPort();
        if (port == -1) {
            port = 80;
        }
        return new InetSocketAddress(target.getHost(), port);
    }

    /**
     * Return whether {@code status} indicates that the response is a redirect.
     */
    private boolean isRedirect(DSGHTTPStatus status) {
        int code = status.getCode();
        return code >= 300 && code < 400;
    }

    /**
     * Return whether {@code response} indicates that the connection should be
     * closed, i.e. its Connection header field is set to "close".
     */
    private boolean isConnectionClose(DSGHTTPResponse response) {
        String connection = response.getHeader().get("Connection");
        return connection != null && connection.equalsIgnoreCase("close");
    }

    /**
     * Build the follow-up request for a redirect {@code response} to the original
     * {@code request}, reusing {@code bodyBytes} (the original request's already
     * buffered body, or null if it did not have one) if the redirect requires the
     * body to be resent.
     */
    private DSGHTTPRequest buildRedirectRequest(DSGHTTPRequest request, DSGHTTPResponse response, byte[] bodyBytes)
            throws DSGHTTPException {
        String location = response.getHeader().get("Location");
        URI target;
        try {
            target = request.getTarget().resolve(new URI(location));
        } catch (URISyntaxException e) {
            throw new DSGHTTPException("Invalid Location header: " + location);
        }

        boolean downgradeToGet;
        switch (response.getStatus()) {
        case MOVED_PERMANENTLY:
        case FOUND:
            downgradeToGet = (request.getMethod() == DSGHTTPMethod.POST);
            break;
        case SEE_OTHER:
            downgradeToGet = true;
            break;
        default:
            // TEMPORARY_REDIRECT, PERMANENT_REDIRECT, and any other redirect status keep
            // the original method and body intact.
            downgradeToGet = false;
            break;
        }

        if (downgradeToGet) {
            return new DSGHTTPRequest(DSGHTTPMethod.GET, target);
        }

        DSGHTTPHeader header = new DSGHTTPHeader(new HashMap<>(request.getHeader().fields()));
        ByteArrayInputStream body = (bodyBytes == null) ? null : new ByteArrayInputStream(bodyBytes);
        return new DSGHTTPRequest(request.getMethod(), header, target, body);
    }
}

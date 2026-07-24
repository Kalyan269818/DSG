package dsg.http;

import java.io.IOException;
import java.net.SocketAddress;

import dsg.network.DSGCall;
import dsg.network.DSGCall.DSGCallType;
import dsg.network.DSGMessage;
import dsg.network.DSGServer;

/**
 * Server running the HTTP protocol.
 */
public class DSGHTTPServer extends DSGServer {

    // ##################
    // # INITIALIZATION #
    // ##################

    /** The handler processing incoming requests. */
    private final DSGHTTPHandler handler;

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
        this.handler = handler;
    }

    // ##########
    // # EVENTS #
    // ##########

    // TODO Implement event handling.

    @Override
    protected void accepted(SocketAddress remote) {
        network.dispatch(DSGCall.create(DSGCallType.RECEIVE, remote, new DSGHTTPRequest()));
    }

    @Override
    protected void received(SocketAddress from, DSGMessage message) {
        DSGHTTPRequest request = (DSGHTTPRequest) message;

        DSGHTTPResponse response;
        try {
            response = handler.handle(request);
            if (response == null) {
                response = new DSGHTTPResponse(DSGHTTPStatus.NO_CONTENT);
            }
        } catch (DSGHTTPRequestException e) {
            response = new DSGHTTPResponse(e.getStatus(), e.getMessage());
        } catch (DSGHTTPException e) {
            response = new DSGHTTPResponse(DSGHTTPStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } catch (Exception e) {
            response = new DSGHTTPResponse(DSGHTTPStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        response.getHeader().set("Connection", shouldClose(request) ? "close" : "keep-alive");
        network.dispatch(DSGCall.create(DSGCallType.SEND, from, response));
    }

    /**
     * Return whether the connection should be closed after responding to
     * {@code request}, based on its HTTP version and Connection header field.
     */
    private boolean shouldClose(DSGHTTPRequest request) {
        String connection = request.getHeader().get("Connection");
        boolean keepAliveRequested = connection != null && connection.equalsIgnoreCase("keep-alive");
        boolean closeRequested = connection != null && connection.equalsIgnoreCase("close");

        if (request.getVersion().equals("HTTP/1.0")) {
            return !keepAliveRequested;
        }
        return closeRequested;
    }

    @Override
    protected void sent(SocketAddress to, DSGMessage message) {
        DSGHTTPResponse response = (DSGHTTPResponse) message;
        String connection = response.getHeader().get("Connection");
        if (connection != null && connection.equalsIgnoreCase("close")) {
            network.dispatch(DSGCall.create(DSGCallType.CLOSE, to, null));
        } else {
            // Connection stays open; keep listening for the client's next request on it.
            network.dispatch(DSGCall.create(DSGCallType.RECEIVE, to, new DSGHTTPRequest()));
        }
    }

    @Override
    protected void failed(DSGCall call) {
        switch (call.getType()) {
        case RECEIVE:
            IOException exception = call.getException();
            DSGHTTPResponse response;
            if (exception instanceof DSGHTTPRequestException) {
                DSGHTTPRequestException e = (DSGHTTPRequestException) exception;
                response = new DSGHTTPResponse(e.getStatus(), e.getMessage());
            } else if (exception instanceof DSGHTTPException) {
                response = new DSGHTTPResponse(DSGHTTPStatus.BAD_REQUEST, exception.getMessage());
            } else {
                network.dispatch(DSGCall.create(DSGCallType.CLOSE, call.getRemote(), null));
                break;
            }
            response.getHeader().set("Connection", "close");
            network.dispatch(DSGCall.create(DSGCallType.SEND, call.getRemote(), response));
            break;
        case SEND:
            network.dispatch(DSGCall.create(DSGCallType.CLOSE, call.getRemote(), null));
            break;
        default:
            break;
        }
    }
}

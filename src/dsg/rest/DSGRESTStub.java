package dsg.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

import dsg.http.DSGHTTPClient;
import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPMethod;
import dsg.http.DSGHTTPRequest;
import dsg.http.DSGHTTPResponse;
import dsg.http.DSGHTTPStatus;

/**
 * Client-side representation of a {@link DSGRESTResource}.
 */
public class DSGRESTStub implements DSGRESTResource {
    private URI target;
    private DSGHTTPClient client;

    // ##################
    // # INITIALIZATION #
    // ##################

    /**
     * Create a new client stub for the named {@code target} resource, using
     * {@code client} to issue HTTP request.
     *
     * @param target the target REST resource.
     * @param client the HTTP client used to issue requests.
     */
    public DSGRESTStub(URI target, DSGHTTPClient client) {
        this.target = target;
        this.client = client;
    }

    /**
     * Return the stub's target resource.
     *
     * @return the stub's target resource.
     */
    public URI getTarget() {
        return this.target;
    }

    /**
     * Return the stub's underlying HTTP client.
     *
     * @return the stub's underlying HTTP client.
     */
    public DSGHTTPClient getClient() {
        return this.client;
    }

    // ######################
    // # REQUEST SUBMISSION #
    // ######################

    @Override
    public DSGRESTRepresentation get(DSGRESTContext ctx, DSGHTTPHeader header)
            throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return invoke(DSGHTTPMethod.GET, ctx, null, header);
    }

    @Override
    public DSGRESTRepresentation head(DSGRESTContext ctx, DSGHTTPHeader header)
            throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return invoke(DSGHTTPMethod.HEAD, ctx, null, header);
    }

    @Override
    public DSGRESTRepresentation put(DSGRESTContext ctx, DSGRESTRepresentation representation)
            throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return invoke(DSGHTTPMethod.PUT, ctx, representation, null);
    }

    @Override
    public DSGRESTRepresentation post(DSGRESTContext ctx, DSGRESTRepresentation representation)
            throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return invoke(DSGHTTPMethod.POST, ctx, representation, null);
    }

    @Override
    public DSGRESTRepresentation delete(DSGRESTContext ctx) throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return invoke(DSGHTTPMethod.DELETE, ctx, null, null);
    }

    /**
     * Build and send an HTTP request for {@code method}, then transform the
     * response into the corresponding return value or exception.
     *
     * @param method         the HTTP method to call.
     * @param ctx            a REST context.
     * @param representation a byte representation of a REST resource, used as the
     *                       request body for PUT/POST calls.
     * @param extraHeader    additional header fields to add to the request, used by
     *                       GET/HEAD calls.
     * @return the resource representation returned by the server.
     * @throws DSGRESTException       if the server-side method call failed.
     * @throws DSGRESTRemoteException if a communication-related error occurred.
     */
    private DSGRESTRepresentation invoke(DSGHTTPMethod method, DSGRESTContext ctx,
            DSGRESTRepresentation representation, DSGHTTPHeader extraHeader)
            throws DSGRESTException, DSGRESTRemoteException {
        DSGHTTPRequest request = buildRequest(method, ctx, representation);
        if (extraHeader != null) {
            DSGHTTPHeader requestHeader = request.getHeader();
            for (Entry<String, List<String>> field : extraHeader.fields().entrySet()) {
                for (String value : field.getValue()) {
                    requestHeader.add(field.getKey(), value);
                }
            }
        }

        DSGHTTPResponse response;
        try {
            response = client.communicate(request);
        } catch (IOException e) {
            throw new DSGRESTRemoteException(e);
        }

        DSGHTTPStatus status = response.getStatus();
        if (status == DSGHTTPStatus.NO_CONTENT) {
            return null;
        }

        int code = status.getCode();
        if (code >= 200 && code < 300) {
            try {
                return new DSGRESTRepresentation(response);
            } catch (DSGHTTPException e) {
                throw new DSGRESTRemoteException(e);
            }
        }
        if (code >= 300 && code < 400) {
            try {
                return new DSGRESTRedirect(target, response);
            } catch (URISyntaxException | DSGHTTPException e) {
                throw new DSGRESTRemoteException(e);
            }
        }
        if (status == DSGHTTPStatus.METHOD_NOT_ALLOWED) {
            throw new UnsupportedOperationException("Method not implemented");
        }
        throw new DSGRESTException(status);
    }

    // ########################
    // # REQUEST CONSTRUCTION #
    // ########################

    /**
     * Builds and returns an HTTP request from the given REST data.
     *
     * @param method         the HTTP method to call.
     * @param ctx            a REST context.
     * @param representation a byte representation of a REST resource.
     * @return an HTTP request corresponding to the given REST data.
     */
    private DSGHTTPRequest buildRequest(DSGHTTPMethod method, DSGRESTContext ctx,
            DSGRESTRepresentation representation) {
        if (ctx == null && representation == null) {
            return new DSGHTTPRequest(method, target, null);
        }

        String mergedQuery = target.getQuery();
        if (ctx != null) {
            String ctxQuery = ctx.getQuery().encode();
            if (mergedQuery == null || mergedQuery.isEmpty()) {
                mergedQuery = ctxQuery;
            } else if (!ctxQuery.isEmpty()) {
                mergedQuery += "&" + ctxQuery;
            }
        }

        URI uri;
        try {
            uri = new URI(target.getScheme(), target.getUserInfo(), target.getHost(), target.getPort(),
                    target.getPath(), mergedQuery, target.getFragment());
        } catch (URISyntaxException e) {
            // Should never happen, because we are copying from an existing URI and ctxQuery
            // should always correctly encode things.
            throw new IllegalStateException(e);
        }

        DSGHTTPRequest request;
        if (representation != null) {
            InputStream body = representation.getResource();
            request = new DSGHTTPRequest(method, representation.getHeader(), uri, body);
        } else {
            request = new DSGHTTPRequest(method, uri, null);
        }
        if (ctx == null) {
            return request;
        }

        DSGHTTPHeader header = request.getHeader();
        for (DSGHTTPMediaType acceptedRepresentation : ctx.getAcceptedRepresentations()) {
            header.add("Accept", acceptedRepresentation.toString());
        }
        return request;
    }
}

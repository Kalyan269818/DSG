package dsg.rest;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import dsg.http.DSGHTTPClient;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPMethod;
import dsg.http.DSGHTTPRequest;

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
        return null;
    }

    @Override
    public DSGRESTRepresentation head(DSGRESTContext ctx, DSGHTTPHeader header)
            throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return null;
    }

    @Override
    public DSGRESTRepresentation put(DSGRESTContext ctx, DSGRESTRepresentation representation)
            throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return null;
    }

    @Override
    public DSGRESTRepresentation post(DSGRESTContext ctx, DSGRESTRepresentation representation)
            throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return null;
    }

    @Override
    public DSGRESTRepresentation delete(DSGRESTContext ctx) throws DSGRESTException, DSGRESTRemoteException {
        // TODO Implement method
        return null;
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

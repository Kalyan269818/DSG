package dsg.rest;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPResponse;
import dsg.http.DSGHTTPStatus;

/**
 * A REST resource representation that redirects clients to a different
 * location, where the actual representation can be found.
 */
public class DSGRESTRedirect extends DSGRESTRepresentation {
    private static final String LOCATION_HEADER_NAME = "Location";

    /**
     * Initializes a REST redirect with an additional resource payload.
     *
     * The payload, if any, should be a representation of the redirect taking place
     * (e.g. a HTML document that contains a link to the new location).
     *
     * @param status   the redirect status.
     * @param target   the resource to which clients are redirected.
     * @param type     the media type of {@code resource}.
     * @param resource a byte stream representation of a REST resource.
     * @throws IllegalArgumentException if {@code target} is {@code null} or
     *                                  {@code status} is not a redirect status code
     *                                  (i.e. code 300-399).
     * @throws IllegalArgumentException if {@code resource} without a {@code type}
     *                                  is specified.
     */
    public DSGRESTRedirect(DSGHTTPStatus status, URI target, DSGHTTPMediaType type, InputStream resource) {
        super(status, type, resource);
        if (target == null) {
            throw new IllegalArgumentException("Target cannot be null");
        }
        int code = status.getCode();
        if (code < 300 || code >= 400) {
            throw new IllegalArgumentException("status is not a redirect");
        }
        getHeader().set(LOCATION_HEADER_NAME, target.toASCIIString());
    }

    /**
     * Initializes a REST redirect without a resource payload.
     *
     * @param status the redirect status.
     * @param target the target resource to which clients are redirected.
     */
    public DSGRESTRedirect(DSGHTTPStatus status, URI target) {
        this(status, target, null, null);
    }

    /**
     * Initializes a REST redirect from the original target URI and a redirect
     * response.
     *
     * @param originalTarget the request's original target URI.
     * @param response       the redirect response received from the server.
     *
     * @throws URISyntaxException if the Location header of the response does not
     *                            contain a valid URI.
     * @throws DSGHTTPException   if the content type returned by the server is
     *                            invalid.
     */
    protected DSGRESTRedirect(URI originalTarget, DSGHTTPResponse response)
            throws URISyntaxException, DSGHTTPException {
        this(response.getStatus(), resolveTarget(originalTarget, response), resolveType(response), response.getBody());
    }

    private static URI resolveTarget(URI originalTarget, DSGHTTPResponse response) throws URISyntaxException {
        String location = response.getHeader().get(LOCATION_HEADER_NAME);
        return originalTarget.resolve(new URI(location));
    }

    private static DSGHTTPMediaType resolveType(DSGHTTPResponse response) throws DSGHTTPException {
        String contentType = response.getHeader().get(CONTENT_TYPE_HEADER_NAME);
        if (contentType != null && !contentType.isBlank()) {
            return new DSGHTTPMediaType(contentType);
        }
        return null;
    }

    /**
     * Return the location to which client's are redirected.
     *
     * @return the redirect location.
     */
    public String getLocation() {
        return getHeader().get(LOCATION_HEADER_NAME);
    }
}

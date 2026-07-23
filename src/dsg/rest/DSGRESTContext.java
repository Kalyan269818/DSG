package dsg.rest;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPRequest;
import dsg.http.DSGHTTPRequestException;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGHTTPValues;

/**
 * Metadata related to a client's REST request.
 * 
 * Among others, it describes the resource representations the client accepts.
 */
public class DSGRESTContext {
    private static final String ACCEPT_HEADER_NAME = "Accept";

    private URI target;
    private DSGHTTPValues query;
    private List<DSGHTTPMediaType> acceptedRepresentations;

    /**
     * Create an empty context.
     */
    public DSGRESTContext() {
        target = null;
        query = new DSGHTTPValues();
        acceptedRepresentations = new LinkedList<>();
    }

    /**
     * Create a REST context from an HTTP request and its corresponding HTTP handler
     * context.
     * 
     * @param request the request from which to build the context.
     * @throws DSGHTTPRequestException if {@code request} has invalid content types
     *                                 in its Accept header field.
     */
    protected DSGRESTContext(DSGHTTPRequest request) throws DSGHTTPRequestException {
        this.target = request.getTarget();
        this.query = request.getQuery();
        parseAcceptedRepresentations(request.getHeader().values(ACCEPT_HEADER_NAME));
    }

    /**
     * Return the resource identifier (URI) originally requested by the client.
     * 
     * This is only filled for contexts on the server side.
     * 
     * @return the target URI requested by the client or null, if called at the
     *         client.
     */
    public URI getTarget() {
        return target;
    }

    /**
     * Return the context's query parameters.
     *
     * @return the context's query parameters.
     */
    public DSGHTTPValues getQuery() {
        return query;
    }

    /**
     * Return the representation formats (i.e. media-types) accepted by the client.
     * 
     * The returned list is ordered by client preference, meaning the client would
     * like to receive a representation of the target resource with a media type.
     * 
     * @return list of representations for the target resource.
     */
    public List<DSGHTTPMediaType> getAcceptedRepresentations() {
        return acceptedRepresentations;
    }

    /**
     * Add an accepted representation format.
     * 
     * @param acceptedRepresentation the representation format that will be added.
     */
    public void addAcceptedRepresentation(DSGHTTPMediaType acceptedRepresentation) {
        this.acceptedRepresentations.add(acceptedRepresentation);
    }

    /**
     * Return, whether {@code offer} is one of representations accepted by the
     * client.
     * 
     * @param offer the media type offered by the server.
     * @return true if {@code offer} is a media type accepted by the client.
     */
    public boolean acceptsRepresentation(DSGHTTPMediaType offer) {
        if (acceptedRepresentations == null || acceptedRepresentations.isEmpty()) {
            return false;
        }
        for (DSGHTTPMediaType type : acceptedRepresentations) {
            if (type.accepts(offer)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Run HTTP's content negotiation protocol and return the negotiated media type.
     * 
     * @param offered the list of media types offered by the server.
     * @return the negotiated media type or null, if no match was found.
     * @see <a href=
     *      "https://www.rfc-editor.org/rfc/rfc9110.html#content.negotiation">RFC
     *      9110</a>
     */
    public DSGHTTPMediaType negotiate(Collection<DSGHTTPMediaType> offered) {
        if (offered.isEmpty()) {
            throw new IllegalArgumentException("At least one media type must be offered");
        }
        if (acceptedRepresentations.isEmpty()) {
            // Unfortunately, collections do not have a way to fetch the first
            // element without removing it, so we have to use the streams API
            // here. We can safely call get() here, because we checked above that the
            // collection is not empty.
            return offered.stream().findFirst().get();
        }
        for (DSGHTTPMediaType accepted : acceptedRepresentations) {
            for (DSGHTTPMediaType offer : offered) {
                // Allow the client to specify the target charset, unless the server-side
                // explicitly indicates that it can only offer a specific one.
                if (accepted.hasCharset() && !offer.hasCharset()) {
                    offer = offer.withCharset(accepted.getCharset());
                }
                if (accepted.accepts(offer)) {
                    return offer;
                }
            }
        }
        return null;
    }

    private void parseAcceptedRepresentations(List<String> representations) throws DSGHTTPRequestException {
        List<DSGHTTPMediaType> result = new LinkedList<>();
        if (representations != null) {
            for (String acceptedType : representations) {
                DSGHTTPMediaType mediaType;
                try {
                    mediaType = new DSGHTTPMediaType(acceptedType);
                } catch (DSGHTTPException e) {
                    throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST, e.getMessage());
                }
                result.add(mediaType);
            }
        }
        this.acceptedRepresentations = result;
    }
}

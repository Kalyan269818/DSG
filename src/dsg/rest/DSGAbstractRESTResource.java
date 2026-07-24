package dsg.rest;

import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPStatus;

/**
 * Server-side implementation of a {@link DSGRESTResource} that does not allow
 * any method type to be called.
 */
public abstract class DSGAbstractRESTResource implements DSGRESTResource {
    @Override
    public DSGRESTRepresentation get(DSGRESTContext ctx, DSGHTTPHeader header) throws DSGRESTException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public DSGRESTRepresentation head(DSGRESTContext ctx, DSGHTTPHeader header) throws DSGRESTException {
        try {
            // Ensure that the resource body is always empty for HEAD requests.
            DSGRESTRepresentation representation = get(ctx, header);
            if (representation == null) {
                return null;
            }
            return new DSGRESTRepresentation(representation.getStatus(), representation.getHeader(), null);
        } catch (DSGHTTPException e) {
            // This should never happen.
            throw new DSGRESTException(DSGHTTPStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public DSGRESTRepresentation put(DSGRESTContext ctx, DSGRESTRepresentation resource) throws DSGRESTException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public DSGRESTRepresentation post(DSGRESTContext ctx, DSGRESTRepresentation resource) throws DSGRESTException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public DSGRESTRepresentation delete(DSGRESTContext ctx) throws DSGRESTException {
        throw new UnsupportedOperationException("Method not implemented");
    }
}

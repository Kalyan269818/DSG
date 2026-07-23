package dsg.activitypub;

import dsg.http.DSGHTTPStatus;
import dsg.rest.DSGRESTRepresentation;

/**
 * Exception thrown in failed authentication attempts.
 */
public class DSGActivityPubAuthorizationException extends DSGActivityPubException {

    /**
     * Initialize an authorization exception with a default message.
     */
    public DSGActivityPubAuthorizationException() {
        super("Actor does not have authorization to access this object");
    }

    /**
     * Initialize an authorization exception for an authentication attempt with the
     * given username.
     *
     * @param username the name of the user for which the login attempt failed.
     */
    public DSGActivityPubAuthorizationException(String username) {
        super("Failed login attempt for user = " + username);
    }

    /**
     * Returns the {@link DSGRESTRepresentation} of this exception.
     *
     * @return the exception's REST representation.
     */
    public DSGRESTRepresentation toREST() {
        DSGRESTRepresentation representation = new DSGRESTRepresentation(DSGHTTPStatus.UNAUTHORIZED);
        representation.getHeader().set("WWW-Authenticate", "Basic realm=ActivityPub");
        return representation;
    }
}

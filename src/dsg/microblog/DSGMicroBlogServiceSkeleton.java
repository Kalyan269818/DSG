package dsg.microblog;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;

import dsg.activitypub.DSGActivityPubActor;
import dsg.activitypub.DSGActivityPubAuthenticator;
import dsg.activitypub.DSGActivityPubAuthorizationException;
import dsg.activitypub.DSGActivityPubException;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGHTTPValues;
import dsg.http.DSGStandardMediaTypes;
import dsg.rest.DSGAbstractRESTResource;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRedirect;
import dsg.rest.DSGRESTRepresentation;

/**
 * REST skeleton for the micro blog service.
 *
 * This endpoint allows creating new users by sending form data via a POST
 * request.
 */
public class DSGMicroBlogServiceSkeleton extends DSGAbstractRESTResource {
    /** The micro blog service instance to which requests are forwarded. */
    private DSGMicroBlogService service;
    /** The authenticator used to sing in users. */
    private DSGActivityPubAuthenticator authenticator;

    /**
     * Initialize a micro blog service skeleton for the given {@code service}.
     *
     * @param service       the service this skeleton represents.
     * @param authenticator an authenticator used to authenticate incoming requests.
     */
    public DSGMicroBlogServiceSkeleton(DSGMicroBlogService service, DSGActivityPubAuthenticator authenticator) {
        this.service = service;
        this.authenticator = authenticator;
    }

    @Override
    public DSGRESTRepresentation get(DSGRESTContext ctx, DSGHTTPHeader headers) throws DSGRESTException {
        String authorization = headers.get("Authorization");
        try {
            DSGActivityPubActor actor = authenticator.authenticate(authorization);
            if (actor == null) {
                return null;
            }
            return new DSGRESTRepresentation(DSGStandardMediaTypes.ACTIVITY_STREAMS, actor.toJSON().toInputStream());
        } catch (DSGActivityPubAuthorizationException apae) {
            return null;
        } catch (DSGActivityPubException e) {
            e.printStackTrace();
            throw new DSGRESTException(DSGHTTPStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public DSGRESTRepresentation post(DSGRESTContext ctx, DSGRESTRepresentation representation)
            throws DSGRESTException {
        InputStream resource = representation.getResource();
        DSGHTTPMediaType type = representation.getType();
        if (resource == null || type == null) {
            throw new DSGRESTException(DSGHTTPStatus.BAD_REQUEST);
        }
        if (!type.accepts(DSGStandardMediaTypes.FORM_URL_ENCODED)) {
            throw new DSGRESTException(DSGHTTPStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        try {
            DSGHTTPValues values = new DSGHTTPValues(type, resource);
            String username = values.get("username");
            if (username != null) {
                username = username.trim();
            }
            String password = values.get("password");
            DSGActivityPubActor user = service.register(username, password);
            return new DSGRESTRedirect(DSGHTTPStatus.SEE_OTHER, user.getId());
        } catch (FileAlreadyExistsException fae) {
            throw new DSGRESTException(DSGHTTPStatus.CONFLICT);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new DSGRESTException(DSGHTTPStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            throw new DSGRESTException(DSGHTTPStatus.BAD_REQUEST);
        }
    }
}

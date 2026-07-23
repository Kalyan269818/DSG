package dsg.activitypub;

import java.util.Arrays;
import java.util.List;

import dsg.activitystreams.DSGActivityStreamsEntity;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGStandardMediaTypes;
import dsg.json.DSGJSONValue;
import dsg.rest.DSGAbstractRESTResource;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRepresentation;

/**
 * Server-side REST skeleton for serving ActivityPub objects to clients.
 */
public class DSGActivityPubObjectSkeleton extends DSGAbstractRESTResource {
    private static final List<DSGHTTPMediaType> ACCEPTED_MEDIA_TYPES = Arrays
            .asList(DSGStandardMediaTypes.ACTIVITY_STREAMS);
    /** The object this skeleton serves to its callers. */
    private DSGActivityPubObject object;
    /** The authenticator used for access control. */
    private DSGActivityPubAuthenticator authenticator;

    /**
     * Initialize a Skeleton representing the given {@code object} and using
     * {@code authenticator} to log in users.
     *
     * @param object        the object served to callers.
     * @param authenticator the authenticator used for access control.
     */
    public DSGActivityPubObjectSkeleton(DSGActivityPubObject object, DSGActivityPubAuthenticator authenticator) {
        this.object = object;
        this.authenticator = authenticator;
    }

    /**
     * Return the requested ActivityStreams object if the caller has authorization
     * to see it.
     */
    @Override
    public DSGRESTRepresentation get(DSGRESTContext ctx, DSGHTTPHeader header) throws DSGRESTException {
        DSGHTTPMediaType type = ctx.negotiate(ACCEPTED_MEDIA_TYPES);
        if (type == null) {
            throw new DSGRESTException(DSGHTTPStatus.NOT_ACCEPTABLE);
        }

        String authorization = header.get("Authorization");
        try {
            DSGActivityPubActor actor = authenticator.authenticate(authorization);
            DSGActivityStreamsEntity result = object.get(actor);
            if (result == null) {
                throw new DSGRESTException(DSGHTTPStatus.NOT_FOUND);
            }
            DSGJSONValue json = result.toJSON();
            return new DSGRESTRepresentation(DSGHTTPStatus.OK, type, json.toInputStream(type.getCharset()));
        } catch (DSGActivityPubAuthorizationException ae) {
            return ae.toREST();
        } catch (DSGActivityPubException e) {
            e.printStackTrace();
            throw new DSGRESTException(DSGHTTPStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

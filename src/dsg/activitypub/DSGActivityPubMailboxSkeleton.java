package dsg.activitypub;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsEntity;
import dsg.activitystreams.DSGActivityStreamsCollection;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGStandardMediaTypes;
import dsg.json.DSGJSONException;
import dsg.json.DSGJSONValue;
import dsg.rest.DSGAbstractRESTResource;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRepresentation;

/**
 * Server-side REST skeleton for ActivityPub mailboxes.
 */
public class DSGActivityPubMailboxSkeleton extends DSGAbstractRESTResource {
    private static final List<DSGHTTPMediaType> ACCEPTED_MEDIA_TYPES = Arrays
            .asList(DSGStandardMediaTypes.ACTIVITY_STREAMS);
    /** The application-level mailbox to which requests are forwarded. */
    private DSGActivityPubMailbox mailbox;
    /** The authenticator used to log users in. */
    private DSGActivityPubAuthenticator authenticator;

    // ##################
    // # INITIALIZATION #
    // ##################

    /**
     * Initialize a REST stub for the given {@code mailbox}, using
     * {@code authenticator} to authenticate clients.
     *
     * @param mailbox       the ActivityPub mailbox to which requests are forwarded.
     * @param authenticator the authenticator used to authenticate users.
     */
    public DSGActivityPubMailboxSkeleton(DSGActivityPubMailbox mailbox, DSGActivityPubAuthenticator authenticator) {
        this.mailbox = mailbox;
        this.authenticator = authenticator;
    }

    // ######################
    // # MAILBOX OPERATIONS #
    // ######################

    /**
     * Return all activities from a user's inbox the client is allowed to see.
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
            DSGActivityStreamsCollection<DSGActivityStreamsActivity> activities = mailbox.fetch(actor);
            DSGJSONValue json = activities.toJSON();
            return new DSGRESTRepresentation(DSGHTTPStatus.OK, type, json.toInputStream(type.getCharset()));
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
            throw new DSGRESTException(DSGHTTPStatus.BAD_REQUEST);
        } catch (DSGActivityPubAuthorizationException ae) {
            return ae.toREST();
        } catch (DSGActivityPubException e) {
            e.printStackTrace();
            throw new DSGRESTException(DSGHTTPStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Post a new activity to a user's mailbox using ActivityPub's client-to-server
     * or server-to-server protocol.
     */
    @Override
    public DSGRESTRepresentation post(DSGRESTContext ctx, DSGRESTRepresentation representation)
            throws DSGRESTException {
        // TODO Implement method
        throw new UnsupportedOperationException("Delivery not implemented, yet");
    }
}

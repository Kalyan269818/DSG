package dsg.microblog;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import dsg.activitypub.DSGActivityPubActor;
import dsg.activitypub.DSGActivityPubAuthorizationException;
import dsg.activitypub.DSGActivityPubException;
import dsg.activitypub.DSGActivityPubFederator;
import dsg.activitypub.DSGActivityPubMailbox;
import dsg.activitypub.DSGActivityPubObjectSkeleton;
import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsLink;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.activitystreams.DSGActivityStreamsCollection;
import dsg.rest.DSGRESTSkeleton;

/**
 * ActivityPub outbox implementation for our micro blog service.
 */
public class DSGMicroBlogOutbox implements DSGActivityPubMailbox {

    /** The outbox's unique identifier. */
    private URI id;
    /** The ID of the outbox's owner. */
    private URI owner;
    /** The router used to export objects and activities created by this outbox. */
    private DSGRESTSkeleton router;
    /** The storage where all objects of the local server are stored. */
    private DSGMicroBlogStorage storage;
    /**
     * The federator used for delivery of outbound messages (optional for
     * DSG-IDistrSys-B).
     */
    private DSGActivityPubFederator federator;
    /**
     * The authentication used for {@link DSGActivityPubObjectSkeleton} instances
     * for resources created by this outbox.
     */
    private DSGMicroBlogAuthenticator authenticator;

    // ##################
    // # INITIALIZATION #
    // ##################

    /**
     * Initialize an outbox for the micro blog service.
     *
     * @param id            the outbox's unique identifier.
     * @param owner         the unique identifier of the outbox's owner.
     * @param router        the router used to export resources created by this
     *                      outbox.
     * @param storage       the persistent storage used to store activities and
     *                      objects.
     * @param federator     the federator instance used for outbound delivery.
     * @param authenticator the authenticator used for instantiation of new object
     *                      skeletons.
     */
    public DSGMicroBlogOutbox(URI id, URI owner, DSGRESTSkeleton router, DSGMicroBlogStorage storage,
            DSGActivityPubFederator federator, DSGMicroBlogAuthenticator authenticator) {
        this.id = id;
        this.owner = owner;
        this.router = router;
        this.storage = storage;
        this.federator = federator;
        this.authenticator = authenticator;
    }

    // ######################
    // # MAILBOX OPERATIONS #
    // ######################

    @SuppressWarnings("unchecked")
    @Override
    public DSGActivityStreamsCollection<DSGActivityStreamsActivity> fetch(DSGActivityPubActor actor)
            throws DSGActivityPubException {
        // TODO Implement method
        throw new UnsupportedOperationException("Federation protocol not implemented");
    }

    @SuppressWarnings("unchecked")
    @Override
    public URI deliver(DSGActivityPubActor actor, DSGActivityStreamsActivity activity)
            throws DSGActivityPubAuthorizationException, DSGActivityPubException {
        // TODO Implement method
        throw new UnsupportedOperationException("Delivering to user's outbox not implemented, yet");
    }
}

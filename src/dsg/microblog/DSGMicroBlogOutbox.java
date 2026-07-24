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
        if (actor == null || actor.getId() == null || !actor.getId().equals(owner)) {
            throw new DSGActivityPubAuthorizationException();
        }

        DSGMicroBlogUser user = (DSGMicroBlogUser) actor;

        try {
            // The server is authoritative for IDs; assign fresh ones to the activity and
            // (if present) the object it creates, rather than trusting whatever the caller
            // supplied.
            URI activityID = user.getNextObjectID();
            activity.setId(activityID);

            DSGActivityStreamsObject object = activity.getObject();
            if (object != null) {
                object.setId(user.getNextObjectID());
                storage.storeObject(object);
                router.exportResource(object.getId().getPath(), new DSGActivityPubObjectSkeleton(
                        new DSGMicroBlogObject(object.getId(), storage), authenticator));
            }

            // Store the activity itself, add it to the outbox's own collection, and
            // export a REST resource for it.
            storage.storeObject(activity);
            router.exportResource(activityID.getPath(),
                    new DSGActivityPubObjectSkeleton(new DSGMicroBlogObject(activityID, storage), authenticator));

            DSGActivityStreamsCollection<DSGActivityStreamsActivity> outbox = (DSGActivityStreamsCollection<DSGActivityStreamsActivity>) storage
                    .getObject(id);
            if (outbox == null) {
                throw new DSGActivityPubException("Outbox " + id + " does not exist");
            }
            outbox.add(activity);
            storage.storeObject(outbox);

            // Deliver to all local recipients (i.e. recipients sharing this outbox's origin).
            for (DSGMicroBlogUser recipient : storage.getUsers()) {
                if (!activity.hasSameOrigin(recipient.getId()) || !activity.isRecipient(recipient)) {
                    continue;
                }

                URI inboxID = recipient.getInbox().getTarget();
                DSGActivityStreamsCollection<DSGActivityStreamsActivity> inbox = (DSGActivityStreamsCollection<DSGActivityStreamsActivity>) storage
                        .getObject(inboxID);
                if (inbox == null) {
                    continue;
                }
                inbox.add(activity);
                storage.storeObject(inbox);
            }

            return activityID;
        } catch (IOException e) {
            throw new DSGActivityPubException(e);
        }
    }
}

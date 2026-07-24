package dsg.microblog;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import dsg.activitypub.DSGActivityPubActor;
import dsg.activitypub.DSGActivityPubAuthorizationException;
import dsg.activitypub.DSGActivityPubException;
import dsg.activitypub.DSGActivityPubMailbox;
import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsLink;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.activitystreams.DSGActivityStreamsCollection;

/**
 * ActivityPub inbox implementation for our micro blog service.
 */
public class DSGMicroBlogInbox implements DSGActivityPubMailbox {
    /** The object's ID. */
    private URI id;
    /** The object owner's ID. */
    private URI owner;
    /** The storage where all objects of the local server are stored. */
    private DSGMicroBlogStorage storage;

    // ##################
    // # INITIALIZATION #
    // ##################

    /**
     * Initialize the inbox for the named {@code owner}.
     *
     * @param id      the inbox's unique identifier.
     * @param owner   the inbox's owner.
     * @param storage the persistent storage where the feed is stored.
     */
    public DSGMicroBlogInbox(URI id, URI owner, DSGMicroBlogStorage storage) {
        this.id = id;
        this.owner = owner;
        this.storage = storage;
    }

    // ######################
    // # MAILBOX OPERATIONS #
    // ######################

    @SuppressWarnings("unchecked")
    @Override
    public DSGActivityStreamsCollection<DSGActivityStreamsActivity> fetch(DSGActivityPubActor actor)
            throws DSGActivityPubAuthorizationException, DSGActivityPubException {
        // TODO: Implement method
        if (actor == null || actor.getId() == null || !actor.getId().equals(owner)) {
            throw new DSGActivityPubAuthorizationException();
        }

        try {
            DSGActivityStreamsCollection<DSGActivityStreamsActivity> collection = (DSGActivityStreamsCollection<DSGActivityStreamsActivity>) storage
                    .getObject(id);
            if (collection == null) {
                throw new DSGActivityPubException("Inbox " + id + " does not exist");
            }
            // Items are stored in insertion order; fetch() returns them in reverse
            // (most-recent-first) order instead.
            Collections.reverse(collection.getItems());
            return collection;
        } catch (IOException e) {
            throw new DSGActivityPubException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public URI deliver(DSGActivityPubActor actor, DSGActivityStreamsActivity activity)
            throws DSGActivityPubAuthorizationException, DSGActivityPubException {
        // TODO: Implement method
        throw new UnsupportedOperationException("Federation protocol not supported");
    }
}

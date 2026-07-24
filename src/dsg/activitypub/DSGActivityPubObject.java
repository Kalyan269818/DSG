package dsg.activitypub;

import dsg.activitystreams.DSGActivityStreamsObject;

/**
 * Interface for application-level implementations of ActivityPub resources.
 */
public interface DSGActivityPubObject {
    /**
     * Return the ActivityPub object's ActivityStreams representation.
     *
     * @param actor the actor that wants to access the object or null, if the actor
     *              is unauthenticated.
     * @return the object or null, if it does not exist.
     * @throws DSGActivityPubAuthorizationException if the actor's credential are
     *                                              invalid or if the actor is not
     *                                              allowed to access this object.
     * @throws DSGActivityPubException              if an unexpected problem occurs
     *                                              while trying to deliver the
     *                                              activity to the mailbox.
     * @throws IllegalArgumentException             if the actor's authorization
     *                                              information is malformed.
     */
    DSGActivityStreamsObject get(DSGActivityPubActor actor)
            throws DSGActivityPubAuthorizationException, DSGActivityPubException;
}

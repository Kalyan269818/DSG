package dsg.activitypub;

import java.net.URI;

import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsCollection;

/**
 * Interface for application-level implementations of an ActivityPub mailbox.
 */
public interface DSGActivityPubMailbox {
        /**
         * Fetch all available activities that have been posted to this mailbox.
         *
         * @param actor the ActivityPub actor that wants to access the mailbox or null,
         *              if the actor is not authenticated.
         * @return the mailbox's activities accessible with the given
         *         {@code authorization}.
         * @throws DSGActivityPubAuthorizationException if the actor's credential are
         *                                              invalid or if they do not grant
         *                                              access to the mailbox.
         * @throws DSGActivityPubException              if a problem occurs while trying
         *                                              to fetch the mailbox content.
         * @throws UnsupportedOperationException        if the server does not support
         *                                              the protocol necessary to fetch
         *                                              the mailbox's content.
         * @throws IllegalArgumentException             if the actor's authorization
         *                                              information is malformed.
         */
        DSGActivityStreamsCollection<DSGActivityStreamsActivity> fetch(DSGActivityPubActor actor)
                        throws DSGActivityPubAuthorizationException, DSGActivityPubException;

        /**
         * Deliver an activity to this mailbox.
         *
         * @param actor    the ActivityPub actor that wants to deliver a new activity to
         *                 the mailbox or null, if the actor is not authenticated.
         * @param activity the activity to post to the mailbox.
         * @return if the mailbox is an inbox, it returns the ID of the newly created
         *         activity, otherwise null is returned.
         * @throws DSGActivityPubAuthorizationException if the actor's credential are
         *                                              invalid or if the actor is not
         *                                              allowed to post activities to
         *                                              this mailbox.
         * @throws DSGActivityPubException              if a problem occurs while trying
         *                                              to deliver the activity to the
         *                                              mailbox.
         * @throws UnsupportedOperationException        if the server does not support
         *                                              the protocol necessary to
         *                                              deliver to the mailbox.
         * @throws IllegalArgumentException             if the actor's authorization
         *                                              information is malformed.
         */
        URI deliver(DSGActivityPubActor actor, DSGActivityStreamsActivity activity)
                        throws DSGActivityPubAuthorizationException, DSGActivityPubException;
}

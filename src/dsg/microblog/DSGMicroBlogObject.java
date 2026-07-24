package dsg.microblog;

import java.io.IOException;
import java.net.URI;

import dsg.activitypub.DSGActivityPubActor;
import dsg.activitypub.DSGActivityPubAuthorizationException;
import dsg.activitypub.DSGActivityPubException;
import dsg.activitypub.DSGActivityPubObject;
import dsg.activitystreams.DSGActivityStreamsObject;

/**
 * Implementation of {@link DSGActivityPubObject} that serves an object via its
 * ID.
 */
public class DSGMicroBlogObject implements DSGActivityPubObject {
    /** The object's ID. */
    private URI id;
    /** The storage where all objects of the local server are stored. */
    private DSGMicroBlogStorage storage;

    /**
     * Initialize a microblog object with the given {@code id}.
     *
     * @param id      the ID of the resource served by this wrapper.
     * @param storage the global storage instance.
     */
    public DSGMicroBlogObject(URI id, DSGMicroBlogStorage storage) {
        this.id = id;
        this.storage = storage;
    }

    @Override
    public DSGActivityStreamsObject get(DSGActivityPubActor actor)
            throws DSGActivityPubAuthorizationException, DSGActivityPubException {
        try {
            DSGActivityStreamsObject object = storage.getObject(id);
            // Actor profiles are always visible.
            if (object instanceof DSGActivityPubActor) {
                return object;
            }

            // Only recipients and the object's owner are allowed to post activities.
            // Return null (resource not found) in this case to preserve privacy.
            if (object == null || (!object.isRecipient(actor) && !object.isAttributedTo(actor))) {
                return null;
            }
            return object;
        } catch (IOException ioe) {
            throw new DSGActivityPubException(ioe);
        }
    }
}

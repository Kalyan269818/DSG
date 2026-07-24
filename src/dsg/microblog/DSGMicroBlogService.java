package dsg.microblog;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collection;

import dsg.activitypub.DSGActivityPubActor;
import dsg.activitypub.DSGActivityPubFederator;
import dsg.activitypub.DSGActivityPubMailboxSkeleton;
import dsg.activitypub.DSGActivityPubObjectSkeleton;
import dsg.activitystreams.DSGActivityStreamsCollection.DSGCollectionType;
import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsCollection;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.rest.DSGRESTSkeleton;

/**
 * Implementation of our micro-blogging service.
 */
public class DSGMicroBlogService {
    /** The base URI of the local server all object share. */
    private URI baseURI;
    /** The persistent storage dused to store objects. */
    private DSGMicroBlogStorage storage;
    /** An authenticator used to authenticate incoming HTTP requests. */
    private DSGMicroBlogAuthenticator authenticator;
    /** The REST router used to route requests to ActivityPub resources. */
    private DSGRESTSkeleton router;
    /** The federator used for outbound delivery. */
    private DSGActivityPubFederator federator;

    /**
     * Initialize a micro blog service.
     *
     * @param baseURI       the base URI of the local server.
     * @param storage       the storage to use to persistently store all microblog's
     *                      data.
     * @param authenticator authenticator used to authenticate incoming client
     *                      requests.
     * @param router        the REST skeleton used to route requests to the
     *                      microblog's resources.
     * @param federator     the service delivering activities to outbound
     *                      recipients.
     * @throws IOException if initializing existing resources from storage fails.
     */
    public DSGMicroBlogService(URI baseURI, DSGMicroBlogStorage storage, DSGMicroBlogAuthenticator authenticator,
            DSGRESTSkeleton router, DSGActivityPubFederator federator) throws IOException {
        this.baseURI = baseURI;
        this.storage = storage;
        this.authenticator = authenticator;
        this.router = router;
        this.federator = federator;

        // Create two initial users so students can test delivery.
        DSGMicroBlogUser alice = new DSGMicroBlogUser(baseURI, "alice", "123456");
        DSGMicroBlogUser bob = new DSGMicroBlogUser(baseURI, "bob", "123456");
        if (!storage.exists(alice)) {
            System.out.println("[SERVICE] Creating initial user \"alice\" with password 123456");
            register("alice", "123456");
        }
        if (!storage.exists(bob)) {
            System.out.println("[SERVICE] Creating initial user \"bob\" with password 123456");
            register("bob", "123456");
        }

        // Re-export existing objects
        Collection<DSGActivityStreamsObject> objects = storage.getObjects();
        for (DSGActivityStreamsObject object : objects) {
            URI id = object.getId();
            if (id == null || router.isExported(id.getPath())) {
                continue;
            }
            if (object instanceof DSGActivityPubActor) {
                DSGActivityPubActor actor = (DSGActivityPubActor) object;
                exportActor(actor);
                continue;
            }
            System.out.println("[SERVICE] Exporting object " + id.toString());
            router.exportResource(id.getPath(),
                    new DSGActivityPubObjectSkeleton(new DSGMicroBlogObject(id, storage), authenticator));
        }
    }

    /**
     * Create a new user and return the corresponding ActivityPub actor.
     *
     * @param username the user's name.
     * @param password the password to associate with the username.
     *
     * @return an ActivityPub client service for the newly registered user.
     * @throws IOException if writing the user to storage fails.
     */
    public DSGActivityPubActor register(String username, String password) throws IOException {
        if (username == null || username.isBlank()) {
            System.err.println("Username: " + username);
            throw new IllegalArgumentException("Username is null");
        }
        if (password == null || password.isEmpty()) {
            System.err.println("Password: " + password);
            throw new IllegalArgumentException("Password is null");
        }

        DSGMicroBlogUser newUser = new DSGMicroBlogUser(baseURI, username, password);
        if (storage.exists(newUser.getId())) {
            throw new FileAlreadyExistsException("User with that name already exists");
        }

        storage.storeUser(newUser);
        DSGActivityStreamsCollection<DSGActivityStreamsActivity> inbox = new DSGActivityStreamsCollection<DSGActivityStreamsActivity>(
                newUser.getInbox(), DSGCollectionType.ORDERED_COLLECTION);
        storage.storeObject(inbox);
        DSGActivityStreamsCollection<DSGActivityStreamsActivity> outbox = new DSGActivityStreamsCollection<DSGActivityStreamsActivity>(
                newUser.getOutbox(), DSGCollectionType.ORDERED_COLLECTION);
        storage.storeObject(outbox);
        exportActor(newUser);
        // Do not return the stored password to the caller.
        newUser.setPassword(null);
        System.out.println("[SERVICE] Created new user with username \"" + username + "\"");
        return newUser;
    }

    private void exportActor(DSGActivityPubActor actor) {
        URI id = actor.getId();
        URI inboxID = actor.getInbox().getTarget();
        String inboxPath = inboxID.getPath();
        URI outboxID = actor.getOutbox().getTarget();
        String outboxPath = outboxID.getPath();
        System.out.println("[SERVICE] Exporting actor " + id.toString());
        router.exportResource(id.getPath(),
                new DSGActivityPubObjectSkeleton(new DSGMicroBlogObject(id, storage), authenticator));
        System.out.println("[SERVICE] Exporting inbox " + inboxID.toString());
        router.exportResource(inboxPath,
                new DSGActivityPubMailboxSkeleton(new DSGMicroBlogInbox(inboxID, id, storage), authenticator));
        System.out.println("[SERVICE] Exporting outbox " + outboxID.toString());
        router.exportResource(outboxPath, new DSGActivityPubMailboxSkeleton(
                new DSGMicroBlogOutbox(outboxID, id, router, storage, federator, authenticator), authenticator));

    }
}

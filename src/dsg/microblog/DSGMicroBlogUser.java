package dsg.microblog;

import java.net.URI;
import java.util.UUID;

import dsg.activitypub.DSGActivityPubActor;
import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;

/**
 * A user of the micro blog service.
 *
 * This is an extension of {@link DSGActivityPubActor} that also stored a
 * password.
 */
public class DSGMicroBlogUser extends DSGActivityPubActor {
    /** Format string for user profile paths. */
    private static final String USER_STORAGE_PATH_FMT = "/user/%s";
    /** Format string for user inbox paths. */
    private static final String INBOX_STORAGE_PATH_FMT = "/user/%s/inbox";
    /** Format string for user outbox paths. */
    private static final String OUTBOX_STORAGE_PATH_FMT = "/user/%s/outbox";
    /** Format string for other objects crated by the user. */
    private static final String POSTS_STORAGE_PATH_FMT = "objects/%s";

    /** JSON object key for the actor's password. */
    private static final String PASSWORD_KEY = "password";

    /** The user's password. */
    private String password;

    /**
     * Initialize a new user.
     *
     * @param baseURI  the local servier's URI.
     * @param username the username for the user.
     * @param password the user's password.
     */
    public DSGMicroBlogUser(URI baseURI, String username, String password) {
        super(baseURI.resolve(String.format(USER_STORAGE_PATH_FMT, username)), username,
                baseURI.resolve(String.format(INBOX_STORAGE_PATH_FMT, username)),
                baseURI.resolve(String.format(OUTBOX_STORAGE_PATH_FMT, username)));
        setPassword(password);
    }

    /**
     * Parse micro blog user from its JSON representation.
     *
     * @param json the JSON object to parse.
     * @throws DSGJSONException if {@code json} is not a micro blog user.
     */
    public DSGMicroBlogUser(DSGJSONObject json) throws DSGJSONException {
        super(json);
        this.password = json.getMemberAsString(PASSWORD_KEY);
    }

    /**
     * Get a hash of the user's password.
     *
     * @return the user's password hash.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the user's password.
     *
     * @param password the user's new password.
     */
    public void setPassword(String password) {
        if (password == null) {
            this.password = null;
            return;
        }
        this.password = DSGMicroBlogAuthenticator.hashPassword(password);
    }

    /**
     * Get the next object ID for an object created by this user.
     *
     * @return the next unused ID for object created by this user.
     */
    public URI getNextObjectID() {
        // XXX: We use the inbox path here on purpose, because then the resolved path is
        // under the user's path.
        return getInbox().getTarget().resolve(String.format(POSTS_STORAGE_PATH_FMT, UUID.randomUUID()));
    }

    /**
     * Return the named user's ID on the micro blog instance running at the given
     * {@code baseURI}.
     *
     * @param baseURI  the server's base URI.
     * @param username the desired username.
     * @return the user's ID.
     */
    public static URI idForName(URI baseURI, String username) {
        return baseURI.resolve(String.format(USER_STORAGE_PATH_FMT, username));
    }

    @Override
    public DSGJSONObject toJSON() {
        DSGJSONObject json = super.toJSON();
        json.setMember(PASSWORD_KEY, password);
        return json;
    }
}

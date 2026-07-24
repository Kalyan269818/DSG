package dsg.activitypub;

import java.net.URI;

import dsg.activitystreams.DSGActivityStreamsActor;
import dsg.activitystreams.DSGActivityStreamsLink;
import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;

/**
 * Actor in ActivityPub's protocol. It is an extension of
 * {@link DSGActivityStreamsActor} that has additional attributes, like links to
 * the actor's in- and outbox.
 */
public class DSGActivityPubActor extends DSGActivityStreamsActor {
    /** JSON object key for the actor's inbox. */
    private static final String INBOX_KEY = "inbox";
    /** JSON object key for the actor's inbox. */
    private static final String OUTBOX_KEY = "outbox";
    /** JSON object key for the actor's inbox. */
    private static final String PREFERRED_USERNAME_KEY = "preferredUsername";

    /** The actor's inbox. */
    private DSGActivityStreamsLink inbox;

    /** The actor's outbox. */
    private DSGActivityStreamsLink outbox;

    /** The actor's preferred username. */
    private String preferredUsername;

    /** The actor's authorization information. */
    private String authorization;

    /**
     * Initialize an ActivityPub actor.
     *
     * @param id     an URI identifying the actor.
     * @param name   the actor's name.
     * @param inbox  URI to the actor's inbox.
     * @param outbox URI to the actor's outbox.
     */
    public DSGActivityPubActor(String id, String name, String inbox, String outbox) {
        super(id, name);
        if (inbox == null) {
            throw new IllegalArgumentException("Actor inbox may not be null");
        }
        if (outbox == null) {
            throw new IllegalArgumentException("Actor outbox may not be null");
        }
        this.inbox = new DSGActivityStreamsLink(inbox);
        this.outbox = new DSGActivityStreamsLink(outbox);
        this.preferredUsername = name;
    }

    /**
     * Initialize an ActivityPub actor.
     *
     * @param id     an URI identifying the actor.
     * @param name   the actor's name.
     * @param inbox  URI to the actor's inbox.
     * @param outbox URI to the actor's outbox.
     */
    public DSGActivityPubActor(URI id, String name, URI inbox, URI outbox) {
        super(id, name);
        if (inbox == null) {
            throw new IllegalArgumentException("Actor inbox may not be null");
        }
        if (outbox == null) {
            throw new IllegalArgumentException("Actor outbox may not be null");
        }
        this.inbox = new DSGActivityStreamsLink(inbox);
        this.outbox = new DSGActivityStreamsLink(outbox);
        this.preferredUsername = name;
    }

    /**
     * Parse an ActivityPub actor from its JSON representation.
     *
     * @param json the JSON object to parse.
     * @throws DSGJSONException if {@code json} is not an ActivityPub actor.
     */
    public DSGActivityPubActor(DSGJSONObject json) throws DSGJSONException {
        super(json);
        this.inbox = new DSGActivityStreamsLink(json.getMember(INBOX_KEY));
        this.outbox = new DSGActivityStreamsLink(json.getMember(OUTBOX_KEY));
        this.preferredUsername = json.getMemberAsString(PREFERRED_USERNAME_KEY);
    }

    /**
     * Get a link to the actor's inbox.
     *
     * @return a link pointing to the actor's inbox.
     */
    public DSGActivityStreamsLink getInbox() {
        return inbox;
    }

    /**
     * Set the link to the actor's inbox.
     *
     * @param inbox a link pointing to the actor's inbox.
     */
    public void setInbox(DSGActivityStreamsLink inbox) {
        this.inbox = inbox;
    }

    /**
     * Get a link to the actor's outbox.
     *
     * @return a link pointing to the actor's inbox.
     */
    public DSGActivityStreamsLink getOutbox() {
        return outbox;
    }

    /**
     * Set the link to the actor's outbox.
     *
     * @param outbox a link pointing to the actor's inbox.
     */
    public void setOutbox(DSGActivityStreamsLink outbox) {
        this.outbox = outbox;
    }

    /**
     * Get the user's preferred username.
     *
     * @return the users preferred username.
     */
    public String getPreferredUsername() {
        return preferredUsername;
    }

    /**
     * Set the user's preferred username.
     *
     * @param preferredUsername the users new preferred username.
     */
    public void setPreferredUsername(String preferredUsername) {
        this.preferredUsername = preferredUsername;
    }

    /**
     * Get authorization information for this actor.
     *
     * This is intended to be used in ActivityPub stubs.
     *
     * @return the actor's authorization information or null.
     */
    public String getAuthorization() {
        return authorization;
    }

    /**
     * Set authorization information for this actor.
     *
     * This is intended to be used in ActivityPub stubs.
     *
     * @param authorization the actor's authorization information.
     */
    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    @Override
    public DSGJSONObject toJSON() {
        DSGJSONObject json = super.toJSON();
        json.setMember(INBOX_KEY, inbox);
        json.setMember(OUTBOX_KEY, outbox);
        json.setMember(PREFERRED_USERNAME_KEY, preferredUsername);
        return json;
    }
}

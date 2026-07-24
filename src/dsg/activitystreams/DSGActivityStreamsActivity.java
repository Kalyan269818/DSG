package dsg.activitystreams;

import java.net.URI;

import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;

/**
 * Representation of the fact that an {@link DSGActivityStreamsActor} has
 * created a new {@link DSGActivityStreamsObject}.
 */
public class DSGActivityStreamsActivity extends DSGActivityStreamsObject {
    /**
     * Type name of a create activity. This is the only activity type we support.
     */
    public static final String TYPE_NAME = "Create";

    /** JSON object key for the activity's actor. */
    private static final String ACTOR_KEY = "actor";
    /** JSON object key for the activity's object. */
    private static final String OBJECT_KEY = "object";

    /**
     * The {@link DSGActivityStreamsActor} that created an object (e.g. a person,
     * organization, etc.).
     */
    private DSGActivityStreamsLink actor;

    /**
     * The direct object of an activity.
     *
     * For example, in the activity "John added a movie to his wishlist", the object
     * would be the movie added.
     */
    private DSGActivityStreamsObject object;

    /**
     * Initialize a new create activity.
     *
     * @param id     the unique identifier of the activity.
     * @param actor  the actor that created the object.
     * @param object the object that was created by the actor.
     */
    public DSGActivityStreamsActivity(URI id, DSGActivityStreamsActor actor, DSGActivityStreamsObject object) {
        super(id, TYPE_NAME);
        if (actor != null) {
            this.actor = new DSGActivityStreamsLink(actor.getId());
        } else {
            this.actor = null;
        }
        this.object = object;
    }

    /**
     * Try to parse an ActivityStreams activity from a JSON representation.
     *
     * @param json the JSON object to parse
     * @throws DSGJSONException if {@code json} is not a valid activity.
     */
    public DSGActivityStreamsActivity(DSGJSONObject json) throws DSGJSONException {
        super(json);
        if (!TYPE_NAME.equals(getType())) {
            throw new DSGJSONException(getType() + ": unsupported activity type");
        }
        if (json.hasMember(ACTOR_KEY)) {
            this.actor = new DSGActivityStreamsLink(json.getMemberAsString(ACTOR_KEY));
        }
        if (json.hasMember(OBJECT_KEY)) {
            this.object = new DSGActivityStreamsObject(json.getMemberAs(OBJECT_KEY));
        }
    }

    /**
     * Return the actor that created the object.
     *
     * @return the actor that created the object.
     */
    public DSGActivityStreamsLink getActor() {
        return actor;
    }

    /**
     * Set the actor that created the object.
     *
     * @param actor the id of the actor that created the object.
     */
    public void setActor(DSGActivityStreamsActor actor) {
        this.actor = new DSGActivityStreamsLink(actor.getId());
    }

    /**
     * Set the actor that created the object.
     *
     * @param actor the id of the actor that created the object.
     */
    public void setActor(String actor) {
        this.actor = new DSGActivityStreamsLink(actor);
    }

    /**
     * Set the actor that created the object.
     *
     * @param actor the id of the actor that created the object.
     */
    public void setActor(DSGActivityStreamsLink actor) {
        this.actor = actor;
    }

    /**
     * Return the object that was created.
     *
     * @return the created object.
     */
    public DSGActivityStreamsObject getObject() {
        return object;
    }

    /**
     * Set the create object that was created.
     *
     * @param object the object that has been created.
     */
    public void setObject(DSGActivityStreamsObject object) {
        this.object = object;
    }

    @Override
    public DSGJSONObject toJSON() {
        DSGJSONObject json = super.toJSON();
        json.setMember(ACTOR_KEY, actor);
        json.setMember(OBJECT_KEY, object);
        return json;
    }
}

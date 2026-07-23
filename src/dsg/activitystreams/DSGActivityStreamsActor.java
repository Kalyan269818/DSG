package dsg.activitystreams;

import java.net.URI;

import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;

/**
 * An Actor represents an entity that is capable of carrying out an
 * {@link DSGActivityStreamsActivity activity}.
 */
public class DSGActivityStreamsActor extends DSGActivityStreamsObject {
    /**
     * Type name of an actor that is a real person. This is the only actor type we
     * support right now.
     */
    public static final String TYPE_NAME = "Person";

    /**
     * Initialize an ActivityStreams actor.
     *
     * @param id   an URI identifying the actor.
     * @param name the actor's name.
     * @throws IllegalArgumentException if {@code type} is null.
     * @throws IllegalArgumentException if {@code id} is null.
     */
    public DSGActivityStreamsActor(URI id, String name) {
        super(id, TYPE_NAME);
        setName(name);
    }

    /**
     * Initialize an ActivityStreams actor.
     *
     * @param id   an URI identifying the actor.
     * @param name the actor's name.
     * @throws IllegalArgumentException if {@code type} is null.
     * @throws IllegalArgumentException if {@code id} is null or not a valid URI.
     */
    public DSGActivityStreamsActor(String id, String name) {
        super(id, TYPE_NAME);
        setName(name);
    }

    /**
     * Parse an ActivityStreams actor from its JSON representation.
     *
     * @param json the JSON object to parse.
     * @throws DSGJSONException if {@code json} is not an ActivityStreams actor.
     */
    public DSGActivityStreamsActor(DSGJSONObject json) throws DSGJSONException {
        super(json);
    }
}

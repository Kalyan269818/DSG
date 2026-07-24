package dsg.activitystreams;

import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;
import dsg.json.DSGJSONSerializable;
import dsg.json.DSGJSONValue;

/**
 * State shared by all ActivityStreams objects and links.
 */
public abstract class DSGActivityStreamsEntity implements DSGJSONSerializable {
    /** The JSON-LD context name for ActivityStreams. */
    public static final String LD_NAMESPACE = "https://www.w3.org/ns/activitystreams";
    /** JSON object key for the entity's JSON-LD's context. */
    private static final String LD_NAMESPACE_KEY = "@context";
    /** JSON object key for entity's type. */
    public static final String TYPE_KEY = "type";

    /** The entity's JSON-LD context. */
    private String namespace;
    /** The actual type of an ActivityStreams object or link. */
    private String type;

    /**
     * Initialize an ActivityStreams entity of the named {@code type}.
     *
     * @param type the entities type.
     * @throws IllegalArgumentException if {@code type} is null or empty.
     */
    public DSGActivityStreamsEntity(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.namespace = LD_NAMESPACE;
        this.type = type;
    }

    /**
     * Parse an ActivityStreams entity from its JSON representation.
     *
     * @param json the JSON object to parse.
     * @throws DSGJSONException if {@code json} is not an ActivityStreams entity.
     */
    public DSGActivityStreamsEntity(DSGJSONObject json) throws DSGJSONException {
        this.type = json.getMemberAsString(TYPE_KEY);
    }

    /**
     * Return the ActivityStreams object's or link's type.
     *
     * @return the object's actual type.
     */
    public String getType() {
        return type;
    }

    /**
     * Set the ActivityStream entity's type.
     *
     * @param type the entity's new type.
     * @throws IllegalArgumentException if {@code type} is null or empty.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get the ActivityStreams entity's JSON-LD context.
     *
     * @return the entity's JSON-LD context.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Set the ActivityStreams entity's JSON-LD context.
     *
     * @param context the entity's new JSON-LD context.
     */
    public void setNamespace(String context) {
        this.namespace = context;
    }

    @Override
    public DSGJSONValue toJSON() {
        DSGJSONObject object = new DSGJSONObject();
        object.setMember(LD_NAMESPACE_KEY, LD_NAMESPACE);
        object.setMember(TYPE_KEY, type);
        return object;
    }
}

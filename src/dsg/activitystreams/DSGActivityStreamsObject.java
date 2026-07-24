package dsg.activitystreams;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGStandardMediaTypes;
import dsg.json.DSGJSONArray;
import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;
import dsg.json.DSGJSONValue;

/**
 * An ActivityStreams Object.
 *
 * Similar to JAVA itself, in Activity Streams the primary base type is Object.
 * It defines a set of common properties (e.g., ID, name, and type) that are
 * shared by all other types that are derived from objects.
 *
 */
public class DSGActivityStreamsObject extends DSGActivityStreamsEntity {
    /** Type name of basic objects. */
    public static final String TYPE_NAME = "Object";

    /** JSON object key for the object's ID. */
    private static final String ID_KEY = "id";
    /** JSON object key for the object's attribution. */
    private static final String ATTRIBUTED_TO_KEY = "attributedTo";
    /** JSON object key for the object's content. */
    private static final String CONTENT_KEY = "content";
    /** JSON object key for the object's media type. */
    private static final String MEDIA_TYPE_KEY = "mediaType";
    /** JSON object key for the object's name. */
    private static final String NAME_KEY = "name";
    /** JSON object key for the object's reply to. */
    private static final String IN_REPLY_TO_KEY = "inReplyTo";
    /** JSON object key for the object's publication date. */
    private static final String PUBLISHED_KEY = "published";
    /** JSON object key for the object's summary. */
    private static final String SUMMARY_KEY = "summary";
    /** JSON object key for the object's primary recipients. */
    private static final String TO_KEY = "to";

    /** Global identifier of an object. */
    private URI id;

    /** The entity to which the object creation is attributed to. */
    private DSGActivityStreamsLink attributedTo;

    /** The object's main content. */
    private String content;

    /** The object content's media type. */
    private DSGHTTPMediaType mediaType;

    /** The object's human-readable name. */
    private String name;

    /** This is a reply to another entity (e.g. a post). */
    private DSGActivityStreamsLink inReplyTo;

    /** The date and time at which the object was published. */
    private Date published;

    /** Human-readable summarization of the object encoded as HTML. */
    private String summary;

    /** Entities considered to be part of the primary audience of an object. */
    private Collection<DSGActivityStreamsLink> to;

    /**
     * Initialize an object with a type of "Object".
     *
     * @param id an URI identifying the object.
     */
    public DSGActivityStreamsObject(URI id) {
        this(id, TYPE_NAME);
    }

    /**
     * Initialize an object with a type of "Object".
     *
     * @param id an URI identifying the object.
     * @throws IllegalArgumentException if {@code id} is not a valid URI.
     */
    public DSGActivityStreamsObject(String id) {
        this(id, TYPE_NAME);
    }

    /**
     * Initialize an object with the named {@code type}.
     *
     * @param id   an URI identifying the object.
     * @param type the object's type.
     * @throws IllegalArgumentException if {@code type} is null or empty.
     */
    public DSGActivityStreamsObject(URI id, String type) {
        super(type);
        this.id = id;
    }

    /**
     * Initialize an object with the named {@code type}.
     *
     * @param id   an URI identifying the object.
     * @param type the object's type.
     * @throws IllegalArgumentException if {@code type} is null or empty.
     * @throws IllegalArgumentException if {@code id} is not a valid URI.
     */
    public DSGActivityStreamsObject(String id, String type) {
        super(type);
        if (id == null) {
            this.id = null;
            return;
        }
        try {
            this.id = new URI(id);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException(use);
        }
    }

    /**
     * Parse an ActivityStreams object from its JSON representation.
     *
     * @param json the JSON object to parse.
     * @throws DSGJSONException if {@code json} is not an ActivityStreams object.
     */
    public DSGActivityStreamsObject(DSGJSONObject json) throws DSGJSONException {
        super(json);
        this.id = json.getMemberAsURI(ID_KEY);
        if (json.hasMember(ATTRIBUTED_TO_KEY)) {
            this.attributedTo = new DSGActivityStreamsLink(json.getMember(ATTRIBUTED_TO_KEY));
        }
        this.content = json.getMemberAsString(CONTENT_KEY);
        this.mediaType = json.getMemberAsMediaType(MEDIA_TYPE_KEY);
        this.name = json.getMemberAsString(NAME_KEY);
        if (json.hasMember(IN_REPLY_TO_KEY)) {
            this.inReplyTo = new DSGActivityStreamsLink(json.getMember(IN_REPLY_TO_KEY));
        }
        this.published = json.getMemberAsDate(PUBLISHED_KEY);
        this.summary = json.getMemberAsString(SUMMARY_KEY);
        if (json.hasMember(TO_KEY)) {
            this.to = new ArrayList<>();
            DSGJSONArray recipients = json.getMemberAs(TO_KEY);
            for (DSGJSONValue item : recipients) {
                this.to.add(new DSGActivityStreamsLink(item));
            }
        }
    }

    /**
     * Return the object's unique identifier.
     *
     * @return the object's ID.
     */
    public URI getId() {
        return id;
    }

    /**
     * Set the object's unique identifier.
     *
     * @param id the object's new ID.
     */
    public void setId(URI id) {
        this.id = id;
    }

    /**
     * Return, whether this object and {@code other} belong to the same server.
     *
     * @param other the compared object.
     * @return true if this object and {@code other} belong to the same server,
     *         false otherwise.
     */
    public boolean hasSameOrigin(DSGActivityStreamsObject other) {
        if (id == null || other.id == null) {
            return false;
        }
        return (id.getHost().equals(other.id.getHost()) && id.getPort() == other.id.getPort());
    }

    /**
     * Return, whether this object and {@code other} belong to the same server.
     *
     * @param other the compared object.
     * @return true if this object and {@code other} belong to the same server,
     *         false otherwise.
     */
    public boolean hasSameOrigin(DSGActivityStreamsLink other) {
        if (other == null) {
            return false;
        }
        return hasSameOrigin(other.getTarget());
    }

    /**
     * Return, whether this object and {@code other} belong to the same server.
     *
     * @param other the compared object.
     * @return true if this object and {@code other} belong to the same server,
     *         false otherwise.
     */
    public boolean hasSameOrigin(URI other) {
        if (id == null || other == null) {
            return false;
        }
        return (id.getHost().equals(other.getHost()) && id.getPort() == other.getPort());
    }

    /**
     * Get a link to the the owner of the object.
     *
     * @return a link to the object's owner.
     */
    public DSGActivityStreamsLink getAttributedTo() {
        return attributedTo;
    }

    /**
     * Set the object's owner.
     *
     * @param attributedTo link pointing to the owner of the object.
     */
    public void setAttributedTo(DSGActivityStreamsLink attributedTo) {
        this.attributedTo = attributedTo;
    }

    /**
     * Return, whether this object is attributed to the entity identified by
     * {@code id}.
     *
     * @param id the that should be checked.
     * @return true if this object is attributed to {@code id}, false otherwise.
     */
    public boolean isAttributedTo(DSGActivityStreamsLink id) {
        if (attributedTo == null || id == null) {
            return false;
        }
        return attributedTo.targetMatches(id);
    }

    /**
     * Return, whether this object is attributed to the given {@code object}.
     *
     * @param object the object that should be checked.
     * @return true if this object is attributed to {@code object}, false otherwise.
     */
    public boolean isAttributedTo(DSGActivityStreamsObject object) {
        if (object == null) {
            return false;
        }
        return isAttributedTo(new DSGActivityStreamsLink(object.id));
    }

    /**
     * Get the object's main content.
     *
     * @return the object's main content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Set the object's content, assuming it is encoded as HTML.
     *
     * @param content the HTML content of the object.
     */
    public void setContent(String content) {
        setContent(content, null);
    }

    /**
     * Set the content of the object, assuming it has the named media {@code type}.
     *
     * @param content the object's content.
     * @param type    the media type of {@code content}.
     */
    public void setContent(String content, DSGHTTPMediaType type) {
        // The AcitivityStreams specification states that if not specified, the content
        // property is assumed to contain text/html content.
        if (type == null) {
            type = DSGStandardMediaTypes.HTML;
        }
        this.content = content;
        this.mediaType = type;
    }

    /**
     * Get the media type of the object's {@code content} field.
     *
     * @return the object's media type.
     */
    public DSGHTTPMediaType getMediaType() {
        return this.mediaType;
    }

    /**
     * Get the object's name.
     *
     * @return the object's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the object's name.
     *
     * @param name the object's new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get a link to the object for which this object is considered to be a
     * response.
     *
     * @return the link to the object this object responds to.
     */
    public DSGActivityStreamsLink getInReplyTo() {
        return inReplyTo;
    }

    /**
     * Set that this object was created as a response to the object pointed to by
     * {@code inReplyTo}.
     *
     * @param inReplyTo link to the object this object is considered a response to.
     */
    public void setInReplyTo(DSGActivityStreamsLink inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    /**
     * Get the initial publication date of this object.
     *
     * @return the object's initial publication date.
     */
    public Date getPublished() {
        return published;
    }

    /**
     * Set the initial publication date of this object.
     *
     * @param published the date when this object was initially published.
     */
    public void setPublished(Date published) {
        this.published = published;
    }

    /**
     * Get a short summary of this object.
     *
     * @return this object's summary.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Set a short summary of this object.
     *
     * @param summary the new summary of the object.
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Get the list of recipients for this object.
     *
     * @return the list of recipients for this object.
     */
    public Collection<DSGActivityStreamsLink> getTo() {
        return to;
    }

    /**
     * Set the list of recipients for this object.
     *
     * @param to the new list of recipients for this object.
     */
    public void setTo(Collection<DSGActivityStreamsLink> to) {
        this.to = to;
    }

    /**
     * Return, whether the given link points to a recipient of this object.
     *
     * If {@code link} is null, then this function returns true only if this object
     * is addressed to the public collection.
     *
     * @param link the id of the entity.
     * @return true if {@code id} is a recipient of this message.
     */
    public boolean isRecipient(DSGActivityStreamsLink link) {
        if (to == null) {
            return false;
        }
        for (DSGActivityStreamsLink recipient : to) {
            // Objects addressed to the public collection can be received by everyone.
            if (recipient.isPublicAddress()) {
                return true;
            }
            if (link != null && recipient.targetMatches(link)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return, whether the given {@code actor} is a recipient of this object.
     *
     * @param actor an ActivityStreams actor.
     * @return true if {@code actor} is a recipient of this activity, false
     *         otherwise.
     */
    public boolean isRecipient(DSGActivityStreamsActor actor) {
        DSGActivityStreamsLink link;
        if (actor != null && actor.getId() != null) {
            link = new DSGActivityStreamsLink(actor.getId());
        } else {
            link = DSGActivityStreamsLink.PUBLIC_ADDRESSES.getFirst();
        }
        return isRecipient(link);
    }

    /**
     * Add the target of this link to the list of recipients of this object.
     *
     * @param id link pointing to the desired recipient.
     */
    public void addRecipient(DSGActivityStreamsLink id) {
        if (id == null) {
            return;
        }
        if (to == null) {
            to = new ArrayList<>();
        }
        to.add(id);
    }

    @Override
    public DSGJSONObject toJSON() {
        DSGJSONObject json = (DSGJSONObject) super.toJSON();
        json.setMember(ID_KEY, id);
        json.setMember(ATTRIBUTED_TO_KEY, attributedTo);
        json.setMember(CONTENT_KEY, content);
        json.setMember(MEDIA_TYPE_KEY, mediaType);
        json.setMember(NAME_KEY, name);
        json.setMember(IN_REPLY_TO_KEY, inReplyTo);
        json.setMember(PUBLISHED_KEY, published);
        json.setMember(SUMMARY_KEY, summary);
        if (to == null) {
            return json;
        }
        DSGJSONArray recipients = new DSGJSONArray();
        for (DSGActivityStreamsLink recipient : to) {
            recipients.add(recipient);
        }
        json.setMember(TO_KEY, recipients);
        return json;
    }
}

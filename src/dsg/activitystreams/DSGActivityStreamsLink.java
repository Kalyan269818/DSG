package dsg.activitystreams;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import dsg.http.DSGHTTPMediaType;
import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;
import dsg.json.DSGJSONString;
import dsg.json.DSGJSONValue;

/**
 * An ActivityStreams Link.
 *
 * Links are the second base type of the ActivityStreams specification. They
 * describe a reference to another resource. However, links in ActivityStreams
 * are not mere references, but can contain additional hints how to make use of
 * the resource.
 */
public class DSGActivityStreamsLink extends DSGActivityStreamsEntity {
    /** Type name for links. */
    public static final String TYPE_NAME = "Link";

    /**
     * Pseudo-ID of the public collection which is used for publicly visible posts.
     */
    public static final List<DSGActivityStreamsLink> PUBLIC_ADDRESSES = Arrays.asList(
            new DSGActivityStreamsLink("https://www.w3.org/ns/activitystreams#Public"),
            new DSGActivityStreamsLink("as:Public"), new DSGActivityStreamsLink("Public"));

    /** JSON object key for the link's target. */
    private static final String HREF_KEY = "href";
    /** JSON object key for the link's media type. */
    private static final String MEDIA_TYPE_KEY = "mediaType";
    /** JSON object key for the name. */
    private static final String NAME_KEY = "name";

    /** The link's target URI. */
    private URI href;

    /** The media type of the referenced resource. */
    private DSGHTTPMediaType mediaType;

    /** Human-readable name for the link. */
    private String name;

    /**
     * Initialize a link pointing to the given reference.
     *
     * @param href the target URI of the link.
     *
     * @throws IllegalArgumentException if {@code uri} is null or not a valid URI.
     */
    public DSGActivityStreamsLink(String href) {
        super(TYPE_NAME);
        setTarget(href);
    }

    /**
     * Initialize a link pointing to the given reference.
     *
     * @param href the target URI of the link.
     *
     * @throws IllegalArgumentException if {@code uri} is null
     */
    public DSGActivityStreamsLink(URI href) {
        super(TYPE_NAME);
        setTarget(href);
    }

    /**
     * Initialize a link from another link.
     *
     * @param link the original link.
     */
    public DSGActivityStreamsLink(DSGActivityStreamsLink link) {
        super(link.getType());
        this.href = link.href;
        this.mediaType = link.mediaType;
        this.name = link.name;
    }

    /**
     * Parse an ActivityStreams link from its JSON representation.
     *
     * @param json the JSON object to parse.
     * @throws DSGJSONException if {@code json} is not an ActivityStreams link.
     */
    public DSGActivityStreamsLink(DSGJSONValue json) throws DSGJSONException {
        super(TYPE_NAME);
        if (json instanceof DSGJSONString) {
            this.href = json.asURI();
            return;
        }
        DSGJSONObject object = json.as();
        this.href = object.getMemberAsURI(HREF_KEY);
        this.mediaType = object.getMemberAsMediaType(MEDIA_TYPE_KEY);
        this.name = object.getMemberAsString(NAME_KEY);
    }

    /**
     * Initialize an ActivityStreams link that points to {@code uri}.
     *
     * @param type the Link's ActivityStreams type.
     * @param href the target URI of the link.
     * @throws IllegalArgumentException if {@code} type is null.
     * @throws IllegalArgumentException if {@code uri} is null.
     */
    protected DSGActivityStreamsLink(String type, URI href) {
        super(type);
        setTarget(href);
    }

    /**
     * Initialize an ActivityStreams link that points to {@code uri}.
     *
     * @param type the Link's ActivityStreams type.
     * @param href the target URI of the link.
     * @throws IllegalArgumentException if {@code} type is null.
     * @throws IllegalArgumentException if {@code uri} is null or not a valid URI.
     */
    protected DSGActivityStreamsLink(String type, String href) {
        super(type);
        setTarget(href);
    }

    /**
     * Get the link's target.
     *
     * @return the link's target.
     */
    public URI getTarget() {
        return href;
    }

    /**
     * Return, whether this link points to a target on the server of {@code object}.
     *
     * @param object the object to compare origins with.
     * @return true if this link points to a target on {@code other}'s server, false
     *         otherwise.
     */
    public boolean hasSameOrigin(DSGActivityStreamsObject object) {
        if (object == null) {
            return false;
        }
        URI oid = object.getId();
        return hasSameOrigin(oid);
    }

    /**
     * Return, whether this object and {@code other} belong to the same server.
     *
     * @param other the link to compare origins with.
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
     * @param uri the URI to compare origins with.
     * @return true if this object and {@code other} belong to the same server,
     *         false otherwise.
     */
    public boolean hasSameOrigin(URI uri) {
        if (href == null || uri == null) {
            return false;
        }
        return (href.getHost().equals(uri.getHost()) && href.getPort() == uri.getPort());
    }

    /**
     * Set the links target to {@code href}.
     *
     * @param href the link's new target.
     */
    public void setTarget(URI href) {
        if (href == null) {
            throw new IllegalArgumentException("href must not be null");
        }
        this.href = href;
    }

    /**
     * Set the links target to {@code href}.
     *
     * @param href the link's new target.
     * @throws IllegalArgumentException if {@code href} is not a valid URI.
     */
    public void setTarget(String href) {
        try {
            this.href = new URI(href);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException(use);
        }
    }

    /**
     * Return, whether this link's target matches {@code other}'s target.
     *
     * @param other the link with which to compare this link.
     * @return true if {@code this} and {@code other} have the same href.
     */
    public boolean targetMatches(DSGActivityStreamsLink other) {
        if (other == null) {
            return false;
        }
        return href.equals(other.href);
    }

    /**
     * Return, whether this link's target matches {@code uri}.
     *
     * @param uri the uri with which to compare this link.
     * @return true if this link's target and {@code uri} match, false otherwise.
     */
    public boolean targetMatches(URI uri) {
        return href.equals(uri);
    }

    /**
     * Get the media type of the link's target resource.
     *
     * @return the media type of the link's target resource.
     */
    public DSGHTTPMediaType getMediaType() {
        return mediaType;
    }

    /**
     * Set the media type of the link's target resource.
     *
     * @param mediaType the new media type of the link's target resource.
     */
    public void setMediaType(DSGHTTPMediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Get the link target's name.
     *
     * @return the name of the link's target resource.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the link target resource's name.
     *
     * @param name the new name of the link's target resource.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return, whether this link points to the public collection.
     *
     * @return true if this link's target points to the public collection, false
     *         otherwise.
     */
    public boolean isPublicAddress() {
        for (DSGActivityStreamsLink publicAddress : PUBLIC_ADDRESSES) {
            if (publicAddress.targetMatches(this)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return href.toString();
    }

    @Override
    public DSGJSONValue toJSON() {
        // According to the specification, if only the link's href attribute is set, we
        // can simply serialize it to a string.
        if (getType().equals(TYPE_NAME) && mediaType == null && name == null) {
            return new DSGJSONString(href.toASCIIString());
        }

        DSGJSONObject value = (DSGJSONObject) super.toJSON();
        value.setMember(HREF_KEY, href.toASCIIString());
        value.setMember(MEDIA_TYPE_KEY, mediaType.toString());
        value.setMember(NAME_KEY, name);
        return value;
    }
}

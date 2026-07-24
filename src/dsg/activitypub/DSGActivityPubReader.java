package dsg.activitypub;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import dsg.activitystreams.DSGActivityStreamsActor;
import dsg.activitystreams.DSGActivityStreamsCollection;
import dsg.activitystreams.DSGActivityStreamsEntity;
import dsg.activitystreams.DSGActivityStreamsLink;
import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.json.DSGJSONArray;
import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;
import dsg.json.DSGJSONReader;
import dsg.json.DSGJSONString;
import dsg.json.DSGJSONValue;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRepresentation;

/**
 * A reader that deserializes JSON-encoded ActivityStreams objects.
 */
public class DSGActivityPubReader implements AutoCloseable {

    /** The JSON reader from which ActivityStreams object are read. */
    private DSGJSONReader reader;

    /**
     * Initialize a reader from a REST representation.
     *
     * @param representation the REST representation to read object from.
     * @throws DSGRESTException if the representation is not an ActivityPub stream.
     */
    public DSGActivityPubReader(DSGRESTRepresentation representation) throws DSGRESTException {
        this(representation.getResource(), representation.getType().getCharset());
    }

    /**
     * Initialize a reader from a byte stream and its charset.
     *
     * @param stream  the JSON byte-stream.
     * @param charset the stream's charset.
     */
    public DSGActivityPubReader(InputStream stream, Charset charset) {
        this.reader = new DSGJSONReader(stream, charset);
    }

    /**
     * Initialize a reader from a byte stream using the default charset.
     *
     * @param stream the JSON byte-stream.
     */
    public DSGActivityPubReader(InputStream stream) {
        this.reader = new DSGJSONReader(stream, StandardCharsets.UTF_8);
    }

    /**
     * Read the next ActivityStreams object from the JSON stream.
     *
     * @return the next object from the JSON stream.
     * @throws DSGJSONException if the parsed JSON is not an ActivityStreams object.
     * @throws IOException      if an error occurrs while reading from the JSON
     *                          stream.
     */
    public DSGActivityStreamsObject read() throws DSGJSONException, IOException {
        DSGJSONObject json = reader.readObject();
        if (!json.hasMember(DSGActivityStreamsEntity.TYPE_KEY)) {
            throw new DSGJSONException("JSON object does not have a type");
        }
        return parseJSON(json);
    }

    /**
     * Deserialize an ActivityStreamsObject from an existing JSON object.
     *
     * @param json the JSON representation of an ActivityStreams object
     * @return the parsed ActivityStreams object.
     * @throws DSGJSONException if {@code json} is not an ActivityStreams object.
     */
    public static DSGActivityStreamsObject parseJSON(DSGJSONObject json) throws DSGJSONException {
        String type = json.getMemberAsString(DSGActivityStreamsEntity.TYPE_KEY);
        Class<? extends DSGActivityStreamsEntity> collectionClass;
        switch (type) {
        case DSGActivityStreamsActivity.TYPE_NAME:
            return new DSGActivityStreamsActivity(json);
        case DSGActivityStreamsActor.TYPE_NAME:
            return new DSGActivityPubActor(json);
        case DSGActivityStreamsCollection.COLLECTION_TYPE_NAME:
            collectionClass = getArrayItemType(json, "items");
            return new DSGActivityStreamsCollection<>(json, collectionClass);
        case DSGActivityStreamsCollection.ORDERED_COLLECTION_TYPE_NAME:
            collectionClass = getArrayItemType(json, "orderedItems");
            return new DSGActivityStreamsCollection<>(json, collectionClass);
        default:
            // Fallback to object in all other cases.
            return new DSGActivityStreamsObject(json);
        }
    }

    private static Class<? extends DSGActivityStreamsEntity> getArrayItemType(DSGJSONObject json, String memberName)
            throws DSGJSONException {
        if (!json.hasMember(memberName)) {
            return DSGActivityStreamsObject.class;
        }
        DSGJSONArray items = json.getMemberAs(memberName);
        if (items.size() == 0) {
            return DSGActivityStreamsObject.class;
        }
        DSGJSONValue value = items.stream().findFirst().get();
        if (value instanceof DSGJSONString) {
            return DSGActivityStreamsLink.class;
        }
        if (value instanceof DSGJSONObject) {
            DSGActivityStreamsObject o = parseJSON((DSGJSONObject) value);
            return o.getClass();
        }
        throw new DSGJSONException("Cannot parse " + value.getType() + " into ActivityStreams object");
    }

    @Override
    public void close() throws IOException {
        this.reader.close();
    }
}

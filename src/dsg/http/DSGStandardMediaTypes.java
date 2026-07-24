package dsg.http;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Constant definitions for standard media types that we will use over the
 * course of the semester.
 */
public class DSGStandardMediaTypes {
    /** Media type for ActivityStreams objects. */
    public static final DSGHTTPMediaType ACTIVITY_STREAMS;
    /** Special media type that accepts any other media type. */
    public static final DSGHTTPMediaType ANY;
    /** Media type for BMP images. */
    public static final DSGHTTPMediaType BMP;
    /** Media type for CSS documents. */
    public static final DSGHTTPMediaType CSS;
    /** Default media type if none is given */
    public static final DSGHTTPMediaType DEFAULT;
    /** Media type for multipart HTML forms */
    public static final DSGHTTPMediaType FORM_MULTIPART;
    /** Media type for URL encoded HTML forms */
    public static final DSGHTTPMediaType FORM_URL_ENCODED;
    /** Media type for HTML documents. */
    public static final DSGHTTPMediaType HTML;
    /** Media type for JavaScript source code. */
    public static final DSGHTTPMediaType JAVA_SCRIPT;
    /** Media type for serialized Java objects */
    public static final DSGHTTPMediaType JAVA_SERIALIZED;
    /** Media type for JPEG images. */
    public static final DSGHTTPMediaType JPEG;
    /** Media type for JSON documents. */
    public static final DSGHTTPMediaType JSON;
    /** Media type for JSON-LD documents. */
    public static final DSGHTTPMediaType JSON_LD;
    /** Media type for plaintext. */
    public static final DSGHTTPMediaType PLAINTEXT;
    /** Media type for PNG images. */
    public static final DSGHTTPMediaType PNG;

    private static final Map<String, DSGHTTPMediaType> extensionTypes = new HashMap<>();
    private static final Map<String, String> typeExtensions = new HashMap<>();

    static {
        ACTIVITY_STREAMS = new DSGHTTPMediaType("application", "activity+json", StandardCharsets.UTF_8);
        ANY = new DSGHTTPMediaType(DSGHTTPMediaType.MEDIA_TYPE_WILDCARD, DSGHTTPMediaType.MEDIA_TYPE_WILDCARD, null);
        BMP = new DSGHTTPMediaType("image", "bmp", null);
        CSS = new DSGHTTPMediaType("text", "css", null);
        DEFAULT = new DSGHTTPMediaType("application", "octet-stream", null);
        FORM_MULTIPART = new DSGHTTPMediaType("multipart", "form-data", null);
        FORM_URL_ENCODED = new DSGHTTPMediaType("application", "x-www-form-urlencoded", null);
        HTML = new DSGHTTPMediaType("text", "html", null);
        JAVA_SCRIPT = new DSGHTTPMediaType("text", "javascript", null);
        JAVA_SERIALIZED = new DSGHTTPMediaType("application", "x-java-serialized-object", null);
        JPEG = new DSGHTTPMediaType("image", "jpeg", null);
        JSON = new DSGHTTPMediaType("application", "json", StandardCharsets.UTF_8);
        JSON_LD = new DSGHTTPMediaType("application", "ld+json", StandardCharsets.UTF_8);
        PLAINTEXT = new DSGHTTPMediaType("text", "plain", null);
        PNG = new DSGHTTPMediaType("image", "png", null);

        extensionTypes.put(".bin", DEFAULT);
        typeExtensions.put(DEFAULT.getFullType(), ".bin");

        extensionTypes.put(".txt", PLAINTEXT);
        typeExtensions.put(PLAINTEXT.getFullType(), ".txt");

        extensionTypes.put(".htm", HTML);
        extensionTypes.put(".html", HTML);
        typeExtensions.put(HTML.getFullType(), ".htm");

        extensionTypes.put(".css", CSS);
        typeExtensions.put(CSS.getFullType(), ".css");

        extensionTypes.put(".js", JAVA_SCRIPT);
        typeExtensions.put(JAVA_SCRIPT.getFullType(), ".js");

        extensionTypes.put(".json", JSON);
        typeExtensions.put(JSON.getFullType(), ".json");

        extensionTypes.put(".jsonld", JSON_LD);
        typeExtensions.put(JSON_LD.getFullType(), ".jsonld");
        // There exists no dedicated activity stream file extension.
        typeExtensions.put(ACTIVITY_STREAMS.getFullType(), ".jsonld");

        extensionTypes.put(".bmp", BMP);
        typeExtensions.put(BMP.getFullType(), ".bmp");

        extensionTypes.put(".jpg", JPEG);
        extensionTypes.put(".jpeg", JPEG);
        typeExtensions.put(JPEG.getFullType(), ".jpg");

        extensionTypes.put(".png", PNG);
        typeExtensions.put(PNG.getFullType(), ".png");
    }

    /**
     * Return the default file extension for the given media type.
     *
     * @param type the media type for which to return an extension.
     * @return the file extension including the leading dot or an empty string, if
     *         no extension is associated with the type.
     */
    public static String extensionForType(DSGHTTPMediaType type) {
        if (type == null) {
            return "";
        }
        return typeExtensions.get(type.getFullType());
    }

    /**
     * Return the default type for the given file extension.
     *
     * If no media type is associated with {@code extension}, then the default media
     * type (application/octet-stream) is returned.
     *
     * @param extension a file extension.
     * @return the media type associated with the extension.
     */
    public static DSGHTTPMediaType typeForExtension(String extension) {
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        if (!extensionTypes.containsKey(extension)) {
            return DEFAULT;
        }
        return extensionTypes.get(extension);
    }

    // Private constructor to prevent object instantiation.
    private DSGStandardMediaTypes() {
        throw new AssertionError("You should not instantiate this class");
    }
}

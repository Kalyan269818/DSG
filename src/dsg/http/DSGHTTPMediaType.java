package dsg.http;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

/**
 * The parsed version of a MIME media type as contained in the Content-Type and
 * Accept HTTP headers.
 */
public class DSGHTTPMediaType {
    /** Wildcard meaning any type or sub-type is acceptable. */
    public static final String MEDIA_TYPE_WILDCARD = "*";

    /**
     * Default charset used when no charset is set explicitly.
     *
     * According to RFC 2046, the default character set, which must be assumed in
     * the absence of a charset parameter, is US-ASCII.
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.US_ASCII;
    /** Name of the default charset. */
    public static final String DEFAULT_CHARSET_NAME = "US-ASCII";

    private static final String SUB_TYPE_SEPARATOR = "/";
    private static final String PARAMETER_SEPARATOR = ";";
    private static final String PARAMETER_VALUE_SEPARATOR = "=";

    private String type;
    private String subType;
    private Charset charset;

    /**
     * Create a new instance using the default HTTP media type.
     */
    public DSGHTTPMediaType() {
        type = "application";
        subType = "octet-stream";
        charset = null;
    }

    /**
     * Parse a media type from a string.
     *
     * @param contentType string-encoded HTTP media-type.
     * @throws DSGHTTPException            if {@code contentType} is not a valid
     *                                     HTTP media-type.
     * @throws UnsupportedCharsetException If the Java Virtual Machine does not
     *                                     support this charset.
     * @throws IllegalCharsetNameException If the given charset name contains
     *                                     illegal characters.
     */
    public DSGHTTPMediaType(String contentType) throws DSGHTTPException {
        parse(contentType);
    }

    /**
     * Create a new instance using the given {@code type} {@code subType}, and
     * {@code charset}.
     *
     * @param type    the main type (e.g. "text").
     * @param subType the sub type (e.g. "plain")
     * @param charset an (optional) charset of the media type.
     */
    protected DSGHTTPMediaType(String type, String subType, Charset charset) {
        this.type = type;
        this.subType = subType;
        this.charset = charset;
    }

    /**
     * Return the media-type's top-level type.
     *
     * @return the top-level type.
     */
    public String getType() {
        return type;
    }

    /**
     * Return the media-type's sub-type, or null, if it was not defined.
     *
     * @return the media-type's sub-type or null.
     */
    public String getSubType() {
        return subType;
    }

    /**
     * Return the full type part of this media type.
     *
     * @return the full type part in the form type + "/" + sub-type, excluding the
     *         charset.
     */
    public String getFullType() {
        if (subType == null) {
            return type;
        }
        return type + SUB_TYPE_SEPARATOR + subType;
    }

    /**
     * Return, whether this media type instance has an explicit charset.
     *
     * @return true if the charset has been set explicitly, false otherwise.
     */
    public boolean hasCharset() {
        return charset != null;
    }

    /**
     * Return this media type's charset.
     *
     * As a convenience, when this media type's main type is "text" and no charset
     * is explicitly set, the default charset (US-ASCII) is returned.
     *
     * @return the media type's charset or null, if it does not have one.
     */
    public Charset getCharset() {
        if (charset == null) {
            return DEFAULT_CHARSET;
        }
        return charset;
    }

    /**
     * Return, whether this media type accepts the {@code other} media type.
     *
     * A media type is accepted by another type if all its components match or if
     * this media type contains a wildcard character '*'.
     *
     * @param other the media type to check for acceptance.
     * @return true if this media type is accepted by {@code other}.
     */
    public boolean accepts(DSGHTTPMediaType other) {
        if (!type.equals(MEDIA_TYPE_WILDCARD) && !type.equals(other.type)) {
            return false;
        }
        if (!subType.equals(MEDIA_TYPE_WILDCARD) && !subType.equals(other.subType)) {
            return false;
        }
        return (charset == null || charset.equals(other.charset));
    }

    /**
     * Returns a copy of this media type, replacing the current charset with the
     * given one.
     *
     * @param charset the desired charset.
     * @return a copy of this media type with the given charset.
     */
    public DSGHTTPMediaType withCharset(Charset charset) {
        return new DSGHTTPMediaType(type, subType, charset);
    }

    /**
     * Return, whether this media type and {@code other} are equal.
     *
     * Two media types are considered equal, if their type, subtype and charset are
     * the same.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DSGHTTPMediaType)) {
            return false;
        }
        DSGHTTPMediaType o = (DSGHTTPMediaType) other;
        if (charset == null) {
            if (o.charset != null) {
                return false;
            }
            return type.equals(o.type) && subType.equals(o.subType);
        }
        return type.equals(o.type) && subType.equals(o.subType) && charset.equals(o.charset);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(type);
        if (subType != null) {
            builder.append(SUB_TYPE_SEPARATOR);
            builder.append(subType);
        }
        if (charset != null) {
            builder.append(PARAMETER_SEPARATOR);
            builder.append("charset");
            builder.append(PARAMETER_VALUE_SEPARATOR);
            // Media-type names are usually lower case in MIME headers.
            builder.append(charset.name().toLowerCase());
        }
        return builder.toString();
    }

    private void parse(String contentType) throws DSGHTTPException {
        String[] parts = contentType.split(PARAMETER_SEPARATOR);
        parseType(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            parseParameter(parts[i]);
        }
    }

    private void parseType(String type) throws DSGHTTPException {
        String[] parts = type.split(SUB_TYPE_SEPARATOR);
        this.type = parts[0].trim();
        if (parts.length == 1) {
            return;
        }
        this.subType = parts[1].trim();
    }

    private void parseParameter(String parameter) throws DSGHTTPException {
        String[] parts = parameter.split(PARAMETER_VALUE_SEPARATOR);
        if (parts.length != 2) {
            throw new DSGHTTPException(parameter + ": not a valid parameter");
        }

        String key = parts[0].trim();
        String value = parts[1].trim();
        // Charset is the only available parameter of a media-type.
        if (!key.equals("charset")) {
            return;
        }

        // The charset parameter value is case-insensitive, and JAVA usually uses
        // uppercase letters.
        value = value.toUpperCase();
        this.charset = Charset.forName(value);
    }
}

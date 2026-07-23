package dsg.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPRequest;
import dsg.http.DSGHTTPResponse;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGHTTPValues;
import dsg.http.DSGStandardMediaTypes;

/**
 * A representation of a {@link DSGRESTResource} that can be transferred over
 * the network.
 */
public class DSGRESTRepresentation {
    /** The HTTP header field name storing the content type. */
    protected static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";

    private final DSGHTTPHeader header;
    private final InputStream resource;
    private final DSGHTTPMediaType type;
    private final DSGHTTPStatus status;

    /**
     * Initialize a REST representation with the default status.
     *
     * @param type     the representation's media type.
     * @param resource a byte stream representation of a REST resource.
     */
    public DSGRESTRepresentation(DSGHTTPMediaType type, InputStream resource) {
        this(DSGHTTPStatus.OK, type, resource);
    }

    /**
     * Initialize a REST representation that includes an explicit {@code status}.
     *
     * The status is only meaningful when returned by a REST skeleton
     * implementation.
     *
     * @param status   the status of the representation.
     * @param type     the media type of {@code resource}.
     * @param resource a byte stream representation of a REST resource.
     * @throws IllegalArgumentException if {@code resource} without a {@code type}
     *                                  is specified.
     */
    public DSGRESTRepresentation(DSGHTTPStatus status, DSGHTTPMediaType type, InputStream resource) {
        if (resource != null && type == null) {
            throw new IllegalArgumentException("type must not be null if a resource exists");
        }
        this.header = new DSGHTTPHeader();
        if (type != null) {
            this.header.add(CONTENT_TYPE_HEADER_NAME, type.toString());
        }
        this.resource = resource;
        this.type = type;
        // Default to OK status.
        if (status == null) {
            this.status = DSGHTTPStatus.OK;
        } else {
            this.status = status;
        }
    }

    /**
     * Initialize a REST representation with an explicit status, but no payload.
     *
     * @param status the status of the representation.
     */
    public DSGRESTRepresentation(DSGHTTPStatus status) {
        this(status, (DSGHTTPMediaType) null, (InputStream) null);
    }

    /**
     * Initialize a REST representation of a string.
     *
     * @param resource the string representation of the resource.
     */
    public DSGRESTRepresentation(String resource) {
        this(DSGHTTPStatus.OK, resource);
    }

    /**
     * Initialize a REST representation of a string with an explicit status.
     *
     * The string will be encoded using UTF-8.
     *
     * @param status   the status of the representation.
     * @param resource the string representation of the resource.
     */
    public DSGRESTRepresentation(DSGHTTPStatus status, String resource) {
        this(status, DSGStandardMediaTypes.PLAINTEXT.withCharset(StandardCharsets.UTF_8),
                new ByteArrayInputStream(resource.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Initialize a REST representation from a HTTP form.
     *
     * @param form the form's values.
     */
    public DSGRESTRepresentation(DSGHTTPValues form) {
        byte[] serialized = form.encode().getBytes(StandardCharsets.UTF_8);
        DSGHTTPMediaType type = DSGStandardMediaTypes.FORM_URL_ENCODED.withCharset(StandardCharsets.UTF_8);
        ByteArrayInputStream resource = new ByteArrayInputStream(serialized);
        this(type, resource);
    }

    /**
     * Initialize a REST representation from an HTTP request.
     *
     * @param request the HTTP request.
     * @throws DSGHTTPException if the request's content type is invalid.
     */
    protected DSGRESTRepresentation(DSGHTTPRequest request) throws DSGHTTPException {
        if (request == null) {
            throw new IllegalArgumentException("request may not be null");
        }
        this(DSGHTTPStatus.OK, request.getHeader(), request.getBody());
    }

    /**
     * Initialize a REST representation from an HTTP response.
     *
     * @param response the HTTP response.
     * @throws DSGHTTPException         if the response's content type is invalid.
     * @throws IllegalArgumentException if {@code response} is null.
     * @throws IllegalArgumentException if the response has a body, but its header
     *                                  does not define a content type.
     */
    protected DSGRESTRepresentation(DSGHTTPResponse response) throws DSGHTTPException {
        if (response == null) {
            throw new IllegalArgumentException("response may not be null");
        }
        this(response.getStatus(), response.getHeader(), response.getBody());
    }

    /**
     * Initialize a REST representation from HTTP data.
     *
     * @param status   the HTTP status code.
     * @param header   the HTTP header.
     * @param resource a byte stream representation of a REST resource.
     * @throws DSGHTTPException         if the header's content type field is
     *                                  invalid.
     * @throws IllegalArgumentException if the header does not contain a content
     *                                  type, although a resource is specified.
     */
    protected DSGRESTRepresentation(DSGHTTPStatus status, DSGHTTPHeader header, InputStream resource)
            throws DSGHTTPException {
        String contentType = header.get(CONTENT_TYPE_HEADER_NAME);
        if (contentType != null && !contentType.isBlank()) {
            this.type = new DSGHTTPMediaType(contentType);
        } else {
            this.type = null;
        }
        if (resource != null && type == null) {
            throw new IllegalArgumentException("type must not be null if a resource exists");
        }
        this.header = header;
        this.resource = resource;
        // Default to OK status.
        if (status == null) {
            this.status = DSGHTTPStatus.OK;
        } else {
            this.status = status;
        }
    }

    /**
     * Return the status associated with this representation.
     *
     * @return the status or null, if the representation does not have a status.
     */
    public DSGHTTPStatus getStatus() {
        return status;
    }

    /**
     * Return the HTTP header associated with this representation.
     *
     * @return the header or null, if no header is associated with this
     *         representation.
     */
    public DSGHTTPHeader getHeader() {
        return header;
    }

    /**
     * Return the representation's media type.
     *
     * @return The representation's type.
     */
    public DSGHTTPMediaType getType() {
        return type;
    }

    /**
     * Return this representation's raw byte stream.
     *
     * @return the raw representation payload.
     */
    public InputStream getResource() {
        return resource;
    }

    /**
     * Wraps and returns the representation's byte stream into a reader with the
     * correct charset.
     *
     * @return a reader that uses this representation's charset or null if the
     *         representation contains no resource payload.
     */
    public Reader getResourceReader() {
        if (resource == null) {
            return null;
        }
        return new InputStreamReader(resource, type.getCharset());
    }

    /**
     * Read this representation's payload into a string.
     *
     * @return the representation's payload.
     * @throws IOException if reading the representation payload fails.
     */
    public String readAllAsString() throws IOException {
        Reader reader = getResourceReader();
        if (reader == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        char[] buf = new char[4096];
        int bytesRead = 0;
        while ((bytesRead = reader.read(buf)) != -1) {
            result.append(buf, 0, bytesRead);
        }
        return result.toString();
    }
}

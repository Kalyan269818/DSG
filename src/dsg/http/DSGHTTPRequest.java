package dsg.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Encoder;

import dsg.network.DSGMessage;

/**
 * HTTP request sent by a client.
 */
public class DSGHTTPRequest implements DSGMessage {
    private DSGHTTPMethod method;
    private URI target;
    private String version;
    private DSGHTTPHeader header;
    private InputStream body;

    /**
     * Initialize an empty request.
     *
     * This constructor should only be used for deserializing requests.
     */
    protected DSGHTTPRequest() {
        this.method = null;
        this.target = null;
        this.version = null;
        this.header = null;
        this.body = null;
    }

    /**
     * Initialize a HTTP request.
     *
     * @param method the request's method.
     * @param header the request's list of header fields.
     * @param target the request's target resource.
     * @param body   an (optional) request body.
     */
    public DSGHTTPRequest(DSGHTTPMethod method, DSGHTTPHeader header, URI target, InputStream body) {
        if (method == null) {
            throw new IllegalArgumentException("HTTP method must not be null");
        }
        if (header == null) {
            throw new IllegalArgumentException("HTTP header must not be null");
        }
        if (target.getScheme() != null && !target.getScheme().equals("http")) {
            throw new IllegalArgumentException("Request URL does not use the HTTP protocol.");
        }

        this.method = method;
        this.target = target;
        this.version = "HTTP/1.1";
        this.header = header;
        this.body = body;

        // The HTTP 1.1 specification states that the Host header must always be set.
        int port = target.getPort();
        if (port == -1) {
            port = 80;
        }
        this.header.set("Host", target.getHost() + ":" + port);

        // Transform user info into basic auth credentials.
        String userInfo = target.getUserInfo();
        if (userInfo != null && !userInfo.isBlank()) {
            Encoder encoder = Base64.getEncoder();
            String credentials = encoder.encodeToString(userInfo.getBytes(StandardCharsets.US_ASCII));
            this.header.set("Authorization", "Basic " + credentials);
        }

        String contentType = this.header.get("Content-Type");
        if (body == null) {
            this.header.set("Content-Length", "0");
        } else if (contentType == null || contentType.isEmpty()) {
            // Set the default encoding.
            this.header.set("Content-Type", "application/octet-stream");
        }

        // Let the server know that we do not support content compression etc.
        this.header.set("Accept-Encoding", "identity");

        String userAgent = this.header.get("User-Agent");
        if (userAgent == null || userAgent.isEmpty()) {
            this.header.set("User-Agent", "DSG HTTP Library");
        }
    }

    /**
     * Initialize an HTTP request with the default set of header fields.
     *
     * @param method the request's method.
     * @param target the request's target resource.
     * @param body   an (optional) request body.
     */
    public DSGHTTPRequest(DSGHTTPMethod method, URI target, InputStream body) {
        this(method, new DSGHTTPHeader(), target, body);
    }

    /**
     * Initialize an HTTP request without a request body.
     *
     * @param method the request's method.
     * @param target the request's target resource.
     */
    public DSGHTTPRequest(DSGHTTPMethod method, URI target) {
        this(method, new DSGHTTPHeader(), target, null);
    }

    /**
     * Return this request's method.
     *
     * @return the request method.
     */
    public DSGHTTPMethod getMethod() {
        return this.method;
    }

    /**
     * Return this request's target resource.
     *
     * @return the target resource.
     */
    public URI getTarget() {
        return this.target;
    }

    /**
     * Parse and return the request target's query element.
     *
     * @return the request target's query element.
     */
    public DSGHTTPValues getQuery() {
        return new DSGHTTPValues(target);
    }

    /**
     * Get this request's HTTP version.
     *
     * @return the HTTP version (e.g. "HTTP/1.1").
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Get this request's header fields.
     *
     * @return the request header fields.
     */
    public DSGHTTPHeader getHeader() {
        return this.header;
    }

    /**
     * Get this request's body or null, if it does not have one.
     *
     * @return the request's body or null.
     */
    public InputStream getBody() {
        return this.body;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        String target = this.target.getRawPath();
        if (target.equals("")) {
            target = "/";
        }
        String query = this.target.getRawQuery();
        if (query != null && !query.equals("")) {
            target += "?" + query;
        }
        String fragment = this.target.getRawFragment();
        if (fragment != null && !fragment.equals("")) {
            target += "#" + fragment;
        }

        String requestLine = this.method + " " + target + " " + this.version + "\r\n";
        stream.write(requestLine.getBytes(StandardCharsets.US_ASCII));
        if (this.body != null) {
            try (InputStream body = this.body) {
                ByteArrayOutputStream bodyStream = new ByteArrayOutputStream(1024);
                body.transferTo(bodyStream);
                int len = bodyStream.size();
                this.header.set("Content-Length", "" + len);
                this.header.serialize(stream);
                bodyStream.writeTo(stream);
            }
        } else {
            this.header.serialize(stream);
        }
        stream.flush();
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DSGHTTPReader reader = new DSGHTTPReader(stream);
        String requestLine = reader.readLine();
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST, "Invalid request line");
        }

        this.method = DSGHTTPMethod.forName(parts[0]);
        if (this.method == null) {
            // RFC 9110, Section 9.1: An origin server that receives a request method that
            // is
            // unrecognized or not implemented SHOULD respond with the 501 (Not Implemented)
            // status
            // code.
            throw new DSGHTTPRequestException(DSGHTTPStatus.NOT_IMPLEMENTED, "Unknown request method");
        }

        try {
            this.target = new URI(parts[1]);
        } catch (URISyntaxException e) {
            throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST, "Invalid target");
        }

        this.version = parts[2];
        if (!this.version.equals("HTTP/1.0") && !this.version.equals("HTTP/1.1")) {
            throw new DSGHTTPRequestException(DSGHTTPStatus.HTTP_VERSION_NOT_SUPPORTED, "Unsupported HTTP version");
        }

        try {
            this.header = reader.readHeader();
        } catch (DSGHTTPException e) {
            throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST, e.getMessage());
        }
        if (this.version == "HTTP/1.1" && this.header.get("Host") == null) {
            // RFC 9110, Section 7.2: A user agent MUST generate a Host header field in a
            // request.
            throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST, "Host header is missing");
        }
        if (this.header.get("Transfer-Encoding") != null) {
            // RFC 9112, Section 6.1: A server that receives a request message with a
            // transfer
            // coding it does not understand SHOULD respond with 501 (Not Implemented).
            throw new DSGHTTPRequestException(DSGHTTPStatus.NOT_IMPLEMENTED, "Transfer-Encoding is not supported");
        }

        String contentLength = this.header.get("Content-Length");
        if (contentLength == null) {
            contentLength = "0";
            this.header.set("Content-Length", "0");
        }

        // RFC 9112, Section 6.3: If a message is received without Transfer-Encoding and
        // with an
        // invalid Content-Length header field, then the message framing is invalid and
        // the
        // recipient MUST treat it as an unrecoverable error [...]. If the unrecoverable
        // error
        // is in a request message, the server MUST respond with a 400 (Bad Request)
        // status code
        // and then close the connection.
        int len;
        try {
            len = Integer.parseInt(contentLength);
            if (len < 0) {
                throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST, "Content-Length is smaller than 0");
            }
        } catch (NumberFormatException e) {
            // According to the specification, there is a special case where
            // "Content-Length" should
            // be considered valid if it is a list of integers and all of these integers
            // have the
            // same value. Considering that we are only using this code for our exercises,
            // we do not
            // implement this special behavior, because it would make parsing the content
            // length
            // significantly more complex.
            throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST, "Content-Length is not a number");
        }
        if (len == 0) {
            this.body = null;
            return;
        }
        String contentType = this.header.get("Content-Type");
        if (contentType == null || contentType.isEmpty()) {
            // RFC 9110: "If a Content-Type header field is not present, the recipient MAY
            // [...] assume a media type of "application/octet-stream"".
            this.header.set("Content-Type", "application/octet-stream");
        }

        byte[] buf = new byte[len];
        reader.stream().readNBytes(buf, 0, len);
        this.body = new ByteArrayInputStream(buf);
    }

    /**
     * Replace the this request's body with {@code body}.
     *
     * @param body the new request body.
     */
    protected void setBody(InputStream body) {
        this.body = body;
    }
}

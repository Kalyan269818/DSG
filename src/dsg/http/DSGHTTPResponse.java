package dsg.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import dsg.network.DSGMessage;

/**
 * HTTP response sent by a server.
 */
public class DSGHTTPResponse implements DSGMessage {
    /** Date formate as expected by HTTP's Date header. */
    private static final SimpleDateFormat HTTP_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",
            Locale.ENGLISH);
    static {
        // HTTP timestamps are always GMT.
        HTTP_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    String version;
    DSGHTTPStatus status;
    DSGHTTPHeader header;
    InputStream body;

    /**
     * Initialize an HTTP response with the given {@code status} and no body.
     * 
     * @param status the response status.
     */
    public DSGHTTPResponse(DSGHTTPStatus status) {
        this(status, new DSGHTTPHeader(), (InputStream) null);
    }

    /**
     * Initialize an HTTP response with the given {@code status}, using the given
     * {@code body} and assume it is of the given {@code contentType}.
     * 
     * @param status      the response status.
     * @param contentType the response body's content type.
     * @param body        the response body.
     */
    public DSGHTTPResponse(DSGHTTPStatus status, DSGHTTPMediaType contentType, InputStream body) {
        DSGHTTPHeader header = new DSGHTTPHeader();
        if (contentType != null) {
            header.add("Content-Type", contentType.toString());
        }
        this(status, header, body);
    }

    /**
     * Initialize an HTTP response with the given {@code status}, using the given
     * string's content as its body and assume the body is of the given
     * {@code contentType}.
     * 
     * If body is not {@code null}, but {@code contentType} is, {@code body} will be
     * encoded and sent to the client using UTF-8.
     * 
     * @param status      the response status.
     * @param contentType the content type of the response body.
     * @param body        the response body.
     */
    public DSGHTTPResponse(DSGHTTPStatus status, DSGHTTPMediaType contentType, String body) {
        ByteArrayInputStream stream = null;
        if (body != null) {
            if (contentType == null) {
                contentType = DSGStandardMediaTypes.PLAINTEXT.withCharset(StandardCharsets.UTF_8);
            }
            stream = new ByteArrayInputStream(body.getBytes(contentType.getCharset()));
        }
        this(status, contentType, stream);
    }

    /**
     * Initialize an HTTP response with the given {@code status} and {@code body},
     * assuming a text/plain content type and UTF-8 as the body's charset.
     * 
     * @param status the response status.
     * @param body   the response body.
     */
    public DSGHTTPResponse(DSGHTTPStatus status, String body) {
        this(status, null, body);
    }

    /**
     * Initialize an HTTP response with the given {@code status}, {@code header} and
     * {@code body}.
     * 
     * @param status the response status.
     * @param header the response header.
     * @param body   the response body.
     */
    public DSGHTTPResponse(DSGHTTPStatus status, DSGHTTPHeader header, InputStream body) {
        if (status == null) {
            throw new IllegalArgumentException("HTTP status must not be null");
        }
        if (header == null) {
            throw new IllegalArgumentException("HTTP header must not be null");
        }
        this.version = "HTTP/1.1";
        this.status = status;
        this.header = header;
        this.body = body;
        if (body == null) {
            this.header.set("Content-Length", "0");
            this.header.remove("Content-Type");
        }
        // Set default content type, if none was given.
        if (body != null && !header.isSet("Content-Type")) {
            this.header.set("Content-Type", DSGStandardMediaTypes.DEFAULT.toString());
        }
        if (!header.isSet("Server")) {
            header.set("Server", "DSGHTTPServer");
        }
        header.set("Date", HTTP_DATE_FORMAT.format(new Date()));
    }

    /**
     * Initialize an empty HTTP response.
     * 
     * This should be used only to deserialize an HTTP responses from the network.
     */
    protected DSGHTTPResponse() {
        this.version = null;
        this.status = null;
        this.header = null;
        this.body = null;
    }

    /**
     * Return this response's version.
     * 
     * @return the HTTP version.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Return this response's status.
     * 
     * @return the response status.
     */
    public DSGHTTPStatus getStatus() {
        return this.status;
    }

    /**
     * Return this response's header fields.
     * 
     * @return the response header fields.
     */
    public DSGHTTPHeader getHeader() {
        return this.header;
    }

    /**
     * Return this response's body.
     * 
     * @return the response body.
     */
    public InputStream getBody() {
        return this.body;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        String statusLine = this.version + " " + this.status.getCode() + " " + this.status.getMessage() + "\r\n";
        stream.write(statusLine.getBytes(StandardCharsets.US_ASCII));
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
        String statusLine = reader.readLine();
        String[] parts = statusLine.split(" ");
        if (parts.length < 2) {
            throw new IOException("Invalid status line");
        }

        this.version = parts[0];
        if (!this.version.equals("HTTP/1.0") && !this.version.equals("HTTP/1.1")) {
            throw new IOException("Unsupported HTTP version");
        }

        int statusCode;
        try {
            statusCode = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid status code received");
        }
        this.status = DSGHTTPStatus.forCode(statusCode);

        // We ignore the status reason sent by the server, because DSGHTTPStatus already
        // encodes reasons according to RFC 9110.
        try {
            this.header = reader.readHeader();
        } catch (DSGHTTPException e) {
            throw new IOException(e.getMessage());
        }

        if (this.header.get("Transfer-Encoding") != null) {
            // We do not support transfer encodings other than identity.
            throw new IOException("Transfer-Encoding is not supported");
        }

        String contentLength = this.header.get("Content-Length");
        if (contentLength == null || contentLength.equals("0")) {
            this.body = null;
            return;
        }

        int len;
        try {
            len = Integer.parseInt(contentLength);
            if (len < 0) {
                throw new IOException("Invalid content length");
            }
        } catch (NumberFormatException e) {
            throw new IOException("Invalid content length");
        }
        byte[] buf = new byte[len];
        reader.stream().readNBytes(buf, 0, len);
        this.body = new ByteArrayInputStream(buf);
    }

}

package dsg.http;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utility for reading HTTP messages from an InputStream.
 *
 * This class is used by {@link DSGHTTPRequest} and {@link DSGHTTPResponse} to
 * parse HTTP messages. You do not have to interact with this class during the
 * assignment or understand how it works.
 */
class DSGHTTPReader {

    private final InputStream stream;

    /**
     * Initialize an HTTP reader that reads HTTP messages from {@code stream}.
     *
     * @param stream the input byte stream.
     */
    public DSGHTTPReader(InputStream stream) {
        this.stream = stream;
    }

    /**
     * Read the next line delimited by \r\n from the stream.
     *
     * The method's return value is a byte array, because the allowed set of
     * characters depends on the context.
     *
     * @return the line read from {@code stream} as a byte array.
     * @throws IOException if reading from the input stream fails.
     */
    public String readLine() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (;;) {
            byte b = this.expectByte();
            switch (b) {
            case '\r':
                // RFC 9112, Section 2.2: "Although the line terminator for the start-line and
                // fields is the sequence CRLF, a recipient MAY recognize a single LF as a line
                // terminator and ignore any preceding CR.".
                while (b == '\r') {
                    b = this.expectByte();
                }
                // RFC 9112, Section 2.2: "A sender MUST NOT generate a bare CR (a CR character
                // not immediately followed by LF) within any protocol elements other than the
                // content. A recipient of such a bare CR MUST consider that element to be
                // invalid or replace each bare CR with SP before processing the element or
                // forwarding the message."
                if (b != '\n') {
                    throw new DSGHTTPException("Bare carriage return received");
                }
                /* FALLTHROUGH */
            case '\n':
                return output.toString(StandardCharsets.US_ASCII);
            default:
                output.write(b);
            }
        }
    }

    /**
     * Expect reading a single byte from the reader's stream.
     *
     * @return the byte read.
     * @throws IOException If EOF is read or the underlying stream thrwos an
     *                     IOException.
     */
    private byte expectByte() throws IOException {
        int i = stream.read();
        if (i == -1) {
            throw new EOFException("Unexpected EOF");
        }
        return (byte) i;
    }

    /**
     * Read and return the HTTP header from the reader's underlying stream.
     *
     * @return the parsed HTTP header.
     * @throws IOException if an IO error occurs.
     */
    public DSGHTTPHeader readHeader() throws IOException {
        DSGHTTPHeader header = new DSGHTTPHeader();
        for (;;) {
            String line = this.readLine();
            // An empty line marks the beginning of the request body.
            if (line.equals("")) {
                break;
            }
            this.parseFieldLine(line, header);
        }
        return header;
    }

    private void parseFieldLine(String fieldLine, DSGHTTPHeader header) throws DSGHTTPException {
        if (fieldLine.equals("")) {
            throw new IllegalStateException("Tried to parse header from empty line");
        }

        // Headers may not start with whitespaces.
        char first = fieldLine.charAt(0);
        if (first == ' ' || first == '\t') {
            throw new DSGHTTPException("Header starts with whitespace");
        }
        // A header's key must be at least one character.
        if (first == ':') {
            throw new DSGHTTPException("Empty header name");
        }

        int separatorIndex = -1;
        for (int i = 0; i < fieldLine.length(); i++) {
            char b = fieldLine.charAt(i);
            if (b == ':') {
                // RFC 9112, Section 5.1: No whitespace is allowed between the field name and
                // colon.
                char prev = fieldLine.charAt(i - 1);
                if (this.isWhitespace(prev)) {
                    throw new DSGHTTPException("Header name contains trailing whitespace");
                }
                separatorIndex = i;
                break;
            }
            // RFC 9110, Section 5.1 defines that header names consist solely of tokens.
            if (!this.isToken(b)) {
                throw new DSGHTTPException("Header contains invalid byte '" + b + "'");
            }
        }
        if (separatorIndex == -1) {
            throw new DSGHTTPException("Header without colon encountered.");
        }

        String name = fieldLine.substring(0, separatorIndex);
        boolean quoted = false;
        boolean escaped = false;
        StringBuilder value = new StringBuilder();
        for (int i = separatorIndex + 1; i < fieldLine.length(); i++) {
            char c = fieldLine.charAt(i);
            switch (c) {
            case '"':
                value.append(c);
                if (escaped) {
                    escaped = false;
                    continue;
                }
                quoted = !quoted;
                break;
            case '\\':
                if (escaped) {
                    value.append(c);
                    escaped = false;
                    continue;
                }

                if (quoted) {
                    // RFC 9110, Section 5.6.4: The backslash octet ("\") can be used as a
                    // single-octet quoting mechanism within quoted-string and comment
                    // constructs. Recipients that process the value of a quoted-string MUST
                    // handle a quoted-pair as if it were replaced by the octet following the
                    // backslash.
                    escaped = true;
                    continue;
                }
                value.append(c);
                break;
            case ',':
                escaped = false;
                if (!quoted) {
                    if (value.length() > 0) {
                        // The header implementation takes care of trimming preceding and
                        // trailing whitespace.
                        header.add(name, value.toString());
                    }
                    value.setLength(0);
                    continue;
                }
                value.append(c);
                break;
            default:
                escaped = false;
                if (!this.isVisibleCharacter(c) && !this.isWhitespace(c) && !this.isOpaqueData(c)) {
                    throw new DSGHTTPException("HTTP header field value contains invalid characters");
                }
                value.append(c);
            }
        }
        if (quoted || escaped) {
            throw new DSGHTTPException("Malformed field value");
        }
        // Add the final value, if any.
        if (value.length() > 0) {
            header.add(name, value.toString());
        }
    }

    /**
     * Return true if and only if {@code c} is a token as described in RFC 9110
     * Section 5.6.2.
     *
     * @param c the character that should be a token.
     * @return true if {@code b} is token.
     */
    private boolean isToken(char c) {
        return c == '!' || (c >= '#' && c <= '\'') || (c >= '*' && c != ',' && c <= '.') || (c >= '0' && c <= '9')
                || (c >= 'A' && c <= 'Z') || (c >= '^' && c <= 'z') || c == '|' || c == '~';
    }

    /**
     * Return, whether {@code c} is a visible ASCII character (VCHAR) as described
     * in RFC 5234 Appendix B.1.
     *
     * @param c the character that should be a VCHAR.
     * @return true if {@code c} is a VCHAR.
     */
    private boolean isVisibleCharacter(char c) {
        return c >= '!' && c <= '~';
    }

    /**
     * Return whether {@code c} is a whitespace according to RFC 9110.
     *
     * @param c the character under test.
     * @return true if {@code c} is a whitespace.
     */
    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t';
    }

    /**
     * Return true, if {@code c} is in the range of opaque data according to RFC
     * 9110.
     *
     * @param c the character under test.
     * @return true if {@code c}'s value lies between 0x80 and 0xff;
     */
    private boolean isOpaqueData(char c) {
        return c >= 0x80 && c <= 0xff;
    }

    /**
     * Return the reader's underlying input stream, which allows reading raw data
     * (e.g. the request body).
     *
     * @return the reader's underlying input stream.
     */
    public InputStream stream() {
        return this.stream;
    }

}

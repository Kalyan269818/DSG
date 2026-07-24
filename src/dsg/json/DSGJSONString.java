package dsg.json;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;

import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPMediaType;
import dsg.json.DSGJSONToken.Type;

public class DSGJSONString extends DSGJSONValue implements Comparable<DSGJSONString> {
    private final static String[] ESCAPE_SEQUENCE_LOOKUP_TABLE = new String[] { "\\0", "\u0001", "\u0002", "\u0003",
            "\u0004", "\u0005", "\u0006", "\u0007", "\\b", "\\t", "\\n", "\\u000b", "\\f", "\\r", "\u000e", "\u000f",
            "\u0010", "\u0011", "\u0012", "\u0013", "\u0014", "\u0015", "\u0016", "\u0017", "\u0018", "\u0019",
            "\u001a", "\u001b", "\u001c", "\u001d", "\u001E", "\u001f",

    };

    /** The JSON strings value. */
    private String value;

    /**
     * Initialize an empty string.
     */
    public DSGJSONString() {
        this("");
    }

    /**
     * Initialize the JSON string from an existing string.
     *
     * @param value the string's used as the JSON value.
     */
    public DSGJSONString(String value) {
        if (value == null) {
            value = "";
        }
        this.value = value;
    }

    /**
     * Initialize a JSON string from an URI.
     *
     * @param uri the URI used as the string's value.
     */
    public DSGJSONString(URI uri) {
        if (uri == null) {
            value = "";
        }
        this.value = uri.toASCIIString();
    }

    /**
     * Initialize a JSON string from a Date.
     *
     * @param uri the Date used as the string's value.
     */
    public DSGJSONString(Date date) {
        if (date == null) {
            value = "";
        }
        this.value = DSGJSONValue.DATE_FORMAT.format(date);
    }

    /**
     * Return a representation of this JSON string, escaping all characters that
     * must be escaped according to the specification.
     *
     * More specifically, according to RFC 8259, the following characters have to be
     * escaped: quotation mark, reverse solidus, and the control characters (U+0000
     * through U+001F).
     */
    public String getEscapedValue() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.value.length(); i++) {
            char c = this.value.charAt(i);
            switch (c) {
            case '"':
                builder.append("\\\"");
                break;
            case '\\':
                builder.append("\\\\");
                break;
            default:
                if (c < ESCAPE_SEQUENCE_LOOKUP_TABLE.length) {
                    builder.append(ESCAPE_SEQUENCE_LOOKUP_TABLE[c]);
                } else {
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    @Override
    public String asString() {
        return value;
    }

    @Override
    public URI asURI() throws DSGJSONException {
        if (value == null) {
            return null;
        }
        try {
            return new URI(value);
        } catch (URISyntaxException use) {
            throw new DSGJSONException(use.getMessage());
        }
    }

    @Override
    public Date asDate() throws DSGJSONException {
        try {
            return DSGJSONValue.DATE_FORMAT.parse(value);
        } catch (ParseException e) {
            throw new DSGJSONException(e.getMessage());
        }
    }

    @Override
    public DSGHTTPMediaType asMediaType() throws DSGJSONException {
        try {
            return new DSGHTTPMediaType(value);
        } catch (DSGHTTPException e) {
            throw new DSGJSONException(e.getMessage());
        }
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public DSGJSONType getType() {
        return DSGJSONType.String;
    }

    @Override
    public int compareTo(DSGJSONString other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof DSGJSONString) {
            return this.compareTo((DSGJSONString) other) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    protected void serializeJSON(Writer writer) throws IOException {
        writer.write(DSGJSONToken.QUOTATION_MARK_SYMBOL);
        writer.write(this.getEscapedValue());
        writer.write(DSGJSONToken.QUOTATION_MARK_SYMBOL);
    }

    @Override
    protected void deserializeJSON(DSGJSONReader reader) throws DSGJSONException, IOException {
        DSGJSONToken token = reader.expectTokenOf(Type.STRING);
        this.value = token.getValue();
    }
}

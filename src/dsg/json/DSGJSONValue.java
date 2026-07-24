package dsg.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import dsg.http.DSGHTTPMediaType;

/**
 * Class implemented by all JSON values.
 */
public abstract class DSGJSONValue {
    /** RFC3339 date format as required by ActivityStreams. */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

    /**
     * JSON data types.
     */
    public static enum DSGJSONType {
        Array, Boolean, Number, Object, String,
    }

    /**
     * Return the Value's type.
     *
     * @return the value's type.
     */
    public abstract DSGJSONType getType();

    /**
     * Encode the JSON value into its byte-representation using the default JSON
     * charset.
     *
     * @return an input stream containing the encoded version of this value.
     */
    public InputStream toInputStream() {
        return toInputStream(StandardCharsets.UTF_8);
    }

    /**
     * Encode the JSON value into its byte-representation, using the given
     * {@code charset}.
     *
     * @param charset the charset to use during encoding.
     * @return an input stream containing the encoded version of this value.
     */
    public InputStream toInputStream(Charset charset) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (Writer writer = new OutputStreamWriter(output, charset)) {
                serializeJSON(writer);
            }
            return new ByteArrayInputStream(output.toByteArray());
        } catch (IOException ioe) {
            // This should never happen, because we write to a byte array.
            throw new IllegalStateException(ioe);
        }
    }

    /**
     * Return this value represented as a long.
     *
     * @return the value's long representation.
     * @throws DSGJSONException if the value cannot be represented as a long.
     *
     */
    public long asLong() throws DSGJSONException {
        throw new DSGJSONException("Cannot represent " + getType() + " as long");
    }

    /**
     * Return this value's string representation.
     *
     * @return this JSON value's string representation.
     * @throws DSGJSONException if the value cannot be represented as a string.
     *
     */
    public String asString() throws DSGJSONException {
        throw new DSGJSONException("Cannot represent " + getType() + " as string");
    }

    /**
     * Try to parse this JSON value into an URI.
     *
     * @return this JSON value's URI representation.
     * @throws DSGJSONException if the value cannot be parsed into an URI.
     *
     */
    public URI asURI() throws DSGJSONException {
        throw new DSGJSONException("Cannot represent " + getType() + " as URI");
    }

    /**
     * Try to parse this JSON value into a Date.
     *
     * @return this JSON value's Date representation.
     * @throws DSGJSONException if the value cannot be parsed into Date.
     *
     */
    public Date asDate() throws DSGJSONException {
        throw new DSGJSONException("Cannot represent " + getType() + " as Date");
    }

    /**
     * Try to parse this JSON value into a HTTP media type.
     *
     * @return this JSON value's media type representation.
     * @throws DSGJSONException if the value cannot be parsed into a media type.
     *
     */
    public DSGHTTPMediaType asMediaType() throws DSGJSONException {
        throw new DSGJSONException("Cannot represent " + getType() + " as DSGHTTPMediaType");
    }

    /**
     * Try to cast this JSON value into a type of T.
     *
     * @param <T>
     * @return the JSON value cast into T.
     * @throws DSGJSONException if this JSON value is not of type T.
     */
    @SuppressWarnings("unchecked")
    public <T extends DSGJSONValue> T as() throws DSGJSONException {
        try {
            return (T) this;
        } catch (ClassCastException cce) {
            throw new DSGJSONException(cce.getMessage());
        }
    }

    /**
     * Return a string with this values JSON.
     */
    @Override
    public String toString() {
        StringWriter writer = new StringWriter();
        try {
            serializeJSON(writer);
        } catch (IOException e) {
            // Should never happen.
            throw new IllegalStateException(e);
        }
        return writer.toString();
    }

    /**
     * Write the byte-representation of this JSON value to {@code out}.
     *
     * @param out the writer to which the JSON value will be written.
     * @throws IOException if an error occurs while writing to {@code out}.
     */
    protected abstract void serializeJSON(Writer out) throws IOException;

    /**
     * Deserialize the value from the given {@code reader}.
     *
     * @param reader the reader from which to read the JSON value.
     * @throws DSGJSONException If the reader's content is not valid JSON.
     * @throws IOException      if an error occurs while reading from the reader's
     *                          underlying input stream.
     */
    protected abstract void deserializeJSON(DSGJSONReader reader) throws DSGJSONException, IOException;

}

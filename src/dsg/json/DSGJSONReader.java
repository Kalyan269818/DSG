package dsg.json;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import dsg.json.DSGJSONToken.Type;

/**
 * Reads and returns JSON-serialized values from an input stream.
 */
public class DSGJSONReader implements AutoCloseable {
    private DSGJSONTokenInputStream input;

    public DSGJSONReader(DSGJSONTokenInputStream input) {
        this.input = input;
    }

    public DSGJSONReader(InputStream input, Charset charset) {
        this.input = new DSGJSONLexer(input, charset);
    }

    public DSGJSONReader(InputStream input) {
        this.input = new DSGJSONLexer(input);
    }

    /**
     * Read the next JSON value from the reader's underlying stream.
     *
     * @return the reader's next JSON value.
     * @throws DSGJSONException if the input stream contains invalid characters.
     * @throws IOException      if I/O error occurs while reading from the input
     *                          stream or EOF was read.
     */
    public DSGJSONValue read() throws DSGJSONException, IOException {
        DSGJSONToken token = peekExpectToken();
        switch (token.getType()) {
        case BEGIN_ARRAY:
            return readArray();
        case BEGIN_OBJECT:
            return readObject();
        case BOOLEAN:
            return readBoolean();
        case NULL:
            return null;
        case NUMBER:
            return readNumber();
        case STRING:
            return readString();
        default:
            throw new DSGJSONException(token.getPosition(), "Unexpected " + token.getType() + ", expected value");
        }
    }

    /**
     * Read an array from the reader's underlying input stream.
     *
     * @return the array read.
     * @throws DSGJSONException if the next JSON value in the input stream is not an
     *                          array.
     * @throws IOException      if an I/O error occurs while reading from the input
     *                          stream or if an unexpected EOF was read.
     */
    public DSGJSONArray readArray() throws DSGJSONException, IOException {
        DSGJSONArray array = new DSGJSONArray();
        array.deserializeJSON(this);
        return array;
    }

    /**
     * Read an object from the reader's underlying input stream.
     *
     * @return the array read.
     * @throws DSGJSONException if the next JSON value in the input stream is not an
     *                          object.
     * @throws IOException      if an I/O error occurs while reading from the input
     *                          stream or if an unexpected EOF was read.
     */
    public DSGJSONObject readObject() throws DSGJSONException, IOException {
        DSGJSONObject object = new DSGJSONObject();
        object.deserializeJSON(this);
        return object;
    }

    /**
     * Read a boolean from the reader's underlying input stream.
     *
     * @return the array read.
     * @throws DSGJSONException if the next JSON value in the input stream is not a
     *                          boolean.
     * @throws IOException      if an I/O error occurs while reading from the input
     *                          stream or if an unexpected EOF was read.
     */
    public DSGJSONBoolean readBoolean() throws DSGJSONException, IOException {
        DSGJSONBoolean bool = new DSGJSONBoolean();
        bool.deserializeJSON(this);
        return bool;
    }

    /**
     * Read a number from the reader's underlying input stream.
     *
     * @return the array read.
     * @throws DSGJSONException if the next JSON value in the input stream is not a
     *                          number.
     * @throws IOException      if an I/O error occurs while reading from the input
     *                          stream or if an unexpected EOF was read.
     */
    public DSGJSONNumber readNumber() throws DSGJSONException, IOException {
        DSGJSONNumber number = new DSGJSONNumber();
        number.deserializeJSON(this);
        return number;
    }

    /**
     * Read a string from the reader's underlying input stream.
     *
     * @return the string read.
     * @throws DSGJSONException if the next JSON value in the input stream is not a
     *                          valid string.
     * @throws IOException      if an I/O error occurs while reading from the input
     *                          stream or if an unexpected EOF was read.
     */
    public DSGJSONString readString() throws DSGJSONException, IOException {
        DSGJSONString str = new DSGJSONString();
        str.deserializeJSON(this);
        return str;
    }

    /**
     * Expect a token to be returned from the underlying
     * {@link DSGJSONTokenInputStream}, consuming the token in the process.
     *
     * @return the next token from the reader's input stream.
     * @throws IOException if an I/O error occurs while reading from the input
     *                     stream or if an unexpected EOF was read.
     */
    protected DSGJSONToken expectToken() throws IOException {
        DSGJSONToken token = this.input.read();
        if (token == null) {
            throw new EOFException("Unexpected EOF, expected token");
        }
        return token;
    }

    /**
     * Peek the next token from the underlying {@link DSGJSONTokenInputStream} and
     * expect, that a token is actually returned.
     *
     * @return the next token from the reader's input stream.
     * @throws IOException if an I/O error occurs while reading from the input
     *                     stream or if an unexpected EOF was read.
     */
    protected DSGJSONToken peekExpectToken() throws IOException {
        DSGJSONToken token = this.input.peek();
        if (token == null) {
            throw new EOFException("Unexpected EOF expected token");
        }
        return token;
    }

    /**
     * Consume the current token from the underlying {@link DSGJSONTokenInputStream}
     * without returning it.
     *
     * @throws IOException if an I/O error occurs while reading from the input
     *                     stream or if an unexpected EOF was read.
     */
    protected void consumeToken() throws IOException {
        expectToken();
    }

    /**
     * Expect that the next token is of the named {@code type} and return it.
     *
     * @param type
     * @return
     * @throws DSGJSONException if the actual token is not of the expected
     *                          {@code type}.
     * @throws IOException      if an I/O error occurs while reading from the input
     *                          stream or if an unexpected EOF was read.
     */
    protected DSGJSONToken expectTokenOf(Type type) throws DSGJSONException, IOException {
        try {
            DSGJSONToken token = this.expectToken();
            if (token.getType() != type) {
                throw new DSGJSONException(token, type);
            }
            return token;
        } catch (EOFException e) {
            throw new EOFException("Unexpected EOF expected token of type " + type);
        }
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }
}

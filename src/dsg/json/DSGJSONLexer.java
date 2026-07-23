package dsg.json;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import dsg.json.DSGJSONToken.Type;

/**
 * An implementation of {@link DSGJSONTokenInputStream} that reads JSON from an
 * {@link InputStream} and transforms it into a stream of tokens.
 */
public class DSGJSONLexer implements DSGJSONTokenInputStream, AutoCloseable {

    private BufferedReader input;
    private DSGJSONTokenPosition position;
    private StringBuilder value;
    private DSGJSONTokenPosition valueStart;
    private boolean eof;
    private DSGJSONToken next;

    /**
     * Read from an input stream with the default JSON charset (UTF-8).
     *
     * @param stream the input stream from which to read data.
     */
    public DSGJSONLexer(InputStream input) {
        this(input, StandardCharsets.UTF_8);
    }

    /**
     * Read java objects from the given stream using the given charset.
     *
     * @param reader the reader from which JSON is read.
     */
    public DSGJSONLexer(InputStream input, Charset charset) {
        this.input = new BufferedReader(new InputStreamReader(input, charset));
        this.position = new DSGJSONTokenPosition();
        this.value = new StringBuilder();
        this.eof = false;
        this.next = null;
    }

    /**
     * Peek peeks at the next JSON token from the lexer's underlying stream without
     * consuming it.
     *
     * return the next JSON token or null, if the stream has reached EOF.
     *
     * @throws IOException if an I/O error occurs on the underlying reader.
     */
    @Override
    public synchronized DSGJSONToken peek() throws IOException {
        if (this.next != null) {
            return this.next;
        }
        if (this.eof) {
            return null;
        }
        this.next = read();
        return this.next;
    }

    /**
     * Return and consume the next JSON token from the lexer's underlying stream.
     *
     * @return the next JSON token or null, if the stream has reached EOF.
     * @throws IOException if an I/O error occurs on the underlying reader.
     */
    @Override
    public synchronized DSGJSONToken read() throws IOException {
        if (this.next != null) {
            DSGJSONToken result = this.next;
            this.next = null;
            return result;
        }
        if (this.eof) {
            return null;
        }

        this.value.setLength(0);
        for (;;) {
            this.input.mark(1);
            int i = this.input.read();
            if (i == -1) {
                this.eof = true;
                return null;
            }
            this.position.newChar();

            char c = (char) i;
            switch (c) {
            case DSGJSONToken.BEGIN_OBJECT_SYMBOL:
                return new DSGJSONToken(DSGJSONToken.Type.BEGIN_OBJECT, null, new DSGJSONTokenPosition(this.position));
            case DSGJSONToken.END_OBJECT_SYMBOL:
                return new DSGJSONToken(DSGJSONToken.Type.END_OBJECT, null, new DSGJSONTokenPosition(position));
            case DSGJSONToken.BEGIN_ARRAY_SYMBOL:
                return new DSGJSONToken(DSGJSONToken.Type.BEGIN_ARRAY, null, new DSGJSONTokenPosition(position));
            case DSGJSONToken.END_ARRAY_SYMBOL:
                return new DSGJSONToken(DSGJSONToken.Type.END_ARRAY, null, new DSGJSONTokenPosition(position));
            case DSGJSONToken.NAME_SEPARATOR_SYMBOL:
                return new DSGJSONToken(DSGJSONToken.Type.NAME_SEPARATOR, null, new DSGJSONTokenPosition(position));
            case DSGJSONToken.VALUE_SEPARATOR_SYMBOL:
                return new DSGJSONToken(DSGJSONToken.Type.VALUE_SEPARATOR, null, new DSGJSONTokenPosition(position));
            case DSGJSONToken.QUOTATION_MARK_SYMBOL:
                this.valueStart = new DSGJSONTokenPosition(this.position);
                return this.readString();
            case DSGJSONToken.MINUS_SYMBOL:
                this.valueStart = new DSGJSONTokenPosition(position);
                this.value.append(c);
                return readNumber();
            case '\n':
                this.position.newLine();
                /* FALLTHROUGH */
            default:
                // Whitespace does have no meaning in this context, so we can skip it.
                if (this.isWhitespace(c)) {
                    continue;
                }

                this.input.reset();
                this.position.reset();
                this.valueStart = new DSGJSONTokenPosition(this.position);
                if (this.isDigit(c)) {
                    return this.readNumber();
                }
                return this.readLiteral();
            }

        }
    }

    private DSGJSONToken readString() throws IOException {
        for (;;) {
            char c = this.readExpectedByte();
            switch (c) {
            case DSGJSONToken.QUOTATION_MARK_SYMBOL:
                return this.createToken(Type.STRING);
            case DSGJSONToken.ESCAPE_SYMBOL:
                this.readEscapeSequence();
                break;
            default:
                if (!this.unescaped(c)) {
                    throw new DSGJSONException(this.position, "Invalid character '" + c + "' in string.");
                }
                this.value.append(c);
            }
        }
    }

    private boolean unescaped(char b) {
        return b >= ' ' && b != '"' && b != '\\';
    }

    private void readEscapeSequence() throws IOException {
        char c = this.readExpectedByte();
        switch (c) {
        case DSGJSONToken.ESCAPE_SYMBOL:
        case DSGJSONToken.QUOTATION_MARK_SYMBOL:
        case '/':
            this.value.append(c);
            break;
        case 'b':
            this.value.append('\b');
        case 'f':
            this.value.append('\f');
        case 'n':
            this.value.append('\n');
            break;
        case 'r':
            this.value.append('\r');
            break;
        case 't':
            this.value.append('\t');
            break;
        case 'u':
            this.readUnicodeEscapeCode();
            break;
        default:
            throw new DSGJSONException(this.position, "Unexpected byte '" + c + "', expected escapable character");
        }
    }

    private void readUnicodeEscapeCode() throws IOException {
        // Unicode escape characters consist of a backslash, 'u', and a 4 hexadecimal
        // digits.
        int codePoint = 0;
        for (int i = 0; i < 4; i++) {
            char c = this.readExpectedByte();
            try {
                int b = hexToByte(c);
                codePoint = (codePoint << 4) | b;
            } catch (NumberFormatException e) {
                throw new DSGJSONException(this.position, e.getMessage());
            }
        }
        this.value.appendCodePoint(codePoint);
    }

    private int hexToByte(char c) {
        if (this.isDigit(c)) {
            return (c - '0') & 0xf;
        } else if (c >= 'a' && c <= 'f') {
            return ((c - 'a') + 10) & 0xf;
        } else if (c >= 'A' && c <= 'F') {
            return ((c - 'A') + 10) & 0xf;
        }
        throw new NumberFormatException("'" + c + "' is not a hexadecimal digit");
    }

    private DSGJSONToken readNumber() throws IOException {
        // Numbers must start with at least one digit.
        char c = this.readExpectedByte();
        if (c < '0' || c > '9') {
            throw new DSGJSONException(this.position, "Unexpected byte '" + c + "', digit expected");
        }
        this.value.append(c);
        // '0' requires special treatment, because it is a terminal state, that is, it
        // cannot be followed by other digits.
        if (c != '0') {
            for (;;) {
                this.input.mark(1);
                int i = this.input.read();
                if (i == -1) {
                    return this.createToken(Type.NUMBER);
                }
                c = (char) i;
                if (!this.isDigit(c)) {
                    this.input.reset();
                    break;
                }
                this.position.newChar();
                this.value.append(c);
            }
        }
        for (;;) {
            this.input.mark(1);
            int i = this.input.read();
            if (i == -1) {
                return this.createToken(Type.NUMBER);
            }

            this.position.newChar();
            c = (char) i;
            switch (c) {
            case '.':
                this.value.append(c);
                return readFraction();
            case 'e':
            case 'E':
                this.value.append(c);
                return readExponent();
            default:
                this.input.reset();
                return this.createToken(Type.NUMBER);
            }
        }
    }

    private DSGJSONToken readFraction() throws IOException {
        // Fractions must have at least one digit.
        char c = this.readExpectedByte();
        if (!this.isDigit(c)) {
            throw new DSGJSONException(this.position, "Unexpected byte '" + c + "', expected digit");
        }
        this.value.append(c);

        for (;;) {
            this.input.mark(1);
            int i = this.input.read();
            if (i == -1) {
                return this.createToken(Type.NUMBER);
            }

            this.position.newChar();
            c = (char) i;
            switch (c) {
            case 'e':
            case 'E':
                this.value.append(c);
                return readExponent();
            default:
                if (!this.isDigit(c)) {
                    this.input.reset();
                    this.position.reset();
                    return this.createToken(Type.NUMBER);
                }

                this.value.append(c);
                break;
            }
        }
    }

    private DSGJSONToken readExponent() throws IOException {
        // Exponents start with an optional sign and must consist of at least one digit.
        char c = this.readExpectedByte();
        if (c == DSGJSONToken.PLUS_SYMBOL || c == DSGJSONToken.MINUS_SYMBOL) {
            this.value.append(c);
            c = this.readExpectedByte();
        }
        if (!this.isDigit(c)) {
            throw new DSGJSONException(this.position, "Unexpected byte '" + c + "', expected digit");
        }

        this.value.append(c);
        for (;;) {
            this.input.mark(1);
            int i = this.input.read();
            if (i == -1) {
                return this.createToken(Type.NUMBER);
            }

            this.position.newChar();
            c = (char) i;
            if (!this.isDigit(c)) {
                this.input.reset();
                return this.createToken(Type.NUMBER);
            }
            this.position.newChar();
        }
    }

    private DSGJSONToken readLiteral() throws IOException {
        for (;;) {
            this.input.mark(1);
            int i = this.input.read();
            if (i == -1) {
                break;
            }

            char c = (char) i;
            this.position.newChar();
            if (!this.isLowercaseLetter(c)) {
                this.input.reset();
                if (c == '\n') {
                    this.position.newLine();
                }
                break;
            }
            this.value.append(c);
        }

        String value = this.value.toString();
        switch (value) {
        case DSGJSONToken.TRUE_LITERAL:
        case DSGJSONToken.FALSE_LITERAL:
            return this.createToken(Type.BOOLEAN);
        case DSGJSONToken.NULL_LITERAL:
            return this.createToken(Type.NULL);
        default:
            throw new DSGJSONException(this.valueStart,
                    "Invalid literal '" + value + "' read, expected true, false, or null");
        }
    }

    private DSGJSONToken createToken(Type type) {
        String value = this.value.toString();
        this.value.setLength(0);
        return new DSGJSONToken(type, value, new DSGJSONTokenPosition(this.valueStart));
    }

    private char readExpectedByte() throws IOException {
        int c = this.input.read();
        if (c == -1) {
            throw new EOFException(this.position + ": Unexpected end of file");
        }
        this.position.newChar();
        return (char) c;
    }

    private boolean isWhitespace(char c) {
        return c == '\t' || c == '\n' || c == '\r' || c == ' ';
    }

    private boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isLowercaseLetter(int c) {
        // Technically only the letters in "true", "false", and "null" are allowed, but
        // just checking ASCII lowercase is faster.
        return c >= 'a' && c <= 'z';
    }

    @Override
    public void close() throws IOException {
        this.input.close();
    }
}

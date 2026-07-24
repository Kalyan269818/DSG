package dsg.json;

import java.io.IOException;

public interface DSGJSONTokenInputStream {
    /**
     * Peek at the next {@link DSGJSONToken} from this stream without consuming it.
     *
     * @return the next token or {@code null} if no token more tokens are available.
     */
    public DSGJSONToken peek() throws IOException;

    /**
     * Read and consume the next {@link DSGJSONToken} from this stream.
     *
     * @return the next token or {@code null} if no token more tokens are available.
     */
    public DSGJSONToken read() throws IOException;

    /**
     * Closes the input stream.
     *
     * @throws Exception if an error occurs while trying to close the stream.
     */
    void close() throws IOException;
}

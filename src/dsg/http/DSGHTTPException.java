package dsg.http;

import java.io.IOException;

/**
 * Exception thrown when a problem related to HTTP is encountered.
 */
public class DSGHTTPException extends IOException {

    /**
     * Initialize an HTTP exception with the given {@code message}.
     *
     * @param message a message describing the exception.
     */
    public DSGHTTPException(String message) {
        super(message);
    }
}

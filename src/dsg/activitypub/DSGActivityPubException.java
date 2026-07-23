package dsg.activitypub;

/**
 * Exception thrown if something went wrong during an ActivityPub operation.
 */
public class DSGActivityPubException extends Exception {

    /**
     * Initialize an ActivityPub exception with the given message.
     *
     * @param message the exception's message.
     */
    public DSGActivityPubException(String message) {
        super(message);
    }

    /**
     * Initialize an ActivityPub exception with the given cause.
     *
     * @param cause the underlying cause of this exception.
     */
    public DSGActivityPubException(Throwable cause) {
        super(cause);
    }

    /**
     * Initialize an ActivityPub exception with a message and a cause.
     *
     * @param cause   the underlying cause of this exception.
     * @param message the exception's message.
     */
    public DSGActivityPubException(String message, Throwable cause) {
        super(message, cause);
    }
}

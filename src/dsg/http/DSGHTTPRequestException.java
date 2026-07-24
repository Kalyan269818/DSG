package dsg.http;

/**
 * Exception thrown when a problem related to an HTTP request is encountered.
 */
public class DSGHTTPRequestException extends DSGHTTPException {
	private DSGHTTPStatus status;

	/**
	 * Initialize a request exception with the named status.
	 * 
	 * The exception's message will be the default reason phrase of {@code status}.
	 * 
	 * @param status the status that should be sent to the client.
	 */
	public DSGHTTPRequestException(DSGHTTPStatus status) {
		this(status, status.toString());
	}

	/**
	 * Initialize a request exception with the named status and a message.
	 * 
	 * In addition to a message, the exception takes an HTTP status code as an
	 * argument. This makes makes it easier for server implementations to send back
	 * proper error responses to clients.
	 * 
	 * @param status  the status that should be sent to the client.
	 * @param message the exception message.
	 */
	public DSGHTTPRequestException(DSGHTTPStatus status, String message) {
		super(message);
		this.status = status;
	}

	/**
	 * Return the HTTP status code corresponding to the exception.
	 * 
	 * @return the exception's corresponding HTTP status code.
	 */
	public DSGHTTPStatus getStatus() {
		return this.status;
	}
}

package dsg.rest;

import dsg.http.DSGHTTPStatus;

/**
 * Exception thrown if a REST-related error occurred.
 */
public class DSGRESTException extends Exception {
    private DSGHTTPStatus status;

    /**
     * Initialize a REST exception with the given {@code status}.
     * 
     * @param status the status of the exception.
     */
    public DSGRESTException(DSGHTTPStatus status) {
        super(status.toString());
        this.status = status;
    }

    /**
     * Return HTTP status corresponding to the exception.
     * 
     * @return the exception's HTTP status.
     */
    public DSGHTTPStatus getStatus() {
        return status;
    }
}

package dsg.rest;

/**
 * Exception thrown for communication-related problems encountered during a REST
 * remote procedure call.
 */
public class DSGRESTRemoteException extends Exception {
    public DSGRESTRemoteException() {
    }

    public DSGRESTRemoteException(String message) {
        super(message);
    }

    public DSGRESTRemoteException(String message, Throwable cause) {
        super(message, cause);
    }

    public DSGRESTRemoteException(Throwable cause) {
        super(cause);
    }
}

package dsg.http;

/**
 * Interface implemented by classes that can respond to incoming HTTP requests.
 */
public interface DSGHTTPHandler {
    /**
     * Handle an incoming HTTP {@code request} and return a corresponding response.
     * 
     * @param request the request that will be processed.
     * @return the corresponding response.
     * @throws DSGHTTPException        if an unexpected error occurred.
     * @throws DSGHTTPRequestException if the request is somehow invalid.
     */
    public DSGHTTPResponse handle(DSGHTTPRequest request) throws DSGHTTPException, DSGHTTPRequestException;
}

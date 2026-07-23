package dsg.rest;

import dsg.http.DSGHTTPRequest;

/**
 * Factory interface for dynamically creating new REST resource instances.
 *
 * The factory can be used to dynamically generate resources that have not been
 * exported explicitly by using {@link DSGRESTSkeleton}'s {@code exportObject()}
 * method. It serves two purposes: 1.) It can be used in cases where resources
 * might be generated dynamically and 2.) providing an instance for objects that
 * do not exist, but are going to be created by a PUT request.
 */
public interface DSGRESTResourceFactory {
    /**
     * Conditionally create and return a new resource that has not been exported
     * explicitly.
     *
     * @param request the HTTP request.
     * 
     * @return a dynamically created resource or null, if no adequate resource for
     *         the request exists.
     * @throws DSGRESTException if the client's request is invalid.
     */
    public DSGAbstractRESTResource newInstance(DSGHTTPRequest request) throws DSGRESTException;
}

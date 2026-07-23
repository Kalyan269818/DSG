package dsg.rest;

import dsg.http.DSGHTTPHandler;
import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPRequest;
import dsg.http.DSGHTTPRequestException;
import dsg.http.DSGHTTPResponse;

/**
 * An HTTP handler functioning as RPC skeleton for {@link DSGRESTResource}
 * implementations.
 */
public class DSGRESTSkeleton implements DSGHTTPHandler {

    // ##################
    // # INITIALIZATION #
    // ##################

    /**
     * Initialize a REST handler that does not have a resource factory.
     */
    public DSGRESTSkeleton() {
        this(null);
    }

    /**
     * Initialize a REST handler with a resource factory.
     *
     * The default resource is a catch-all resource that is called every time a
     * request references a target path that does not exist.
     * 
     * @param resourceFactory factory that is used to create new resources on
     *                        incoming PUT requests.
     */
    public DSGRESTSkeleton(DSGRESTResourceFactory resourceFactory) {
        // TODO Implement constructor
    }

    // ####################
    // # REQUEST HANDLING #
    // ####################

    @Override
    public DSGHTTPResponse handle(DSGHTTPRequest request) throws DSGHTTPException, DSGHTTPRequestException {
        // TODO Implement method
        return null;
    }

    // #######################
    // # RESOURCE MANAGEMENT #
    // #######################

    /**
     * Makes {@code resource} available under the given {@code path}.
     * 
     * @param path     the path at which the resource will be available.
     * @param resource the resource to export.
     */
    public void exportResource(String path, DSGAbstractRESTResource resource) {
        // TODO Implement method
    }

    /**
     * Return, whether a path has been exported.
     * 
     * @param path the path to check.
     * @return true if {@code path} points to an exported resource.
     */
    public boolean isExported(String path) {
        // TODO Implement method
        return false;
    }

    /**
     * Remove the resource with the given {@code path} from this handler.
     * 
     * @param path the path to remove.
     */
    public void unexportResource(String path) {
        // TODO Implement method
    }
}

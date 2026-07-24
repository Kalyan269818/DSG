package dsg.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dsg.http.DSGHTTPHandler;
import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPMethod;
import dsg.http.DSGHTTPRequest;
import dsg.http.DSGHTTPRequestException;
import dsg.http.DSGHTTPResponse;
import dsg.http.DSGHTTPStatus;

/**
 * An HTTP handler functioning as RPC skeleton for {@link DSGRESTResource}
 * implementations.
 */
public class DSGRESTSkeleton implements DSGHTTPHandler {

    // ##################
    // # INITIALIZATION #
    // ##################

    /** Resources explicitly exported via {@link #exportResource(String, DSGAbstractRESTResource)}. */
    private final Map<String, DSGAbstractRESTResource> resources = new ConcurrentHashMap<>();

    /** Factory used to look up resources for paths that were not explicitly exported. */
    private final DSGRESTResourceFactory resourceFactory;

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
        this.resourceFactory = resourceFactory;
    }

    // ####################
    // # REQUEST HANDLING #
    // ####################

    @Override
    public DSGHTTPResponse handle(DSGHTTPRequest request) throws DSGHTTPException, DSGHTTPRequestException {
        // TODO Implement method
        String path = request.getTarget().getPath();
        DSGAbstractRESTResource resource = resources.get(path);

        if (resource == null && resourceFactory != null) {
            try {
                resource = resourceFactory.newInstance(request);
            } catch (DSGRESTException e) {
                return new DSGHTTPResponse(e.getStatus(), e.getMessage());
            }
        }

        if (resource == null) {
            return new DSGHTTPResponse(DSGHTTPStatus.NOT_FOUND);
        }

        DSGHTTPMethod method = request.getMethod();
        if (method != DSGHTTPMethod.GET && method != DSGHTTPMethod.HEAD && method != DSGHTTPMethod.POST
                && method != DSGHTTPMethod.PUT && method != DSGHTTPMethod.DELETE) {
            return new DSGHTTPResponse(DSGHTTPStatus.NOT_IMPLEMENTED);
        }

        DSGRESTContext ctx = new DSGRESTContext(request);
        DSGAbstractRESTResource target = resource;

        try {
            DSGRESTRepresentation representation;
            switch (method) {
            case GET:
                representation = target.get(ctx, request.getHeader());
                break;
            case HEAD:
                representation = target.head(ctx, request.getHeader());
                break;
            case PUT:
                representation = target.put(ctx, new DSGRESTRepresentation(request));
                if (representation != null && representation.getStatus() == DSGHTTPStatus.CREATED) {
                    exportResource(path, target);
                }
                break;
            case POST:
                representation = target.post(ctx, new DSGRESTRepresentation(request));
                break;
            default: // DELETE
                representation = target.delete(ctx);
                unexportResource(path);
                break;
            }

            if (representation == null) {
                return new DSGHTTPResponse(DSGHTTPStatus.NO_CONTENT);
            }
            return new DSGHTTPResponse(representation.getStatus(), representation.getHeader(),
                    representation.getResource());
        } catch (UnsupportedOperationException e) {
            return new DSGHTTPResponse(DSGHTTPStatus.METHOD_NOT_ALLOWED);
        } catch (DSGRESTException e) {
            return new DSGHTTPResponse(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            return new DSGHTTPResponse(DSGHTTPStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
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
        resources.put(path, resource);
    }

    /**
     * Return, whether a path has been exported.
     *
     * @param path the path to check.
     * @return true if {@code path} points to an exported resource.
     */
    public boolean isExported(String path) {
        // TODO Implement method
        return resources.containsKey(path);
    }

    /**
     * Remove the resource with the given {@code path} from this handler.
     *
     * @param path the path to remove.
     */
    public void unexportResource(String path) {
        // TODO Implement method
        resources.remove(path);
    }
}

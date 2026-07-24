package dsg.rest;

import dsg.http.DSGHTTPHeader;

/**
 * Interface for REST resources.
 */
public interface DSGRESTResource {
	/**
	 * Return a selected representation of this resource.
	 *
	 * @param ctx    Context associated with this REST request.
	 * @param header the HTTP headers associated with the request.
	 *
	 * @return the selected representation of this resource.
	 *
	 * @throws DSGRESTException              if this resource does not exist.
	 * @throws DSGRESTException              if creating the selected representation
	 *                                       for this resource failed for some
	 *                                       reason.
	 * @throws DSGRESTRemoteException        if an error related to the
	 *                                       communication system occurred.
	 * @throws UnsupportedOperationException if the resource does not support GET
	 *                                       requests.
	 */
	DSGRESTRepresentation get(DSGRESTContext ctx, DSGHTTPHeader header) throws DSGRESTException, DSGRESTRemoteException;

	/**
	 * Return metadata (i.e. HTTP headers), about the selected representation of the
	 * resource.
	 *
	 * @param ctx    Context associated with this REST request.
	 * @param header the HTTP headers associated with the request.
	 *
	 * @return the metadata of the resource.
	 *
	 * @throws DSGRESTException              if this resource does not exist.
	 * @throws DSGRESTRemoteException        if an error related to the
	 *                                       communication system occurred.
	 * @throws UnsupportedOperationException if the resource does not support HEAD
	 *                                       requests.
	 */
	DSGRESTRepresentation head(DSGRESTContext ctx, DSGHTTPHeader header)
			throws DSGRESTException, DSGRESTRemoteException;

	/**
	 * Request that the state of this resource be created or replaced with the state
	 * represented by {@code resource}.
	 *
	 * If the resource has been created as a result of the call to put, then this
	 * method <strong>MUST</strong> return a representation with a status of
	 * {@code DSGHTTPStatus.CREATED}.
	 *
	 * @param ctx      the context associated with this request.
	 * @param resource the representation of the resource to create or update.
	 *
	 * @return an (optional) representation of the status of the action.
	 *
	 * @throws DSGRESTException              if this resource does not exist.
	 * @throws DSGRESTException              if the incoming representation in
	 *                                       incompatible with this resource.
	 * @throws DSGRESTException              if creating or updating the resource
	 *                                       failed for some reason.
	 * @throws DSGRESTRemoteException        if an error related to the
	 *                                       communication system occurred.
	 * @throws UnsupportedOperationException if the resource does not support PUT
	 *                                       requests.
	 */
	DSGRESTRepresentation put(DSGRESTContext ctx, DSGRESTRepresentation resource)
			throws DSGRESTException, DSGRESTRemoteException;

	/**
	 * Request that this resource processes {@code resource} according to its
	 * specific semantics.
	 *
	 * @param ctx      the context associated with this request.
	 * @param resource the representation to process.
	 *
	 * @return the status or result of processing {@code resource}.
	 *
	 * @throws DSGRESTException              if this resource does not exist.
	 * @throws DSGRESTException              if this resource does not know how to
	 *                                       process {@code resource}.
	 * @throws DSGRESTException              if the request failed for some other
	 *                                       reason.
	 * @throws DSGRESTRemoteException        if an error related to the
	 *                                       communication system occurred.
	 * @throws UnsupportedOperationException if the resource does not support POST
	 *                                       requests.
	 */
	DSGRESTRepresentation post(DSGRESTContext ctx, DSGRESTRepresentation resource)
			throws DSGRESTException, DSGRESTRemoteException;

	/**
	 * Request the removal of this resource.
	 *
	 * @param ctx the context associated with this request.
	 *
	 * @return an (optional) representation of the status of the action.
	 *
	 * @throws DSGRESTException              if this resource does not exist.
	 * @throws DSGRESTException              if this resource cannot be deleted for
	 *                                       some reason.
	 * @throws DSGRESTRemoteException        if an error related to the
	 *                                       communication system occurred.
	 * @throws UnsupportedOperationException if the resource does not support DELETE
	 *                                       requests.
	 */
	DSGRESTRepresentation delete(DSGRESTContext ctx) throws DSGRESTException, DSGRESTRemoteException;
}

package dsg.activitypub;

import java.io.IOException;

import dsg.activitystreams.DSGActivityStreamsEntity;
import dsg.activitystreams.DSGActivityStreamsObject;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGStandardMediaTypes;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRemoteException;
import dsg.rest.DSGRESTRepresentation;
import dsg.rest.DSGRESTStub;

/**
 * The client-side surrogate of a remote ActivityPub object.
 */
public class DSGActivityPubObjectStub implements DSGActivityPubObject {

    /** The REST stub used to issue requests. */
    private DSGRESTStub stub;

    /**
     * Initialize a client-stub for a remote ActivityPub object.
     *
     * @param stub the REST stub pointing to the target object.
     */
    public DSGActivityPubObjectStub(DSGRESTStub stub) {
        this.stub = stub;
    }

    @Override
    public DSGActivityStreamsObject get(DSGActivityPubActor actor)
            throws DSGActivityPubAuthorizationException, DSGActivityPubException {
        try {
            DSGHTTPHeader header = new DSGHTTPHeader();
            if (actor != null && actor.getAuthorization() != null) {
                header.set("Authorization", actor.getAuthorization());
            }
            DSGRESTContext ctx = new DSGRESTContext();
            ctx.addAcceptedRepresentation(DSGStandardMediaTypes.ACTIVITY_STREAMS);
            DSGRESTRepresentation resource;
            try {
                resource = stub.get(ctx, header);
            } catch (DSGRESTRemoteException e) {
                throw new DSGActivityPubException(e);
            }
            if (resource.getResource() == null) {
                return null;
            }
            try (DSGActivityPubReader reader = new DSGActivityPubReader(resource)) {
                DSGActivityStreamsEntity entity = reader.read();
                if (!(entity instanceof DSGActivityStreamsObject)) {
                    throw new DSGActivityPubException("Server responded not with an object");
                }
                return (DSGActivityStreamsObject) entity;
            } catch (IOException e) {
                throw new DSGActivityPubException(e);
            }
        } catch (DSGRESTException re) {
            switch (re.getStatus()) {
            case DSGHTTPStatus.BAD_REQUEST:
                throw new IllegalArgumentException();
            case DSGHTTPStatus.UNAUTHORIZED:
                if (actor != null && actor.getAuthorization() == null) {
                    throw new DSGActivityPubAuthorizationException(actor.getName());
                }
            case DSGHTTPStatus.NOT_FOUND:
            case DSGHTTPStatus.GONE:
                return null;
            default:
                throw new DSGActivityPubException("An unexpected error occurred");
            }
        }
    }

}

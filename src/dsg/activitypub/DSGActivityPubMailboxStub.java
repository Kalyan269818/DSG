package dsg.activitypub;

import java.io.IOException;
import java.net.URI;

import dsg.activitystreams.DSGActivityStreamsActivity;
import dsg.activitystreams.DSGActivityStreamsCollection;
import dsg.activitystreams.DSGActivityStreamsEntity;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGStandardMediaTypes;
import dsg.json.DSGJSONValue;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRemoteException;
import dsg.rest.DSGRESTRepresentation;
import dsg.rest.DSGRESTStub;

/**
 * Stub for transparently accessing ActivityPub mailboxes on remote servers.
 */
public class DSGActivityPubMailboxStub implements DSGActivityPubMailbox {

    /** The REST stub used to issue requests. */
    private DSGRESTStub stub;

    // ##################
    // # INITIALIZATION #
    // ##################

    /**
     * Initialize a mailbox stub with an existing REST stub.
     *
     * @param stub the REST stub pointing to the target object.
     */
    public DSGActivityPubMailboxStub(DSGRESTStub stub) {
        this.stub = stub;
    }

    // ######################
    // # MAILBOX OPERATIONS #
    // ######################

    @SuppressWarnings("unchecked")
    @Override
    public DSGActivityStreamsCollection<DSGActivityStreamsActivity> fetch(DSGActivityPubActor actor)
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
                if (!(entity instanceof DSGActivityStreamsCollection)) {
                    throw new DSGActivityPubException("Server responded with non-object");
                }
                return (DSGActivityStreamsCollection<DSGActivityStreamsActivity>) entity;
            } catch (IOException e) {
                throw new DSGActivityPubException(e);
            }
        } catch (DSGRESTException re) {
            switch (re.getStatus()) {
            case BAD_REQUEST:
                throw new IllegalArgumentException(re);
            case UNAUTHORIZED:
                throw new DSGActivityPubAuthorizationException();
            case NOT_FOUND:
            case GONE:
                return null;
            default:
                throw new DSGActivityPubException(re);
            }
        }
    }

    @Override
    public URI deliver(DSGActivityPubActor actor, DSGActivityStreamsActivity activity)
            throws DSGActivityPubAuthorizationException, DSGActivityPubException {
        // TODO Implement method
        return null;
    }

}

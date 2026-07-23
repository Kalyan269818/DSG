package dsg.activitypub;

/**
 * A service that authenticates ActivityPub actors to the system.
 */
public interface DSGActivityPubAuthenticator {
    /**
     * Authenticate an ActivityPub actor based on authentication credentials
     * provided by the user.
     *
     * @param authorization the actor credentials supplied by the user.
     * @return the ActivityPub actor associated with the credentials or null, if
     *         {@code authorization} was null or no user for the given credentials
     *         exists.
     * @throws DSGActivityPubException              if there are any issues while
     *                                              trying to authenticate the
     *                                              actor.
     * @throws DSGActivityPubAuthorizationException if the user credentials are
     *                                              invalid.
     */
    DSGActivityPubActor authenticate(String authorization)
            throws DSGActivityPubException, DSGActivityPubAuthorizationException;
}

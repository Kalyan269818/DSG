package dsg.microblog;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Encoder;

import dsg.activitypub.DSGActivityPubAuthenticator;
import dsg.activitypub.DSGActivityPubAuthorizationException;
import dsg.activitypub.DSGActivityPubException;

/**
 * Access-control service using HTTP Basic Authentication.
 */
public class DSGMicroBlogAuthenticator implements DSGActivityPubAuthenticator {

    /**
     * Prefix for the HTTP Authorization header when using HTTP basic
     * authentication.
     */
    private static final String BASIC_AUTH_PREFIX = "Basic ";

    /** The base URI of the local micro blog service. */
    private final URI baseURI;
    /** The persistent storage storing user data. */
    private final DSGMicroBlogStorage storage;

    /**
     * Initialize an authenticator for the micro blog.
     *
     * @param baseURI the base URI of the local server.
     * @param storage the storage instance of this server.
     */
    public DSGMicroBlogAuthenticator(URI baseURI, DSGMicroBlogStorage storage) {
        this.baseURI = baseURI;
        this.storage = storage;
    }

    /**
     * Return an authorization string from a username and password that is
     * compatible with this authenticator.
     *
     * @param username the user's username.
     * @param password the user's password in plaintext.
     * @return an authorization string encoding username and password.
     */
    public String createAuthorization(String username, String password) {
        byte[] payload = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
        String authorization = BASIC_AUTH_PREFIX + Base64.getEncoder().encodeToString(payload);
        return authorization;
    }

    @Override
    public DSGMicroBlogUser authenticate(String authorization)
            throws DSGActivityPubAuthorizationException, DSGActivityPubException {
        if (authorization == null) {
            return null;
        }
        if (!authorization.startsWith(BASIC_AUTH_PREFIX)) {
            throw new IllegalArgumentException("authorization is not a valid basic auth string");
        }

        authorization = authorization.substring(BASIC_AUTH_PREFIX.length());
        byte[] bytes = Base64.getDecoder().decode(authorization);
        authorization = new String(bytes, StandardCharsets.UTF_8);
        String[] credentials = authorization.split(":", 2);
        if (credentials.length != 2) {
            throw new IllegalArgumentException("malformed basic auth string");
        }

        String username = credentials[0];
        String password = credentials[1];
        URI id = DSGMicroBlogUser.idForName(baseURI, username);
        try {
            DSGMicroBlogUser user = storage.getUser(id);
            if (user == null) {
                return null;
            }
            String hash = user.getPassword();
            if (!DSGMicroBlogAuthenticator.match(password, hash)) {
                throw new DSGActivityPubAuthorizationException(username);
            }
            // Remove password hash from the object so it is not leaked.
            user.setPassword(null);
            return user;
        } catch (IOException e) {
            throw new DSGActivityPubException(e);
        }
    }

    /**
     * Return, whether the given {@code password} and {@code hash} match.
     *
     * @param password the password to match with {@code hash}.
     * @param hash     a password hash.
     * @return true, if the password matches the hash, false otherwise.
     * @throws IllegalArgumentException if {@code hash} is not a valid password
     *                                  hash.
     */
    public static boolean match(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }

        String[] parts = hash.split("\\$");
        if (parts.length != 4) {
            throw new IllegalArgumentException("invalid hash");
        }
        if (!parts[1].equals("6")) {
            throw new IllegalArgumentException("invalid algorithm");
        }

        String salt = parts[2];
        byte[] saltBytes = Base64.getDecoder().decode(salt);
        String gotHash = hashPassword(saltBytes, password.getBytes());
        return MessageDigest.isEqual(gotHash.getBytes(), hash.getBytes());
    }

    /**
     * Return a hash of the given {@code password} with a random salt.
     *
     * @param password the password in plaintext.
     * @return the hashed password.
     */
    public static String hashPassword(String password) {
        byte[] salt = generateSalt();
        return hashPassword(salt, password.getBytes());
    }

    /**
     * Return a hash for the given {@code salt} and {@code password}.
     *
     * @param salt     the salt added to the password hash.
     * @param password the password in plaintext.
     * @return the hashed version of the password.
     */
    public static String hashPassword(byte[] salt, byte[] password) {
        final int rounds = 5000;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-512");
            sha.update(salt);
            byte[] hash = sha.digest(password);
            for (int i = 0; i < rounds; i++) {
                hash = sha.digest(hash);
            }
            Encoder encoder = Base64.getEncoder();
            return String.format("$6$%s$%s", encoder.encodeToString(salt), encoder.encodeToString(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generate a random salt which can be used for hashing passwords.
     *
     * @return the random salt.
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[12];
        SecureRandom rng = new SecureRandom();
        rng.nextBytes(salt);
        return salt;
    }
}

package dsg.microblog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import dsg.activitypub.DSGActivityPubAuthorizationException;
import dsg.activitypub.DSGActivityPubException;
import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPHandler;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPMethod;
import dsg.http.DSGHTTPRequest;
import dsg.http.DSGHTTPRequestException;
import dsg.http.DSGHTTPResponse;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGStandardMediaTypes;
import dsg.rest.DSGRESTSkeleton;

/**
 * HTTPHandler for the micro blog service.
 *
 * This handler multiplexes between a static file server and the ActivityPub
 * skeleton that you implemented.
 */
public class DSGMicroBlogHandler implements DSGHTTPHandler {

    /**
     * Path to the directory where static files (HTML, CSS, JavaScript) are located.
     */
    private String staticFilesDirectory;
    /** Authenticator used for the login page. */
    private DSGMicroBlogAuthenticator authenticator;
    /** REST skeleton used to route requests to ActivityPub resources. */
    private DSGRESTSkeleton router;

    /**
     * Initialize a micro blog HTTP handler.
     *
     * @param staticFilesDirectory the local directory where static files reside.
     * @param authenticator        the authenticator used to check user's
     *                             authorization information.
     * @param router               the REST skeleton used to route requests to
     *                             ActivityPub resources.
     */
    public DSGMicroBlogHandler(String staticFilesDirectory, DSGMicroBlogAuthenticator authenticator,
            DSGRESTSkeleton router) {
        this.staticFilesDirectory = staticFilesDirectory;
        this.authenticator = authenticator;
        this.router = router;
    }

    @Override
    public DSGHTTPResponse handle(DSGHTTPRequest request) throws DSGHTTPException, DSGHTTPRequestException {
        DSGHTTPMethod method = request.getMethod();
        System.out.println("[HANDLER] " + method + " " + request.getTarget());
        switch (method) {
        case OPTIONS:
            // We might have to handle OPTIONS request sent by clients before JavaScript
            // is allowed to fetch cross-origin reasources. For ActivityPub content, we want
        case HEAD:
        case GET:
            switch (request.getTarget().getPath()) {
            case LOGIN_PATH:
                return login(request);
            case REGISTER_PATH:
                return register(request);
            default:
                // Default to serving static files on GET and HEAD, unless the client requests
                // an ActivityStreams response.
                DSGHTTPHeader requestHeader = request.getHeader();
                String accepts = requestHeader.get("Accept");
                if (accepts == null) {
                    return serveStaticFile(request);
                }
                DSGHTTPMediaType acceptedType;
                try {
                    acceptedType = new DSGHTTPMediaType(accepts);
                } catch (DSGHTTPException e) {
                    throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST);
                }
                if (!acceptedType.getFullType().equals(DSGStandardMediaTypes.ACTIVITY_STREAMS.getFullType())) {
                    System.out.println("[HANDLER] Serve static file");
                    return serveStaticFile(request);
                }
            }
            /* FALLTHROUGH */
        default:
            // Maybe we have to make sure we allow Cross-Origin-Requests for fetching
            // ActivityPub Data.
            // DSGHTTPHeader responseHeader = response.getHeader();
            // responseHeader.add("Access-Control-Allow-Origin", "*");
            System.out.println("[HANDLER] Forwarding request to REST skeleton");
            DSGHTTPResponse response = router.handle(request);
            return response;
        }

    }

    private static final String LOGIN_PATH = "/login";

    /**
     * Check the user's login information and instruct the browser to show a login
     * dialog, if the user is not signed in.
     */
    private DSGHTTPResponse login(DSGHTTPRequest request) throws DSGHTTPException, DSGHTTPRequestException {
        try {
            String authorization = request.getHeader().get("Authorization");
            DSGMicroBlogUser user = authenticator.authenticate(authorization);
            if (user == null) {
                DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.UNAUTHORIZED);
                response.getHeader().set("WWW-Authenticate", "Basic realm=DSGMicroBlog");
                return response;
            }
            DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.SEE_OTHER);
            response.getHeader().set("Location", "/");
            return response;
        } catch (DSGActivityPubAuthorizationException apae) {
            DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.UNAUTHORIZED);
            response.getHeader().set("WWW-Authenticate", "Basic realm=DSGMicroBlog");
            return response;
        } catch (DSGActivityPubException ape) {
            ape.printStackTrace();
            throw new DSGHTTPException(ape.getMessage());
        }
    }

    private static final String REGISTER_PATH = "/register.html";

    /**
     * Show the registration form if the user is not already signed in.
     */
    private DSGHTTPResponse register(DSGHTTPRequest request) throws DSGHTTPException, DSGHTTPRequestException {
        try {
            String authorization = request.getHeader().get("Authorization");
            DSGMicroBlogUser user = authenticator.authenticate(authorization);
            if (user == null) {
                return serveStaticFile(request);
            }

            // Redirect to main page if already signed in.
            DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.SEE_OTHER);
            response.getHeader().set("Location", "/");
            return response;
        } catch (DSGActivityPubAuthorizationException apae) {
            return serveStaticFile(request);
        } catch (DSGActivityPubException ape) {
            ape.printStackTrace();
            throw new DSGHTTPException(ape.getMessage());
        }
    }

    private static final String INDEX_PATH = "/index.html";

    /**
     * Serve a static file based on the incoming {@code request}.
     *
     * @param request the request which will be served a static file.
     *
     * @throws DSGHTTPException        if an error occurs while reading the file's
     *                                 content.
     * @throws DSGHTTPRequestException if the file does not exist or the client is
     *                                 not allowed to access it.
     */
    private DSGHTTPResponse serveStaticFile(DSGHTTPRequest request) throws DSGHTTPException, DSGHTTPRequestException {
        String requestPath = request.getTarget().getPath();
        if (requestPath.equals("/") || router.isExported(requestPath)) {
            requestPath = INDEX_PATH;
        } else if (requestPath.endsWith("/")) {
            // Redirect the browser to paths without slashes
            requestPath = requestPath.replaceAll("/+$", "");
            DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.PERMANENT_REDIRECT);
            response.getHeader().set("Location", requestPath);
            return response;
        }

        String path = join(staticFilesDirectory, requestPath);
        File file = new File(path);
        DSGHTTPStatus status = DSGHTTPStatus.OK;
        // In error error cases we have to fall back to our index page so that the
        // single-page application works as expected.
        if (!file.exists()) {
            throw new DSGHTTPRequestException(DSGHTTPStatus.NOT_FOUND);
        }
        // Only serve files, not directory content.
        if (file.isDirectory()) {
            throw new DSGHTTPRequestException(DSGHTTPStatus.FORBIDDEN);
        }

        DSGHTTPMediaType documentType = DSGStandardMediaTypes.typeForExtension(getExtension(requestPath));
        DSGHTTPResponse response;
        switch (request.getMethod()) {
        case GET:
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                Files.copy(file.toPath(), output);
                response = new DSGHTTPResponse(status, documentType, new ByteArrayInputStream(output.toByteArray()));
            } catch (FileNotFoundException fne) {
                throw new DSGHTTPRequestException(DSGHTTPStatus.NOT_FOUND);
            } catch (IOException e) {
                throw new DSGHTTPException("Failed to read file");
            }
            break;
        case HEAD:
            try {
                long size = Files.size(file.toPath());
                response = new DSGHTTPResponse(status);
                DSGHTTPHeader header = response.getHeader();
                header.set("Content-Length", "" + size);
            } catch (FileNotFoundException fne) {
                throw new DSGHTTPRequestException(DSGHTTPStatus.NOT_FOUND);
            } catch (IOException e) {
                throw new DSGHTTPException("Failed to access file");
            }
            break;
        default:
            throw new DSGHTTPRequestException(DSGHTTPStatus.BAD_REQUEST);
        }
        return response;
    }

    /**
     * Securely joins root and path components to form a path of the form
     * "{@code root}/{@code path}".
     *
     * More specifically, {@code path} is normalized in such a manner that it cannot
     * break out of the named filesystem {@code root} before joining both
     * components.
     *
     * @param root the root path component.
     * @param path the path to be securely appended to {@code root}.
     * @return the securely joined path.
     */
    private String join(String root, String path) {
        // Transform the incoming path into an absolute path and clean it to
        // to prevent directory traversal attacks.
        // First transform the path into an absolute path.
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return clean(root + "/" + clean(path));
    }

    /**
     * Clean a path by following the algorithm originally described by Rob Pike for
     * the Plan 9 operating system. More specifically, the method iteratively
     * applies the following rules:
     *
     * <ol>
     * <li>Reduce multiple slashes to a single slash.</li>
     * <li>Eliminate . path name elements (the current directory).</li>
     * <li>Eliminate .. path name elements (the parent directory) and the non-.
     * non-.., element that precedes them.</li>
     * <li>Eliminate .. elements that begin a rooted path, that is, replace /.. by /
     * at the beginning of a path.</li>
     * <li>Leave intact .. elements that begin a non-rooted path</li>
     * </ol>
     *
     * @param path the path to be cleaned.
     * @return the cleaned path.
     *
     * @see <a href="https://9p.io/sys/doc/lexnames.html">Rob Pike: Lexical File
     *      Names in Plan 9 or Getting Dot-Dot Right</a>
     */
    private String clean(String path) {
        if (path.equals("")) {
            return ".";
        }

        boolean rooted = path.charAt(0) == '/';
        int len = path.length();
        StringBuilder builder = new StringBuilder();

        int i = 0;
        // Stable characters, i.e. the position where we are not allowed to remove
        // elements when encountering a ".." element.
        int stable = 0;
        if (rooted) {
            builder.append('/');
            i = 1;
            stable = 1;
        }
        while (i < len) {
            switch (path.charAt(i)) {
            case '/':
                // Rule 1: Reduce multiple slashes to single slash.
                i++;
                break;
            case '.':
                // Rule 2: Eliminate . path elements.
                if (i + 1 == len) {
                    i++;
                    continue;
                }
                char next = path.charAt(i + 1);
                if (next == '/') {
                    i++;
                    continue;
                }

                if (next == '.' && (i + 2 == len || path.charAt(i + 2) == '/')) {
                    i += 2;
                    // Rule 3: Eliminate .. path name elements (the parent directory) and the non-.
                    // non-.., element that precedes them.
                    if (stable < builder.length()) {
                        int newLength = builder.length() - 2;
                        while (newLength > stable && builder.charAt(newLength) != '/') {
                            newLength--;
                        }
                        builder.setLength(newLength);
                        continue;
                    }
                    // Rule 4: Eliminate .. elements that begin a rooted path, that is, replace /..
                    // by / at the beginning of a path.
                    if (!rooted) {
                        // Rule 5: Leave intact .. elements that begin a non-rooted path.
                        if (builder.length() > 0) {
                            builder.append('/');
                        }
                        builder.append("..");
                        stable = builder.length();
                    }
                    continue;
                }
                /* FALLTHROUGH */
            default:
                // Actual path element, append a slash if necessary and then copy it.
                if (rooted && builder.length() != 1 || !rooted && builder.length() != 0) {
                    builder.append('/');
                }
                for (; i < len; i++) {
                    char c = path.charAt(i);
                    if (c == '/') {
                        break;
                    }
                    builder.append(c);
                }
            }
        }
        return builder.toString();
    }

    /**
     * Return the named path's filename, that is the component following the final
     * slash.
     *
     * @param path the full path to the file or directory.
     * @return the name component of the path or null if path is the root node
     *         ("/").
     */
    private String getName(String path) {
        if (path.equals("/")) {
            return "/";
        }
        int i = path.lastIndexOf('/');
        if (i != -1) {
            path = path.substring(i + 1);
        }
        return path;
    }

    /**
     * Return named file's extension, including the preceding dot
     *
     * @param path the full path to the the file.
     * @return the file's extension or an empty string, if it does not have any.
     */
    private String getExtension(String path) {
        String name = getName(path);
        int i = name.lastIndexOf('.');
        if (i == -1) {
            return "";
        }
        return name.substring(i);
    }

}

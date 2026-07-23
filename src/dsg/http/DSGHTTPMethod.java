package dsg.http;

/**
 * HTTP Request Methods.
 *
 * @see <a href="https://www.rfc-editor.org/rfc/rfc9110.html#name-methods">RFC
 *      9110</a>
 */
public enum DSGHTTPMethod {
    //@formatter:off
    /** Request transfer of a selected representation of the resource. */
    GET,
    /** Obtain metadata about the selected representation of the resource. */
    HEAD,
    /** Instruct target to process representation enclosed in the request. */
    POST,
    /** Replace the state of the resource with the enclosed representation. */
    PUT,
    /** Remove the target resource. */
    DELETE,
    /** Request the server to establish a tunnel to a destination server. */
    CONNECT,
    /** Request information about the available communication options. */
    OPTIONS,
    /** Request an application-level loop-back of the request.  */
    TRACE;

    //@formatter:on
    /**
     * Return an HTTP method from its name.
     *
     * @param name the method name.
     * @return the HTTP method with the given {@code name} or null if it does not
     *         exist.
     */
    public static DSGHTTPMethod forName(String name) {
        for (DSGHTTPMethod method : DSGHTTPMethod.values()) {
            if (name.equals(method.toString())) {
                return method;
            }
        }
        return null;
    }
}

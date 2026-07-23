package dsg.http;

/**
 * HTTP Status Codes.
 *
 * @see <a href=
 *      "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">RFC
 *      9110</a>
 */
public enum DSGHTTPStatus {
        //@formatter:off
        /* Informational */
        /** Initial part of a request has been successfully received. */
        CONTINUE(100),
        /**
         * The server agrees to switch the application-level protocol used on
         * this connection.
         */
        SWITICHING_PROTOCOLS(101),

        /* Successful */
        /** Request has succeeded. */
        OK(200),
        /**
         * The request was successful and has caused one or more resource to be
         * created.
         */
        CREATED(201),
        /**
         * The request has been accepted for processing, but has not been
         * completed.
         */
        ACCEPTED(202),
        /**
         * The request was successful, but the enclosed content has been
         * modified from that of the origin server's.
         */
        NON_AUTHORATIVE_INFORMATION(203),
        /**
         * The request was successful and there is no additional content to
         * send in response to the request.
         */
        NO_CONTENT(204),
        /**
         * The request was successful and the server wants the client to reset
         * the "document view" (e.g. an HTML form) which initiated the request.
         */
        RESET_CONTENT(205),
        /**
         * The server successfully fulfilled a range request for the target
         * resource.
         */
        PARTIAL_CONTENT(206),

        /* Redirection */
        /** The target resource has more than one representation. */
        MULTIPLE_CHOICES(300),
        /**
         * The target resource has been assigned a new URI that should be used
         * for future requests.
         */
        MOVED_PERMANENTLY(301),
        /** The target resource resides temporarily under a different URI. */
        FOUND(302),
        /** The server is redirecting the user to a different resource. */
        SEE_OTHER(303),
        /**
         * The resource has not been modified relative to the last version the
         * client knows of.
         */
        NOT_MODIFIED(304),
        /**
         * The 305 (Use Proxy) status code was defined in a previous version of
         * this specification and is now deprecated.
         * @deprecated
         */
        @Deprecated
        USE_PROXY(305),
        /**
         * The target resource temporarily resides under a different URI and
         * the client MUST NOT change the request method on automatic
         * redirection.
         */
        TEMPORARY_REDIRECT(307),
        /**
         * The target resource has been permanently assigned a new URI that
         * should be used on all future requests and the client MUST NOT
         * change the request method on automatic redirection.
         */
        PERMANENT_REDIRECT(308),

        /* Client Errors */
        /** The client request is invalid. */
        BAD_REQUEST(400),
        /** The request lacks valid authentication. */
        UNAUTHORIZED(401),
        /** Reserved for future use. */
        PAYMENT_REQUIRED(402),
        /** The server refuses to fulfill the request. */
        FORBIDDEN(403),
        /**
         * The server did not find a current representation for the target
         * resource or is not willing to disclose that it exists.
         */
        NOT_FOUND(404),
        /** The request method is not supported by the target resource. */
        METHOD_NOT_ALLOWED(405),
        /**
         * The target resource does not have a representation that satisfies
         * the client's list of accepted representation.
         */
        NOT_ACCEPTABLE(406),
        /**
         * The client needs to authenticate itself in order to use a proxy for
         * this request.
         */
        PROXY_AUTHENTICIATION_REQUIRED(407),
        /**
         * The server did not receive a complete request message within the
         * time it was willing to wait.
         */
        REQUEST_TIMEOUT(408),
        /**
         * The request could not be completed due to a conflict with the
         * current state of the target resource.
         */
        CONFLICT(409),
        /**
         * Access to the resource is no longer available and this condition is
         * likely to be permanent.
         */
        GONE(410),
        /**
         * The servers refuses to accept requests without a defined
         * Content-Length.
         */
        LENGTH_REQUIRED(411),
        /**
         * One or more conditions given in the request header failed.
         */
        PRECONDITION_FAILED(412),
        /**
         * The server is refusing to process a request, because the request
         * content is larger than the server is willing or able to process.
         */
        CONTENT_TOO_LARGE(413),
        /**
         * The server refuses to process the request, because the target URI
         * is larger than the server is willing or able to process.
         */
        URI_TOO_LONG(414),
        /** The server does not support the format of the request content. */
        UNSUPPORTED_MEDIA_TYPE(415),
        /**
         * The set of ranges in the request's Range header field has been
         * rejected.
         */
        RANGE_NOT_SATISFIABLE(416),
        /**
         * The exception given in the request's Expectation header field could
         * not be met.
         */
        EXPECTATION_FAILED(417),

        /**
         * The request was directed to a server that is unable or unwilling to
         * produce an authoritative response for the target URI.
         */
        MISDIRECTED_REQUEST(421),
        /**
         * The server is unable to process the instructions contained in the
         * request content, although it understands the content's media type.
         */
        UNPROCESSABLE_CONTENT(422),
        /**
         * The server refuses to process the request using the current
         * protocol, but might be willing to do so after the client upgrades
         * to a different protocol.
         */
        UPGRADE_REQUIRED(426),

        /* Server Errors */
        /**
         * The server encountered an unexpected condition that prevented it
         * from fulfilling the request.
         */
        INTERNAL_SERVER_ERROR(500),
        /** The server does not recognize the request method. */
        NOT_IMPLEMENTED(501),
        /**
         * The server acts as a gateway or proxy and received an invalid
         * response from an inbound server.
         */
        BAD_GATEWAY(502),
        /**
         * The server is currently unable to process the request due to a
         * temporary overload or scheduled maintenance.
         */
        SERVICE_UNAVAILABLE(503),
        /**
         * The server acts as a gateway or proxy and did not receive a timely
         * response from the upstream server it contacted to fulfill the
         * request.
         */
        GATEWAY_TIMEOUT(504),
        /** The server does not support the request's HTTP version. */
        HTTP_VERSION_NOT_SUPPORTED(505);

        //@formatter:on
        private int code;

        private DSGHTTPStatus(int code) {
                this.code = code;
        }

        /**
         * Return the {@link DSGHTTPStatus} for the named status {@code code}.
         *
         * @param code the status code.
         * @return the appropriate {@link DSGHTTPStatus} or null, if no status with the
         *         given {@code code} exists.
         */
        public static DSGHTTPStatus forCode(int code) {
                for (DSGHTTPStatus status : DSGHTTPStatus.values()) {
                        if (code == status.code) {
                                return status;
                        }
                }
                return null;
        }

        /**
         * Return this status' code.
         *
         * @return the status code.
         */
        public int getCode() {
                return this.code;
        }

        /**
         * Return a human-readable message describing this status.
         *
         * @return a status message.
         */
        public String getMessage() {
                return this.toString();
        }

        @Override
        public String toString() {
                switch (this) {
                case ACCEPTED:
                        return "Accepted";
                case BAD_GATEWAY:
                        return "Bad Gateway";
                case BAD_REQUEST:
                        return "Bad request";
                case CONFLICT:
                        return "Conflict";
                case CONTENT_TOO_LARGE:
                        return "Content too large";
                case CONTINUE:
                        return "Continue";
                case CREATED:
                        return "Created";
                case EXPECTATION_FAILED:
                        return "Expectation failed";
                case FORBIDDEN:
                        return "Forbidden";
                case FOUND:
                        return "Found";
                case GATEWAY_TIMEOUT:
                        return "Gateway timeout";
                case GONE:
                        return "Gone";
                case HTTP_VERSION_NOT_SUPPORTED:
                        return "HTTP version not supported";
                case INTERNAL_SERVER_ERROR:
                        return "Internal server error";
                case LENGTH_REQUIRED:
                        return "Length required";
                case METHOD_NOT_ALLOWED:
                        return "Method not allowed";
                case MISDIRECTED_REQUEST:
                        return "Misdirected request";
                case MOVED_PERMANENTLY:
                        return "Moved permanently";
                case MULTIPLE_CHOICES:
                        return "Multiple choices";
                case NON_AUTHORATIVE_INFORMATION:
                        return "Non authorative information";
                case NOT_ACCEPTABLE:
                        return "Not acceptable";
                case NOT_FOUND:
                        return "Not found";
                case NOT_IMPLEMENTED:
                        return "Not implemented";
                case NOT_MODIFIED:
                        return "Not modified";
                case NO_CONTENT:
                        return "No content";
                case OK:
                        return "OK";
                case PARTIAL_CONTENT:
                        return "Partial content";
                case PAYMENT_REQUIRED:
                        return "Payment required";
                case PERMANENT_REDIRECT:
                        return "Permanent redirect";
                case PRECONDITION_FAILED:
                        return "Precondition failed";
                case PROXY_AUTHENTICIATION_REQUIRED:
                        return "Proxy authentication required";
                case RANGE_NOT_SATISFIABLE:
                        return "Range not satisfiable";
                case REQUEST_TIMEOUT:
                        return "Request timeout";
                case RESET_CONTENT:
                        return "Reset content";
                case SEE_OTHER:
                        return "See other";
                case SERVICE_UNAVAILABLE:
                        return "Service unavailable";
                case SWITICHING_PROTOCOLS:
                        return "Switching protocols";
                case TEMPORARY_REDIRECT:
                        return "Temporary redirect";
                case UNAUTHORIZED:
                        return "Unauthorized";
                case UNPROCESSABLE_CONTENT:
                        return "Unprocessable content";
                case UNSUPPORTED_MEDIA_TYPE:
                        return "Unsupported media type";
                case UPGRADE_REQUIRED:
                        return "Upgrade required";
                case URI_TOO_LONG:
                        return "URI too long";
                case USE_PROXY:
                        return "Use proxy";
                default:
                        return "Unknown";
                }
        }
}

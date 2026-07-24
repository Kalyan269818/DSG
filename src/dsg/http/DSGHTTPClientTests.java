package dsg.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import dsg.network.DSGCall.DSGCallType;
import dsg.network.DSGMessage;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@TestMethodOrder(OrderAnnotation.class)
public class DSGHTTPClientTests {
    // #########
    // # TESTS #
    // #########

    private static final URI TARGET_RESOURCE;
    private static final URI REDIRECT_RESOURCE;
    static {
        try {
            TARGET_RESOURCE = new URI("http://localhost:8000/");
            REDIRECT_RESOURCE = TARGET_RESOURCE.resolve("/other/resource");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    @Order(0)
    void simpleGETRequest() throws IOException {
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE);
        wantRequest.getHeader().set("Connection", "close");
        InetSocketAddress wantAddress = new InetSocketAddress(TARGET_RESOURCE.getHost(), TARGET_RESOURCE.getPort());
        DSGHTTPResponse wantResponse = new DSGHTTPResponse(DSGHTTPStatus.NO_CONTENT);
        DSGMockClientOperation[] wantOperations = new DSGMockClientOperation[] {
                new DSGMockClientOperation(DSGCallType.CONNECT, wantAddress),
                new DSGMockClientOperation(wantAddress, wantRequest),
                new DSGMockClientOperation(wantAddress, wantResponse),
                new DSGMockClientOperation(DSGCallType.CLOSE, wantAddress) };

        DSGMockHTTPClient client = new DSGMockHTTPClient(wantOperations);
        try {
            DSGHTTPResponse gotResponse = client.communicate(new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE));
            DSGHTTPTestUtil.assertResponsesMatch(wantResponse, gotResponse);
            client.assertAllOperationsExecuted();
        } finally {
            client.shutdown();
        }
    }

    @Test
    @Order(1)
    void simplePOSTRequestWithPayload() throws IOException {
        DSGHTTPHeader wantHeader = new DSGHTTPHeader();
        wantHeader.set("Connection", "close");
        wantHeader.set("Content-Type", DSGStandardMediaTypes.PLAINTEXT.toString());
        byte[] wantPayload = "Hello, World!".getBytes(StandardCharsets.US_ASCII);
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.POST, wantHeader, TARGET_RESOURCE,
                new ByteArrayInputStream(wantPayload));

        InetSocketAddress wantAddress = new InetSocketAddress(TARGET_RESOURCE.getHost(), TARGET_RESOURCE.getPort());
        DSGHTTPResponse wantResponse = new DSGHTTPResponse(DSGHTTPStatus.NO_CONTENT);
        DSGMockClientOperation[] wantOperations = new DSGMockClientOperation[] {
                new DSGMockClientOperation(DSGCallType.CONNECT, wantAddress),
                new DSGMockClientOperation(wantAddress, wantRequest),
                new DSGMockClientOperation(wantAddress, wantResponse),
                new DSGMockClientOperation(DSGCallType.CLOSE, wantAddress) };

        DSGMockHTTPClient client = new DSGMockHTTPClient(wantOperations);
        try {
            DSGHTTPResponse gotResponse = client.communicate(new DSGHTTPRequest(DSGHTTPMethod.POST,
                    DSGHTTPTestUtil.cloneHeader(wantHeader), TARGET_RESOURCE, new ByteArrayInputStream(wantPayload)));
            DSGHTTPTestUtil.assertResponsesMatch(wantResponse, gotResponse);
            client.assertAllOperationsExecuted();
        } finally {
            client.shutdown();
        }
    }

    @Test
    @Order(2)
    void clientFollowsRedirectIfInstructed() throws IOException {
        DSGHTTPRequest redirectedRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE);
        redirectedRequest.getHeader().set("Connection", "close");
        InetSocketAddress wantAddress = new InetSocketAddress(TARGET_RESOURCE.getHost(), TARGET_RESOURCE.getPort());
        DSGHTTPResponse redirectResponse = new DSGHTTPResponse(DSGHTTPStatus.SEE_OTHER);
        redirectResponse.getHeader().set("Location", REDIRECT_RESOURCE.getPath());

        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, REDIRECT_RESOURCE);
        DSGHTTPResponse wantResponse = new DSGHTTPResponse(DSGHTTPStatus.NO_CONTENT);
        DSGMockClientOperation[] wantOperations = new DSGMockClientOperation[] {
                new DSGMockClientOperation(DSGCallType.CONNECT, wantAddress),
                new DSGMockClientOperation(wantAddress, redirectedRequest),
                new DSGMockClientOperation(wantAddress, redirectResponse),
                new DSGMockClientOperation(DSGCallType.CLOSE, wantAddress),
                new DSGMockClientOperation(DSGCallType.CONNECT, wantAddress),
                new DSGMockClientOperation(wantAddress, wantRequest),
                new DSGMockClientOperation(wantAddress, wantResponse),
                new DSGMockClientOperation(DSGCallType.CLOSE, wantAddress), };

        DSGMockHTTPClient client = new DSGMockHTTPClient(wantOperations, true, false);
        try {
            DSGHTTPResponse gotResponse = client.communicate(new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE));
            DSGHTTPTestUtil.assertResponsesMatch(wantResponse, gotResponse);
            client.assertAllOperationsExecuted();
        } finally {
            client.shutdown();
        }
    }

    @Test
    @Order(3)
    @EnabledIfEnvironmentVariable(named = "DSG_MASTER", matches = "true")
    void keepAliveClientDoesNotCloseConnection() throws IOException {
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE);
        wantRequest.getHeader().set("Connection", "keep-alive");
        InetSocketAddress wantAddress = new InetSocketAddress(TARGET_RESOURCE.getHost(), TARGET_RESOURCE.getPort());
        DSGHTTPResponse wantResponse = new DSGHTTPResponse(DSGHTTPStatus.NO_CONTENT);
        wantResponse.getHeader().set("Connection", "keep-alive");
        DSGMockClientOperation[] wantOperations = new DSGMockClientOperation[] {
                new DSGMockClientOperation(DSGCallType.CONNECT, wantAddress),
                new DSGMockClientOperation(wantAddress, wantRequest),
                new DSGMockClientOperation(wantAddress, wantResponse),
                // Do not close connection if keepAlive is set
        };

        DSGMockHTTPClient client = new DSGMockHTTPClient(wantOperations, false, true);
        try {
            DSGHTTPResponse gotResponse = client.communicate(new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE));
            DSGHTTPTestUtil.assertResponsesMatch(wantResponse, gotResponse);
            client.assertAllOperationsExecuted();
        } finally {
            client.shutdown();
        }
    }

    // #######################
    // # Mock Network Client #
    // #######################

    /** Expected call to DSGClient's methods. */
    private static class DSGMockClientOperation {
        private DSGCallType type;
        private SocketAddress address;
        private DSGMessage message;

        private DSGMockClientOperation(DSGCallType type, SocketAddress address) {
            this(type, address, null);
        }

        private DSGMockClientOperation(SocketAddress to, DSGHTTPRequest request) {
            this(DSGCallType.SEND, to, request);
        }

        private DSGMockClientOperation(SocketAddress from, DSGHTTPResponse response) {
            this(DSGCallType.RECEIVE, from, response);
        }

        private DSGMockClientOperation(DSGCallType type, SocketAddress address, DSGMessage message) {
            this.type = type;
            this.address = address;
            this.message = message;
        }

        public DSGCallType getType() {
            return type;
        }

        public SocketAddress getAddress() {
            return address;
        }

        public DSGMessage getMessage() {
            return message;
        }

    };

    /**
     * A Mock client that extends the HTTP client and overrides the network layer's
     * methods, so that we do not have to rely on actual network communication while
     * testing client functionality.
     */
    private static class DSGMockHTTPClient extends DSGHTTPClient {
        private int next;
        private DSGMockClientOperation[] wantOperations;

        public DSGMockHTTPClient(DSGMockClientOperation[] wantOperations) {
            super();
            this.next = 0;
            this.wantOperations = wantOperations;
        }

        public DSGMockHTTPClient(DSGMockClientOperation[] wantOperations, boolean followRedirects, boolean keepAlive) {
            super(followRedirects, keepAlive);
            this.wantOperations = wantOperations;
        }

        @Override
        public void connect(SocketAddress to) throws IOException {
            DSGMockClientOperation operation = assertNextOperation(DSGCallType.CONNECT);
        }

        @Override
        public void send(SocketAddress to, DSGMessage message) throws IOException {
            assertNotNull(message);
            if (!(message instanceof DSGHTTPRequest)) {
                fail("Trying send message that is not a HTTP request");
            }

            DSGMockClientOperation operation = assertNextOperation(DSGCallType.SEND);
            DSGHTTPRequest gotRequest = (DSGHTTPRequest) message;
            DSGHTTPRequest wantRequest = (DSGHTTPRequest) operation.getMessage();

            assertEquals(operation.getAddress(), to);
            DSGHTTPTestUtil.assertRequestsMatch(wantRequest, gotRequest);
        }

        @Override
        public void receive(SocketAddress from, DSGMessage message) throws IOException {
            assertNotNull(message);
            if (!(message instanceof DSGHTTPResponse)) {
                fail("Trying receive message that is not a HTTP Response");
            }

            DSGMockClientOperation operation = assertNextOperation(DSGCallType.RECEIVE);
            DSGHTTPResponse gotResponse = (DSGHTTPResponse) message;
            DSGHTTPResponse wantResponse = (DSGHTTPResponse) operation.getMessage();

            assertEquals(operation.getAddress(), from);
            gotResponse.version = wantResponse.version;
            gotResponse.status = wantResponse.status;
            gotResponse.header = wantResponse.header;
            gotResponse.body = wantResponse.body;
        }

        @Override
        public void close(SocketAddress address) throws IOException {
            DSGMockClientOperation operation = assertNextOperation(DSGCallType.CLOSE);
            assertEquals(operation.getAddress(), address);
        }

        public void assertAllOperationsExecuted() {
            StringBuilder missingOperations = new StringBuilder();
            missingOperations.append("[");
            for (int i = next; i < wantOperations.length; i++) {
                missingOperations.append(wantOperations[i].getType().toString().toLowerCase());
                missingOperations.append(", ");
            }
            missingOperations.append("]");

            assertEquals(wantOperations.length, next,
                    "Not all expected DSGClient methods have been called. Missing methods: "
                            + missingOperations.toString());
        }

        private DSGMockClientOperation assertNextOperation(DSGCallType want) {
            if (next >= wantOperations.length) {
                fail("Unexpected call to " + want.toString().toLowerCase());
                return null;
            }
            DSGMockClientOperation operation = this.wantOperations[next];
            assertEquals(want, operation.getType());
            next++;
            return operation;
        }
    }
}

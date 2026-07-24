package dsg.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
public class DSGHTTPServerTests {

    private static final String STATUS_CODE_HEADER = "X-DSG-Status-Code";
    private static final String RETURN_NULL_HEADER = "X-DSG-Return-Null";
    private static final String CONNECTION_HEADER = "X-DSG-Connection";
    private static final String THROW_EXCEPTION_HEADER = "X-DSG-Throw-Exception";

    // #########
    // # TESTS #
    // #########

    @Test
    @Order(0)
    void getRequest() {
        DSGHTTPClient client = new DSGHTTPClient();
        DSGHTTPRequest request = new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE);
        try {
            DSGHTTPResponse response = client.communicate(request);
            assertEquals(DSGHTTPStatus.OK, response.getStatus());
            assertNull(response.getBody());
        } catch (IOException e) {
            fail(e);
        } finally {
            client.shutdown();
        }
    }

    @Test
    @Order(1)
    void echoRequestBody() {
        DSGHTTPClient client = new DSGHTTPClient();
        byte[] wantPayload = new byte[1024];
        new Random().nextBytes(wantPayload);
        DSGHTTPRequest request = new DSGHTTPRequest(DSGHTTPMethod.POST, TARGET_RESOURCE,
                new ByteArrayInputStream(wantPayload));
        try {
            DSGHTTPResponse response = client.communicate(request);
            assertEquals(DSGHTTPStatus.OK, response.getStatus());
            assertNotNull(response.getBody());
            assertArrayEquals(wantPayload, response.getBody().readAllBytes());
        } catch (IOException e) {
            fail(e);
        } finally {
            client.shutdown();
        }
    }

    @Test
    @Order(2)
    void handlerReturningNullReturnsNoContent() {
        DSGHTTPClient client = new DSGHTTPClient();
        DSGHTTPRequest request = new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE);
        request.getHeader().set(RETURN_NULL_HEADER, "true");
        try {
            DSGHTTPResponse response = client.communicate(request);
            assertEquals(DSGHTTPStatus.NO_CONTENT, response.getStatus());
            assertNull(response.getBody());
        } catch (IOException e) {
            fail(e);
        } finally {
            client.shutdown();
        }
    }

    @Test
    @Order(3)
    void httpExceptionReturnsInternalServerError() {
        DSGHTTPClient client = new DSGHTTPClient();
        DSGHTTPRequest request = new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE);
        request.getHeader().set(THROW_EXCEPTION_HEADER, DSGHTTPException.class.getName());
        try {
            DSGHTTPResponse response = client.communicate(request);
            assertEquals(DSGHTTPStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        } catch (IOException e) {
            fail(e);
        } finally {
            client.shutdown();
        }
    }

    @Test
    @Order(4)
    void httpRequestExceptionReturnsStatus() {
        DSGHTTPClient client = new DSGHTTPClient();
        DSGHTTPRequest request = new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE);
        request.getHeader().set(THROW_EXCEPTION_HEADER, DSGHTTPRequestException.class.getName());
        request.getHeader().set(STATUS_CODE_HEADER, "410");
        try {
            DSGHTTPResponse response = client.communicate(request);
            assertEquals(DSGHTTPStatus.GONE, response.getStatus());
        } catch (IOException e) {
            fail(e);
        } finally {
            client.shutdown();
        }
    }

    @Test
    @Order(5)
    void runtimeExceptionReturnsInternalServerError() {
        DSGHTTPClient client = new DSGHTTPClient();
        DSGHTTPRequest request = new DSGHTTPRequest(DSGHTTPMethod.GET, TARGET_RESOURCE);
        request.getHeader().set(THROW_EXCEPTION_HEADER, RuntimeException.class.getName());
        try {
            DSGHTTPResponse response = client.communicate(request);
            assertEquals(DSGHTTPStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        } catch (IOException e) {
            fail(e);
        } finally {
            client.shutdown();
        }
    }

    // ###############################
    // # INITIALIZATION AND TEARDOWN #
    // ###############################

    private static final URI TARGET_RESOURCE;
    static {
        try {
            TARGET_RESOURCE = new URI("http://localhost:8000/");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
    private Thread serverThread;
    private DSGHTTPServer server;

    @BeforeEach
    void init() {
        try {
            server = new DSGHTTPServer(8000, 2, new DSGHTTPTestHandler());
            serverThread = new Thread() {
                @Override
                public void run() {
                    try {
                        server.serve();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            serverThread.start();
        } catch (IOException e) {
            fail("Failed to start HTTP server", e);
        }
    }

    @AfterEach
    void teardown() {
        serverThread.interrupt();
        server.shutdown();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            fail("Failed to shutdown server thread", e);
        }
    }

    // ################
    // # ECHO HANDLER #
    // ################

    private static class DSGHTTPTestHandler implements DSGHTTPHandler {

        @Override
        public DSGHTTPResponse handle(DSGHTTPRequest request) throws DSGHTTPException, DSGHTTPRequestException {
            DSGHTTPHeader header = request.getHeader();

            String wantReturnNull = header.get(RETURN_NULL_HEADER);
            if (wantReturnNull != null && wantReturnNull.equals("true")) {
                return null;
            }

            String wantStatusCode = header.get(STATUS_CODE_HEADER);
            DSGHTTPStatus status = DSGHTTPStatus.OK;
            if (wantStatusCode != null && !wantStatusCode.isBlank()) {
                int code = Integer.parseInt(wantStatusCode);
                status = DSGHTTPStatus.forCode(code);
            }
            String wantException = header.get(THROW_EXCEPTION_HEADER);
            if (DSGHTTPException.class.getName().equals(wantException)) {
                throw new DSGHTTPException("Oh no, an exception has been thrown!");
            }
            if (DSGHTTPRequestException.class.getName().equals(wantException)) {
                if (status == DSGHTTPStatus.OK) {
                    status = DSGHTTPStatus.BAD_REQUEST;
                }
                throw new DSGHTTPRequestException(status);
            }
            if (RuntimeException.class.getName().equals(wantException)) {
                throw new RuntimeException("Oh no, an unexpected runtime exception has been thrown");
            }

            String wantConnection = header.get(CONNECTION_HEADER);
            if (wantConnection != null && !wantConnection.isBlank()) {
                header.set("Connection", wantConnection);
            }

            InputStream body = request.getBody();
            return new DSGHTTPResponse(status, header, body);
        }
    }
}

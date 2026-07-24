package dsg.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import dsg.http.DSGHTTPClient;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPMethod;
import dsg.http.DSGHTTPRequest;
import dsg.http.DSGHTTPResponse;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGHTTPTestUtil;
import dsg.http.DSGStandardMediaTypes;

@TestMethodOrder(OrderAnnotation.class)
public class DSGRESTStubTests {

    // #########
    // # TESTS #
    // #########

    @Test
    @Order(0)
    void simpleMethodCall() throws IOException {
        DSGHTTPStatus wantStatus = DSGHTTPStatus.OK;
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGHTTPMediaType wantType = DSGStandardMediaTypes.PLAINTEXT.withCharset(StandardCharsets.UTF_8);
        String wantResource = "Test Response";
        DSGHTTPResponse response = new DSGHTTPResponse(wantStatus, wantType, wantResource);

        DSGMockHTTPClient httpClient = new DSGMockHTTPClient(wantRequest, response);
        DSGRESTStub stub = new DSGRESTStub(DSGHTTPTestUtil.TARGET_RESOURCE, httpClient);

        try {
            DSGRESTContext ctx = new DSGRESTContext();
            DSGHTTPHeader header = new DSGHTTPHeader();
            DSGRESTRepresentation got = stub.get(ctx, header);
            assertEquals(wantType, got.getType());
            assertEquals(wantResource, got.readAllAsString());
        } catch (DSGRESTException | DSGRESTRemoteException e) {
            fail(e);
        }
    }

    @Test
    @Order(1)
    void noContentReturnsNull() throws IOException {
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.NO_CONTENT);

        DSGMockHTTPClient httpClient = new DSGMockHTTPClient(wantRequest, response);
        DSGRESTStub stub = new DSGRESTStub(DSGHTTPTestUtil.TARGET_RESOURCE, httpClient);

        try {
            DSGRESTContext ctx = new DSGRESTContext();
            DSGHTTPHeader header = new DSGHTTPHeader();
            DSGRESTRepresentation got = stub.get(ctx, header);
            assertNull(got);
        } catch (DSGRESTException | DSGRESTRemoteException e) {
            fail(e);
        }
    }

    @Test
    @Order(2)
    void methodNotAllowedThrowsException() throws IOException {
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.METHOD_NOT_ALLOWED);

        DSGMockHTTPClient httpClient = new DSGMockHTTPClient(wantRequest, response);
        DSGRESTStub stub = new DSGRESTStub(DSGHTTPTestUtil.TARGET_RESOURCE, httpClient);
        DSGRESTContext ctx = new DSGRESTContext();
        DSGHTTPHeader header = new DSGHTTPHeader();
        assertThrows(UnsupportedOperationException.class, () -> stub.get(ctx, header));
    }

    @Test
    @Order(3)
    void redirectStatusReturnsRESTRedirect() throws IOException {
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.SEE_OTHER);
        response.getHeader().set("Location", DSGHTTPTestUtil.REDIRECT_RESOURCE.getPath());

        DSGMockHTTPClient httpClient = new DSGMockHTTPClient(wantRequest, response);
        DSGRESTStub stub = new DSGRESTStub(DSGHTTPTestUtil.TARGET_RESOURCE, httpClient);

        try {
            DSGRESTContext ctx = new DSGRESTContext();
            DSGHTTPHeader header = new DSGHTTPHeader();
            DSGRESTRepresentation got = stub.get(ctx, header);
            assertInstanceOf(DSGRESTRedirect.class, got);
            DSGRESTRedirect gotRedirect = (DSGRESTRedirect) got;
            assertEquals(DSGHTTPTestUtil.REDIRECT_RESOURCE.toString(), gotRedirect.getLocation());
        } catch (DSGRESTException | DSGRESTRemoteException e) {
            fail(e);
        }
    }

    @Test
    @Order(4)
    void errorStatusThrowsRESTException() throws IOException {
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGHTTPResponse response = new DSGHTTPResponse(DSGHTTPStatus.BAD_REQUEST);

        DSGMockHTTPClient httpClient = new DSGMockHTTPClient(wantRequest, response);
        DSGRESTStub stub = new DSGRESTStub(DSGHTTPTestUtil.TARGET_RESOURCE, httpClient);

        try {
            DSGRESTContext ctx = new DSGRESTContext();
            DSGHTTPHeader header = new DSGHTTPHeader();
            stub.get(ctx, header);
            fail("Want DSGRESTException to be thrown, but nothing was thrown.");
        } catch (DSGRESTException re) {
            assertEquals(response.getStatus(), re.getStatus());
        } catch (Exception e) {
            fail("Got exception = " + e.getClass().getName() + " (want DSGRESTException)");
        }
    }

    @Test
    @Order(5)
    void ioExceptionWrappedInRemoteException() throws Throwable {
        DSGHTTPRequest wantRequest = new DSGHTTPRequest(DSGHTTPMethod.GET, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGMockHTTPClient httpClient = new DSGMockHTTPClient(wantRequest, new IOException());
        DSGRESTStub stub = new DSGRESTStub(DSGHTTPTestUtil.TARGET_RESOURCE, httpClient);

        try {
            DSGRESTContext ctx = new DSGRESTContext();
            DSGHTTPHeader header = new DSGHTTPHeader();
            stub.get(ctx, header);
            fail("Want DSGRESTRemoteException to be thrown, but nothing was thrown.");
        } catch (DSGRESTRemoteException re) {
        } catch (Exception e) {
            fail("Got exception = " + e.getClass().getName() + " (want DSGRESTRemoteException)");
        }
    }

    // ####################
    // # Mock HTTP Client #
    // ####################
    public static class DSGMockHTTPClient extends DSGHTTPClient {
        private DSGHTTPRequest want;
        private DSGHTTPResponse result;
        private IOException exception;

        public DSGMockHTTPClient(DSGHTTPRequest want, DSGHTTPResponse result) throws IOException {
            super();
            this.want = want;
            this.result = result;
        }

        public DSGMockHTTPClient(DSGHTTPRequest want, IOException exception) throws IOException {
            super();
            this.want = want;
            this.result = null;
            this.exception = exception;
        }

        @Override
        public DSGHTTPResponse communicate(DSGHTTPRequest request) throws IOException {
            DSGHTTPTestUtil.assertRequestsMatch(want, request);
            if (exception != null) {
                throw exception;
            }
            return result;
        }
    }
}

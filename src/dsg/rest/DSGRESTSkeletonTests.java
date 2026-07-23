package dsg.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import dsg.http.DSGHTTPException;
import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMethod;
import dsg.http.DSGHTTPRequest;
import dsg.http.DSGHTTPRequestException;
import dsg.http.DSGHTTPResponse;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGHTTPTestUtil;

@TestMethodOrder(OrderAnnotation.class)
public class DSGRESTSkeletonTests {

    // #########
    // # TESTS #
    // #########

    @Test
    @Order(0)
    void representationToResponse() {
        DSGHTTPMethod wantMethod = DSGHTTPMethod.GET;
        DSGHTTPRequest request = new DSGHTTPRequest(wantMethod, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGRESTRepresentation representation = new DSGRESTRepresentation(DSGHTTPStatus.OK);
    }

    @Test
    @Order(1)
    void unknownResourceReturnsNotFound() throws DSGHTTPRequestException, DSGHTTPException {
        DSGHTTPMethod wantMethod = DSGHTTPMethod.GET;
        DSGHTTPRequest request = new DSGHTTPRequest(wantMethod, DSGHTTPTestUtil.TARGET_RESOURCE);

        DSGRESTSkeleton skeleton = new DSGRESTSkeleton();
        DSGHTTPResponse response = skeleton.handle(request);
        assertEquals(DSGHTTPStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    @Order(2)
    void returningRESTRedirectReturnsRedirectResponse() throws DSGHTTPRequestException, DSGHTTPException {
        DSGHTTPMethod wantMethod = DSGHTTPMethod.GET;
        DSGHTTPRequest request = new DSGHTTPRequest(wantMethod, DSGHTTPTestUtil.TARGET_RESOURCE);

        DSGHTTPStatus wantStatus = DSGHTTPStatus.PERMANENT_REDIRECT;
        DSGRESTSkeleton skeleton = new DSGRESTSkeleton();
        skeleton.exportResource(DSGHTTPTestUtil.TARGET_RESOURCE.getPath(), new DSGRESTTestResource(wantMethod,
                new DSGRESTRedirect(wantStatus, DSGHTTPTestUtil.REDIRECT_RESOURCE)));
        DSGHTTPResponse response = skeleton.handle(request);
        assertEquals(wantStatus, response.getStatus());
        assertEquals(DSGHTTPTestUtil.REDIRECT_RESOURCE.toASCIIString(), response.getHeader().get("Location"));
    }

    @Test
    @Order(3)
    void throwingUnsupportedOperationExceptionReturnsMethodNotAllowed()
            throws DSGHTTPRequestException, DSGHTTPException {
        DSGHTTPMethod wantMethod = DSGHTTPMethod.GET;
        DSGHTTPRequest request = new DSGHTTPRequest(wantMethod, DSGHTTPTestUtil.TARGET_RESOURCE);

        DSGRESTSkeleton skeleton = new DSGRESTSkeleton();
        skeleton.exportResource(DSGHTTPTestUtil.TARGET_RESOURCE.getPath(),
                new DSGRESTTestResource(null, (DSGRESTRepresentation) null));
        DSGHTTPResponse response = skeleton.handle(request);
        assertEquals(DSGHTTPStatus.METHOD_NOT_ALLOWED, response.getStatus());
    }

    @Test
    @Order(4)
    void returningNullReturnsNoContent() throws DSGHTTPRequestException, DSGHTTPException {
        DSGHTTPMethod wantMethod = DSGHTTPMethod.GET;
        DSGHTTPRequest request = new DSGHTTPRequest(wantMethod, DSGHTTPTestUtil.TARGET_RESOURCE);

        DSGRESTSkeleton skeleton = new DSGRESTSkeleton();
        skeleton.exportResource(DSGHTTPTestUtil.TARGET_RESOURCE.getPath(),
                new DSGRESTTestResource(DSGHTTPMethod.GET, (DSGRESTRepresentation) null));
        DSGHTTPResponse response = skeleton.handle(request);
        // It is ok to either return null, because the HTTP server should return
        // NO_CONTENT in that case or to create a response with a NO_CONTENT status.
        if (response == null) {
            return;
        }
        assertEquals(DSGHTTPStatus.NO_CONTENT, response.getStatus());
        assertNull(response.getBody());
    }

    @Test
    @Order(5)
    void throwingRESTExceptionReturnsStatusCode() throws DSGHTTPException {
        DSGHTTPMethod wantMethod = DSGHTTPMethod.GET;
        DSGHTTPRequest request = new DSGHTTPRequest(wantMethod, DSGHTTPTestUtil.TARGET_RESOURCE);

        DSGHTTPStatus wantStatus = DSGHTTPStatus.BAD_REQUEST;
        DSGRESTSkeleton skeleton = new DSGRESTSkeleton();
        skeleton.exportResource(DSGHTTPTestUtil.TARGET_RESOURCE.getPath(),
                new DSGRESTTestResource(wantMethod, new DSGRESTException(wantStatus)));

        // It is okay to either rethrow the exception such that the HTTP server will
        // create the correct response or to return the corresponding response directly.
        try {
            DSGHTTPResponse response = skeleton.handle(request);
            assertEquals(wantStatus, response.getStatus());
        } catch (DSGHTTPRequestException re) {
            assertEquals(wantStatus, re.getStatus());
        }
    }

    @Test
    @Order(7)
    void unknownResourceTriggersFactoryLookup() throws DSGHTTPRequestException, DSGHTTPException {
        DSGHTTPMethod wantMethod = DSGHTTPMethod.GET;
        DSGHTTPRequest request = new DSGHTTPRequest(wantMethod, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGTestResourceFactory factory = new DSGTestResourceFactory();
        DSGRESTSkeleton skeleton = new DSGRESTSkeleton(factory);
        skeleton.handle(request);
        assertTrue(factory.wasCalled());
    }

    // PUT -> created implicitly exports resource
    @Test
    @Order(8)
    void successfulPUTImplicitlyExportsResource() throws DSGHTTPRequestException, DSGHTTPException {
        DSGHTTPRequest request = new DSGHTTPRequest(DSGHTTPMethod.PUT, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGRESTTestResource resource = new DSGRESTTestResource(DSGHTTPMethod.PUT,
                new DSGRESTRepresentation(DSGHTTPStatus.CREATED));
        DSGTestResourceFactory factory = new DSGTestResourceFactory(resource);
        DSGRESTSkeleton skeleton = new DSGRESTSkeleton(factory);
        skeleton.handle(request);
        assertTrue(skeleton.isExported(DSGHTTPTestUtil.TARGET_RESOURCE.getPath()));
    }

    // DELETE -> OK implicitly exports resource
    @Test
    @Order(9)
    void successfulDELETEImplicitlyUnexportsResource() throws DSGHTTPRequestException, DSGHTTPException {
        DSGHTTPRequest request = new DSGHTTPRequest(DSGHTTPMethod.DELETE, DSGHTTPTestUtil.TARGET_RESOURCE);
        DSGRESTTestResource resource = new DSGRESTTestResource(DSGHTTPMethod.DELETE,
                new DSGRESTRepresentation(DSGHTTPStatus.OK));
        DSGRESTSkeleton skeleton = new DSGRESTSkeleton();
        skeleton.exportResource(DSGHTTPTestUtil.TARGET_RESOURCE.getPath(), resource);
        skeleton.handle(request);
        assertFalse(skeleton.isExported(DSGHTTPTestUtil.TARGET_RESOURCE.getPath()));
    }

    // #################
    // # TEST RESOURCE #
    // #################

    private static class DSGRESTTestResource extends DSGAbstractRESTResource {
        private DSGHTTPMethod wantMethod;
        private DSGRESTRepresentation value;
        private DSGRESTException exception;

        public DSGRESTTestResource(DSGHTTPMethod method, DSGRESTRepresentation value) {
            this.wantMethod = method;
            this.value = value;
            this.exception = null;
        }

        public DSGRESTTestResource(DSGHTTPMethod method, DSGRESTException exception) {
            this.wantMethod = method;
            this.value = null;
            this.exception = exception;
        }

        @Override
        public DSGRESTRepresentation get(DSGRESTContext ctx, DSGHTTPHeader header) throws DSGRESTException {
            assertNotNull(ctx);
            assertNotNull(header);
            return handle(DSGHTTPMethod.GET);
        }

        @Override
        public DSGRESTRepresentation head(DSGRESTContext ctx, DSGHTTPHeader header) throws DSGRESTException {
            assertNotNull(ctx);
            assertNotNull(header);
            return handle(DSGHTTPMethod.HEAD);
        }

        @Override
        public DSGRESTRepresentation put(DSGRESTContext ctx, DSGRESTRepresentation resource) throws DSGRESTException {
            assertNotNull(ctx);
            assertNotNull(resource);
            return handle(DSGHTTPMethod.PUT);
        }

        @Override
        public DSGRESTRepresentation post(DSGRESTContext ctx, DSGRESTRepresentation resource) throws DSGRESTException {
            assertNotNull(ctx);
            assertNotNull(resource);
            return handle(DSGHTTPMethod.POST);
        }

        @Override
        public DSGRESTRepresentation delete(DSGRESTContext ctx) throws DSGRESTException {
            assertNotNull(ctx);
            return handle(DSGHTTPMethod.DELETE);
        }

        private DSGRESTRepresentation handle(DSGHTTPMethod gotMethod) throws DSGRESTException {
            if (wantMethod == null) {
                throw new UnsupportedOperationException();
            }
            assertEquals(wantMethod, gotMethod);
            if (exception != null) {
                throw exception;
            }
            return value;
        }
    }

    // #########################
    // # TEST RESOURCE FACTORY #
    // #########################

    public static class DSGTestResourceFactory implements DSGRESTResourceFactory {

        private boolean called;
        private DSGRESTTestResource resource;

        public DSGTestResourceFactory() {
            this.called = false;
            this.resource = null;
        }

        public DSGTestResourceFactory(DSGRESTTestResource resource) {
            this.called = false;
            this.resource = resource;
        }

        @Override
        public DSGAbstractRESTResource newInstance(DSGHTTPRequest request) throws DSGRESTException {
            this.called = true;
            return resource;
        }

        public boolean wasCalled() {
            return called;
        }
    }
}

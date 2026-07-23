package dsg.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import dsg.echo.DSGRESTEchoClient;
import dsg.echo.DSGRESTEchoServer;
import dsg.http.DSGHTTPClient;

@TestMethodOrder(OrderAnnotation.class)
public class DSGRESTIntegrationTests {

    private static final URI ECHO_SERVER_ADDRESS;
    static {
        try {
            ECHO_SERVER_ADDRESS = new URI("http://localhost:8000/");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    // #########
    // # TESTS #
    // #########

    @Test
    @Order(0)
    void helloWorld() throws IOException {
        DSGHTTPClient httpClient = new DSGHTTPClient();
        DSGRESTEchoClient client = new DSGRESTEchoClient(ECHO_SERVER_ADDRESS, httpClient);
        String got = client.echo(null);
        assertEquals("Hello, World!", got);
    }

    @Test
    @Order(1)
    void echoMessage() throws IOException {
        DSGHTTPClient httpClient = new DSGHTTPClient();
        DSGRESTEchoClient client = new DSGRESTEchoClient(ECHO_SERVER_ADDRESS, httpClient);

        byte[] bytes = new byte[1024];
        Random rng = new Random();
        rng.nextBytes(bytes);
        String want = Base64.getEncoder().encodeToString(bytes);
        String got = client.echo(want);
        assertEquals(want, got);
    }

    // ###############################
    // # INITIALIZATION AND TEARDOWN #
    // ###############################
    private Thread serverThread;
    private DSGRESTEchoServer server;

    @BeforeEach
    void init() {
        try {
            server = new DSGRESTEchoServer(8000, 2);
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
            fail("Failed to start REST Echo server", e);
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
}

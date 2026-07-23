package dsg.http;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

/**
 * Utility methods for our HTTP test cases.
 */
public class DSGHTTPTestUtil {

    public static final URI TARGET_RESOURCE;
    public static final URI REDIRECT_RESOURCE;
    static {
        try {
            TARGET_RESOURCE = new URI("http://localhost:8000/some/resource?");
            REDIRECT_RESOURCE = TARGET_RESOURCE.resolve("/other/resource");
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static DSGHTTPHeader cloneHeader(DSGHTTPHeader header) {
        DSGHTTPHeader clone = new DSGHTTPHeader();
        for (Entry<String, List<String>> field : header.fields().entrySet()) {
            String name = field.getKey();
            List<String> values = field.getValue();
            for (String value : values) {
                clone.add(name, value);
            }
        }
        return clone;
    }

    public static void assertRequestsMatch(DSGHTTPRequest want, DSGHTTPRequest got) {
        assertEquals(want.getMethod(), got.getMethod());
        assertEquals(want.getTarget(), got.getTarget());
        assertEquals(want.getVersion(), got.getVersion());
        assertHeadersMatch(want.getHeader(), got.getHeader());
        assertBodiesMatch(want.getBody(), got.getBody());
    }

    public static void assertResponsesMatch(DSGHTTPResponse want, DSGHTTPResponse got) {
        assertEquals(want.version, got.version);
        assertEquals(want.status, got.status);
        assertHeadersMatch(want.header, got.header);
        assertBodiesMatch(want.body, got.body);
    }

    public static void assertHeadersMatch(DSGHTTPHeader want, DSGHTTPHeader got) {
        // It is okay if students add additional header fields, so we do not test,
        // whether got has fields that want has not.
        for (Entry<String, List<String>> wantField : want.fields().entrySet()) {
            String fieldName = wantField.getKey();

            // Skip a non-critical header fields, in order to allow students to play around
            // with them.
            if (fieldName.equals("User-Agent") || fieldName.equals("Server")) {
                continue;
            }

            List<String> gotValues = got.values(fieldName);
            List<String> wantValues = wantField.getValue();

            String gotString = (gotValues == null) ? "null" : String.join(", ", gotValues);
            String wantString = (wantValues == null) ? "null" : String.join(", ", wantValues);
            assertEquals(wantValues, gotValues,
                    "Got header field " + fieldName + " = " + gotString + " (want " + wantString + ") ");
        }

    }

    public static void assertBodiesMatch(InputStream want, InputStream got) {
        if (want == null) {
            assertNull(got);
            return;
        }

        try {
            byte[] gotBytes = got.readAllBytes();
            byte[] wantBytes = want.readAllBytes();
            assertEquals(wantBytes.length, gotBytes.length,
                    "Got message body size = " + gotBytes.length + " (want " + wantBytes.length + ")");
            assertArrayEquals(wantBytes, gotBytes);
        } catch (IOException e) {
            fail(e);
        }
    }

}

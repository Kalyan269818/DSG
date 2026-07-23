package dsg.echo;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import dsg.http.DSGHTTPHeader;
import dsg.http.DSGHTTPMediaType;
import dsg.http.DSGHTTPStatus;
import dsg.http.DSGStandardMediaTypes;
import dsg.rest.DSGAbstractRESTResource;
import dsg.rest.DSGRESTContext;
import dsg.rest.DSGRESTException;
import dsg.rest.DSGRESTRepresentation;

public class DSGRESTEchoResource extends DSGAbstractRESTResource {
    private static final List<DSGHTTPMediaType> AVAILABLE_MEDIA_TYPES = Arrays.asList(DSGStandardMediaTypes.HTML,
            DSGStandardMediaTypes.PLAINTEXT);

    @Override
    public DSGRESTRepresentation get(DSGRESTContext ctx, DSGHTTPHeader header) throws DSGRESTException {
        return getRepresentation(ctx, "Hello, World!");
    }

    @Override
    public DSGRESTRepresentation post(DSGRESTContext ctx, DSGRESTRepresentation representation)
            throws DSGRESTException {
        try {
            if (!DSGStandardMediaTypes.PLAINTEXT.accepts(representation.getType())) {
                throw new DSGRESTException(DSGHTTPStatus.UNSUPPORTED_MEDIA_TYPE);
            }
            String message = representation.readAllAsString();
            return getRepresentation(ctx, message);
        } catch (IOException e) {
            throw new DSGRESTException(DSGHTTPStatus.BAD_REQUEST);
        }
    }

    private DSGRESTRepresentation getRepresentation(DSGRESTContext ctx, String message) throws DSGRESTException {
        DSGHTTPMediaType type = ctx.negotiate(AVAILABLE_MEDIA_TYPES);
        if (type == null) {
            throw new DSGRESTException(DSGHTTPStatus.NOT_ACCEPTABLE);
        }
        // Default to UTF-8 charset for maximum compatibility.
        if (!type.hasCharset()) {
            type = type.withCharset(StandardCharsets.UTF_8);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(output, type.getCharset())) {
            if (type.getSubType().equals("html")) {
                writeHTML(writer, type.getCharset(), message);
            } else {
                writer.write(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new DSGRESTException(DSGHTTPStatus.INTERNAL_SERVER_ERROR);
        }

        InputStream resource = new ByteArrayInputStream(output.toByteArray());
        return new DSGRESTRepresentation(DSGHTTPStatus.OK, type, resource);
    }

    private void writeHTML(Writer writer, Charset charset, String message) throws IOException {
        writer.write("<!DOCTYPE html>\r\n");
        writer.write("<html lang=\"en\">\r\n");
        writer.write("    <head>\r\n");
        writer.write("        <meta charset=\"" + charset.name() + "\">");
        writer.write("        <title>Echo Service</title>\r\n");
        writer.write("    </head>\r\n");
        writer.write("    <body>\r\n");
        writer.write("        <h1>Echo Service</h1>\r\n");
        writer.write("<p>");
        writer.write(message);
        writer.write("</p>\r\n");
        writer.write("</body>\r\n");
        writer.write("</html>\r\n");
        writer.close();
    }
}

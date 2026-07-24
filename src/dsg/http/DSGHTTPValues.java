package dsg.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility for accessing query parameters and url-encoded values.
 */
public class DSGHTTPValues {
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String VALUE_SEPARATOR = "=";

    private final Map<String, List<String>> parameters;

    /**
     * Create an empty set of values.
     */
    public DSGHTTPValues() {
        this.parameters = new HashMap<>();
    }

    /**
     * Parse the HTTP variables from the given URI's query.
     *
     * @param uri the URI from which query parameters should be parsed.
     */
    public DSGHTTPValues(URI uri) {
        this();
        this.decode(uri.getRawQuery(), StandardCharsets.US_ASCII);
    }

    /**
     * Parse url-encoded form parameters from a request's body using the given media
     * type.
     *
     * @param mediaType the media-type of {@code body}.
     * @param body      the payload from which values are parsed.
     *
     * @throws IllegalArgumentException if mediaType is not equal to
     *                                  "application/x-www-form-urlencoded".
     * @throws IOException              if reading from {@code body} fails.
     */
    public DSGHTTPValues(DSGHTTPMediaType mediaType, InputStream body) throws IOException {
        this();
        String type = mediaType.getFullType();
        if (!type.equals(DSGStandardMediaTypes.FORM_URL_ENCODED.getFullType())) {
            throw new IllegalArgumentException(
                    "\"" + type + "\": Invalid media type, (want \"application/x-www-form-urlencoded\")");
        }
        String formData = new String(body.readAllBytes(), mediaType.getCharset());
        this.decode(formData, mediaType.getCharset());
    }

    /**
     * Set the {@code key} to the single {@code value}.
     *
     * @param key   the key to set.
     * @param value the value to associate with {@code key}.
     */
    public void set(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("name may not be null");
        }
        List<String> items = new LinkedList<>();
        items.add(value);
        parameters.put(key, items);
    }

    /**
     * Add {@code value} to the list of values associated with {@code key}.
     *
     * @param key   the key to add to.
     * @param value the value to associate with {@code key}.
     */
    public void add(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("name may not be null");
        }
        List<String> items = this.parameters.get(key);
        if (items == null) {
            items = new LinkedList<>();
            this.parameters.put(key, items);
        }
        items.add(value);
    }

    /**
     * Remove the values associated with the given {@code key}.
     *
     * @param key the key to remove.
     */
    public void remove(String key) {
        if (key == null) {
            return;
        }
        this.parameters.remove(key);
    }

    /**
     * Return the first value associated with the named {@code key}.
     *
     * @param key the key to query.
     * @return the first value associated with {@code key} or null, if no value is
     *         associated with it.
     */
    public String get(String key) {
        List<String> values = parameters.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }

    /**
     * Return all values associated with the named {@code key}.
     *
     * @param key the key to query.
     * @return a list of all values associated with {@code key} or null, if no value
     *         is associated with it.
     */
    public List<String> values(String key) {
        return parameters.get(key);
    }

    /**
     * Encode the values into their URL-encoded format.
     *
     * @return the URL encoded format.
     */
    public String encode() {
        StringBuilder builder = new StringBuilder();
        for (Entry<String, List<String>> entry : parameters.entrySet()) {
            String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            for (String value : entry.getValue()) {
                builder.append(key);
                builder.append(VALUE_SEPARATOR);
                builder.append(URLEncoder.encode(value, StandardCharsets.UTF_8));
                builder.append(PARAMETER_SEPARATOR);
            }
        }
        if (parameters.size() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Entry<String, List<String>> entry : parameters.entrySet()) {
            String key = entry.getKey();
            for (String value : entry.getValue()) {
                builder.append(key);
                builder.append(VALUE_SEPARATOR);
                builder.append(value);
                builder.append(PARAMETER_SEPARATOR);
            }
        }
        if (parameters.size() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    private void decode(String data, Charset charset) {
        if (data == null) {
            this.parameters.clear();
            return;
        }

        String[] params = data.split(PARAMETER_SEPARATOR);
        for (String parameter : params) {
            String[] kv = parameter.split(VALUE_SEPARATOR);
            String key = URLDecoder.decode(kv[0], charset);
            List<String> parameterValues = parameters.get(key);
            if (parameterValues == null) {
                parameterValues = new LinkedList<>();
                parameters.put(key, parameterValues);
            }
            String value = "";
            if (kv.length == 2) {
                value = URLDecoder.decode(kv[1], charset);
            }
            parameterValues.add(value);
        }
    }

}

package dsg.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HTTP header fields.
 */
public class DSGHTTPHeader {

    private HashMap<String, List<String>> fields;

    /**
     * Initialize an empty HTTP header.
     */
    public DSGHTTPHeader() {
        this(new HashMap<>());
    }

    /**
     * Initialize an empty HTTP header, using {@code fields} as its initial header
     * fields.
     * 
     * @param fields the initial header fields.
     */
    public DSGHTTPHeader(HashMap<String, List<String>> fields) {
        this.fields = new HashMap<>();
        for (Entry<String, List<String>> entry : fields.entrySet()) {
            String key = this.canonicalize(entry.getKey());
            this.fields.put(key, entry.getValue());
        }
    }

    /**
     * Return the first value associated with the given field {@code name}, if any.
     * 
     * @param name the header name to retrieve.
     * @return the first value associated with {@code name}.
     */
    public String get(String name) {
        name = this.canonicalize(name);
        List<String> items = fields.get(name);
        if (items == null || items.size() == 0) {
            return null;
        }
        return items.get(0);
    }

    /**
     * Return all values associated with the given field {@code name}, if any.
     * 
     * @param name the header name to retrieve.
     * @return all values associated with {@code name}.
     */
    public List<String> values(String name) {
        return fields.get(name);
    }

    /**
     * Return, whether any value is associated with the field {@code name}.
     * 
     * @param name the header name.
     * @return true if at least one value is associated with {@code name}, false
     *         otherwise.
     */
    public boolean isSet(String name) {
        return fields.containsKey(name);
    }

    /**
     * Return raw header fields.
     * 
     * @return all fields stored in the header.
     */
    public Map<String, List<String>> fields() {
        return this.fields;
    }

    /**
     * Set a field with the given {@code name} to the single {@code value}.
     *
     * The name is automatically canonicalized to HTTP's field name format, that is,
     * the first letter and every letter following a hyphen is capitalized.
     * 
     * Preceding and trailing whitespace is automatically removed from
     * {@code value}. If value is an empty string after trimming, then this method
     * is equivalent to {@code this.remove(name)}
     * 
     * @param name  the header field name.
     * @param value the header field value.
     */
    public void set(String name, String value) {
        name = this.canonicalize(name);
        value = this.trimWhitespace(value);
        if (value.equals("")) {
            fields.remove(name);
            return;
        }
        List<String> items = new ArrayList<>();
        items.add(value);
        fields.put(name, items);
    }

    /**
     * Add a field with the given {@code name} and {@code value} to the header,
     * appending to already existing values.
     *
     * The name is automatically canonicalized to HTTP's field name format, that is,
     * the first letter and every letter following a hyphen is capitalized.
     * 
     * Preceding and trailing whitespace is automatically removed from
     * {@code value}. If value is an empty string after trimming, it is ignored.
     * 
     * @param name  the header field name.
     * @param value the header field value.
     */
    public void add(String name, String value) {
        name = this.canonicalize(name);
        value = this.trimWhitespace(value);
        if (value.equals("")) {
            return;
        }

        List<String> items = this.fields.get(name);
        if (items == null) {
            items = new ArrayList<>();
            this.fields.put(name, items);
        }
        items.add(value);
    }

    /**
     * Remove the field with the given {@code name} from the header.
     * 
     * The name is automatically canonicalized to HTTP's field name format, that is,
     * the first letter and every letter following a hyphen is capitalized.
     * 
     * @param name the name of the header field to remove.
     */
    public void remove(String name) {
        name = this.canonicalize(name);
        this.fields.remove(name);
    }

    /**
     * Return the canonicalized form of {@code str}.
     *
     * Field names in HTTP are case-insensitive and their canonical form is the
     * first Letter and all letters following a hyphen being capitalized, while
     * other letters are not.
     * 
     * @param str the header key to be canonicalized.
     * @return the canonicalized representation of {@code str}.
     */
    private String canonicalize(String str) {
        char[] keyChars = trimWhitespace(str).toLowerCase().toCharArray();
        keyChars[0] = Character.toUpperCase(keyChars[0]);
        for (int i = 1; i < keyChars.length; i++) {
            char prev = keyChars[i - 1];
            if (prev != '-') {
                continue;
            }
            char c = keyChars[i];
            keyChars[i] = Character.toUpperCase(c);
        }
        return new String(keyChars);
    }

    private String trimWhitespace(String str) {
        int start = str.length();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c != ' ' && c != '\t') {
                start = i;
                break;
            }
        }
        if (start >= str.length()) {
            return "";
        }
        // End is not included in substring.
        int end = start + 1;
        for (int i = str.length() - 1; i > start; i--) {
            char c = str.charAt(i);
            if (c != ' ' && c != '\t') {
                end = i + 1;
                break;
            }
        }
        return str.substring(start, end);
    }

    protected void serialize(OutputStream stream) throws IOException {
        for (Entry<String, List<String>> entry : this.fields.entrySet()) {
            String name = entry.getKey();
            List<String> items = entry.getValue();
            if (items.size() == 0) {
                continue;
            }

            stream.write(name.getBytes(StandardCharsets.US_ASCII));
            stream.write(':');
            stream.write(' ');
            String item = this.serializeFieldValue(items.get(0));
            stream.write(item.getBytes(StandardCharsets.US_ASCII));
            if (items.size() > 1) {
                Iterator<String> remaining = items.listIterator(1);
                while (remaining.hasNext()) {
                    item = this.serializeFieldValue(remaining.next());
                    stream.write(',');
                    stream.write(' ');
                    stream.write(item.getBytes(StandardCharsets.US_ASCII));
                }
            }
            stream.write('\r');
            stream.write('\n');
        }
        // The header section is closed using an empty line.
        stream.write('\r');
        stream.write('\n');
    }

    private String serializeFieldValue(String value) {
        if (!value.contains(",") && !value.contains("\"")) {
            return value;
        }

        StringBuilder b = new StringBuilder();
        b.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
            case '\"':
            case '\\':
                b.append('\\');
                /* FALLTHROUGH */
            default:
                b.append(c);
            }
        }
        b.append('"');
        return b.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DSGHTTPHeader{");
        for (Entry<String, List<String>> field : this.fields.entrySet()) {
            builder.append(field.getKey());
            builder.append("=[");

            List<String> items = field.getValue();
            for (String item : items) {
                builder.append(item);
                builder.append(", ");
            }
            if (items.size() > 0) {
                builder.setLength(builder.length() - 2);
            }
            builder.append("], ");
        }
        builder.append("}");
        return builder.toString();
    }
}

package dsg.json;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import dsg.http.DSGHTTPMediaType;
import dsg.json.DSGJSONToken.Type;

public class DSGJSONObject extends DSGJSONValue {
    private Map<DSGJSONString, DSGJSONValue> members;

    /**
     * Create an empty JSON object.
     */
    public DSGJSONObject() {
        this(new HashMap<>());
    }

    /**
     * Create a JSON object using {@code members} as its initial members.
     */
    public DSGJSONObject(HashMap<DSGJSONString, DSGJSONValue> members) {
        this.members = members;
    }

    /**
     * Get the value of the member identified by {@code name}.
     *
     * @param name the member's name.
     * @return the member's value.
     */
    public DSGJSONValue getMember(String name) {
        return this.members.get(new DSGJSONString(name));
    }

    /**
     * Get the double representation of the member identified by {@code name}.
     *
     * @param name the member's name.
     * @return the member's double value or 0, if the member does not exist.
     * @throws DSGJSONException if the member cannot be represented as long.
     */
    public long getMemberAsLong(String name) throws DSGJSONException {
        DSGJSONValue member = members.get(new DSGJSONString(name));
        if (member == null) {
            return 0;
        }
        return member.asLong();
    }

    /**
     * Get the string representation of the member identified by {@code name}.
     *
     * @param name the member's name.
     * @return the member's string value or null, if the member does not exist.
     * @throws DSGJSONException if the member cannot be represented as string.
     */
    public String getMemberAsString(String name) throws DSGJSONException {
        DSGJSONValue member = members.get(new DSGJSONString(name));
        if (member == null) {
            return null;
        }
        return member.asString();
    }

    /**
     * Try to parse the member identified by {@code name} into an URI.
     *
     * @param name the member's name.
     * @return the member's URI value or null, if the member does not exist.
     * @throws DSGJSONException if the member cannot be parsed into an URI.
     */
    public URI getMemberAsURI(String name) throws DSGJSONException {
        DSGJSONValue member = members.get(new DSGJSONString(name));
        if (member == null) {
            return null;
        }
        return member.asURI();
    }

    /**
     * Try to parse the member identified by {@code name} into a Date.
     *
     * @param name the member's name.
     * @return the member's Date value or null, if the member does not exist.
     * @throws DSGJSONException if the member cannot be parsed into an URI.
     */
    public Date getMemberAsDate(String name) throws DSGJSONException {
        DSGJSONValue member = members.get(new DSGJSONString(name));
        if (member == null) {
            return null;
        }
        return member.asDate();
    }

    /**
     * Try to parse the member identified by {@code name} into a HTTP media type.
     *
     * @param name the member's name.
     * @return the member's HTTP media type value or null, if the member does not
     *         exist.
     * @throws DSGJSONException if the member cannot be parsed into an media type.
     */
    public DSGHTTPMediaType getMemberAsMediaType(String name) throws DSGJSONException {
        DSGJSONValue member = members.get(new DSGJSONString(name));
        if (member == null) {
            return null;
        }
        return member.asMediaType();
    }

    /**
     * Try to cast the member identified by {@code name} into a JSON value of type
     * T.
     *
     * @param <T> the target type.
     * @return the JSON value cast to T or null, if the member does not exist..
     * @throws DSGJSONException if this JSON value is not of type T.
     */
    @SuppressWarnings("unchecked")
    public <T extends DSGJSONValue> T getMemberAs(String name) throws DSGJSONException {
        DSGJSONValue member = members.get(new DSGJSONString(name));
        if (member == null) {
            return null;
        }
        return (T) member.as();
    }

    /**
     * Set the member identified by {@code name} to the given value.
     *
     * @param name  the member name.
     * @param value the member's new value.
     * @throws IllegalArgumentException if name is null.
     */
    public void setMember(String name, DSGJSONValue value) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }
        if (value == null) {
            return;
        }
        this.members.put(new DSGJSONString(name), value);
    }

    /**
     * Set the member identified by {@code name} to the given value.
     *
     * @param name  the member name.
     * @param value the member's new value.
     */
    public void setMember(String name, DSGJSONSerializable value) {
        if (value == null) {
            return;
        }
        setMember(name, value.toJSON());
    }

    /**
     * Set the member identified by {@code name} to the given value.
     *
     * @param name  the member name.
     * @param value the member's new value.
     */
    public void setMember(String name, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        setMember(name, new DSGJSONString(value));
    }

    /**
     * Set the member identified by {@code name} to the given value.
     *
     * @param name  the member name.
     * @param value the member's new value.
     */
    public void setMember(String name, long value) {
        setMember(name, new DSGJSONNumber(value));
    }

    /**
     * Set the member identified by {@code name} to the given value.
     *
     * @param name  the member name.
     * @param value the member's new value.
     */
    public void setMember(String name, URI value) {
        if (value == null) {
            return;
        }
        setMember(name, new DSGJSONString(value.toASCIIString()));
    }

    /**
     * Set the member identified by {@code name} to the given value.
     *
     * @param name  the member name.
     * @param value the member's new value.
     */
    public void setMember(String name, Date value) {
        if (value == null) {
            return;
        }
        setMember(name, new DSGJSONString(value));
    }

    /**
     * Set the member identified by {@code name} to the given value.
     *
     * @param name  the member name.
     * @param value the member's new value.
     */
    public void setMember(String name, DSGHTTPMediaType value) {
        if (value == null) {
            return;
        }
        setMember(name, new DSGJSONString(value.toString()));
    }

    /**
     * Return, whether this object has a member with the given name.
     */
    public boolean hasMember(String name) {
        return this.members.containsKey(new DSGJSONString(name));
    }

    @Override
    public DSGJSONType getType() {
        return DSGJSONType.Object;
    }

    @Override
    protected void serializeJSON(Writer writer) throws IOException {
        writer.write(DSGJSONToken.BEGIN_OBJECT_SYMBOL);
        int i = 1;
        int size = this.members.size();
        for (Entry<DSGJSONString, DSGJSONValue> entry : this.members.entrySet()) {
            entry.getKey().serializeJSON(writer);
            writer.write(DSGJSONToken.NAME_SEPARATOR_SYMBOL);
            entry.getValue().serializeJSON(writer);
            // The final member may not contain a trailing comma.
            if (i < size) {
                writer.write(DSGJSONToken.VALUE_SEPARATOR_SYMBOL);
            }
            i++;
        }
        writer.write(DSGJSONToken.END_OBJECT_SYMBOL);
    }

    @Override
    protected void deserializeJSON(DSGJSONReader reader) throws DSGJSONException, IOException {
        // object = begin-object [ member *( value-separator member ) ] end-object
        HashMap<DSGJSONString, DSGJSONValue> newMembers = new HashMap<>();
        DSGJSONToken token = reader.expectTokenOf(Type.BEGIN_OBJECT);

        token = reader.expectToken();
        while (token.getType() != Type.END_OBJECT) {
            if (token.getType() != Type.STRING) {
                throw new DSGJSONException(token, Type.END_OBJECT);
            }

            // member = string name-separator value
            DSGJSONString memberName = new DSGJSONString(token.getValue());
            // RFC 8259, Section 4: The names within an object SHOULD be unique.
            if (newMembers.containsKey(memberName)) {
                throw new DSGJSONException(token.getPosition(), "duplicate member name '" + memberName + "'");
            }
            reader.expectTokenOf(Type.NAME_SEPARATOR);
            DSGJSONValue memberValue = reader.read();
            newMembers.put(memberName, memberValue);
            token = reader.expectToken();
            if (token.getType() == Type.VALUE_SEPARATOR) {
                // The final member must not be followed by a value separator.
                token = reader.expectToken();
                if (token.getType() == Type.END_OBJECT) {
                    throw new DSGJSONException(token, Type.STRING);
                }
            }
        }
        this.members = newMembers;
    }
}

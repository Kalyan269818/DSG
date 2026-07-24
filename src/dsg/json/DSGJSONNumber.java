package dsg.json;

import java.io.IOException;
import java.io.Writer;

import dsg.json.DSGJSONToken.Type;

/**
 * Representation of a parsed JSON number.
 */
public class DSGJSONNumber extends DSGJSONValue {
    private double value;

    public DSGJSONNumber() {
        this(0);
    }

    public DSGJSONNumber(long value) {
        this.value = value;
    }

    @Override
    public long asLong() {
        return (long) this.value;
    }

    @Override
    public String asString() {
        return "" + this.value;
    }

    @Override
    public String toString() {
        return asString();
    }

    @Override
    public DSGJSONType getType() {
        return DSGJSONType.Number;
    }

    @Override
    protected void serializeJSON(Writer out) throws IOException {
        out.write("" + value);
    }

    @Override
    protected void deserializeJSON(DSGJSONReader input) throws DSGJSONException, IOException {
        DSGJSONToken token = input.expectTokenOf(Type.NUMBER);
        try {
            this.value = Double.parseDouble(token.getValue());
        } catch (NumberFormatException nfe) {
            throw new DSGJSONException(token.getPosition(), nfe.getMessage());
        }
    }
}

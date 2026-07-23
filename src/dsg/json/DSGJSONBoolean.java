package dsg.json;

import java.io.IOException;
import java.io.Writer;

import dsg.json.DSGJSONToken.Type;

public class DSGJSONBoolean extends DSGJSONValue {

    private boolean value;

    public DSGJSONBoolean() {
        this(false);
    }

    public DSGJSONBoolean(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return this.value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    @Override
    public DSGJSONType getType() {
        return DSGJSONType.Boolean;
    }

    @Override
    protected void serializeJSON(Writer out) throws IOException {
        if (value) {
            out.write("" + DSGJSONToken.TRUE_LITERAL);
        } else {
            out.write("" + DSGJSONToken.FALSE_LITERAL);
        }
    }

    @Override
    protected void deserializeJSON(DSGJSONReader input) throws DSGJSONException, IOException {
        DSGJSONToken token = input.expectTokenOf(Type.BOOLEAN);
        String str = token.getValue();
        if (DSGJSONToken.TRUE_LITERAL.equals(str)) {
            value = true;
        } else {
            value = false;
        }
    }

}

package dsg.json;

import java.io.IOException;
import dsg.json.DSGJSONToken.Type;

public class DSGJSONException extends IOException {

    public DSGJSONException(String message) {
        super(message);
    }

    public DSGJSONException(DSGJSONTokenPosition position, String message) {
        super(position + ": " + message);
    }

    public DSGJSONException(DSGJSONToken got, Type want) {
        super(got.getPosition() + ": Unexpected " + got.getType() + ", expected " + want);
    }
}

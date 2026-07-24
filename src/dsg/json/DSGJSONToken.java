package dsg.json;

/**
 * Representation of a single JSON lexer token.
 */
public class DSGJSONToken {

    public static final char BEGIN_ARRAY_SYMBOL = '[';
    public static final char BEGIN_OBJECT_SYMBOL = '{';
    public static final char END_ARRAY_SYMBOL = ']';
    public static final char END_OBJECT_SYMBOL = '}';
    public static final char NAME_SEPARATOR_SYMBOL = ':';
    public static final char VALUE_SEPARATOR_SYMBOL = ',';
    public static final char QUOTATION_MARK_SYMBOL = '"';
    public static final char ESCAPE_SYMBOL = '\\';
    public static final char PLUS_SYMBOL = '+';
    public static final char MINUS_SYMBOL = '-';
    public static final String NULL_LITERAL = "null";
    public static final String TRUE_LITERAL = "true";
    public static final String FALSE_LITERAL = "false";

    public static enum Type {
        BEGIN_ARRAY, BEGIN_OBJECT, END_ARRAY, END_OBJECT, NAME_SEPARATOR, VALUE_SEPARATOR, NULL, BOOLEAN, NUMBER, STRING;

        @Override
        public String toString() {
            switch (this) {
                case BEGIN_ARRAY:
                    return "'" + BEGIN_ARRAY_SYMBOL + "'";
                case BEGIN_OBJECT:
                    return "'" + BEGIN_OBJECT_SYMBOL + "'";
                case BOOLEAN:
                    return "boolean";
                case END_ARRAY:
                    return "'" + END_ARRAY_SYMBOL + "'";
                case END_OBJECT:
                    return "'" + END_OBJECT_SYMBOL + "'";
                case NAME_SEPARATOR:
                    return "'" + NAME_SEPARATOR_SYMBOL + "'";
                case NULL:
                    return NULL_LITERAL;
                case NUMBER:
                    return "number";
                case STRING:
                    return "string";
                case VALUE_SEPARATOR:
                    return "'" + VALUE_SEPARATOR + "'";
                default:
                    return "Invalid";
            }
        }
    }

    private final Type type;
    private final String value;
    private final DSGJSONTokenPosition position;

    /**
     * Create a new token with the given type and value.
     * 
     * @param type the token's type.
     * @param value the token's value.
     */
    public DSGJSONToken(Type type, String value, DSGJSONTokenPosition position) {
        this.type = type;
        this.value = value;
        this.position = position;
    }

    /**
     * Return the token's type.
     * 
     * @return the token's type.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Return the token's value, if any.
     * 
     * @return the token's value or null if the token does not have a value.
     */
    public String getValue() {
        return this.value;
    }

    public DSGJSONTokenPosition getPosition() {
        return this.position;
    }

    @Override
    public String toString() {
        switch (this.type) {
            case BEGIN_ARRAY:
                return "" + BEGIN_ARRAY_SYMBOL;
            case BEGIN_OBJECT:
                return "" + BEGIN_OBJECT_SYMBOL;
            case BOOLEAN:
                return this.value;
            case END_ARRAY:
                return "" + END_ARRAY_SYMBOL;
            case END_OBJECT:
                return "" + END_OBJECT_SYMBOL;
            case NAME_SEPARATOR:
                return "" + NAME_SEPARATOR_SYMBOL;
            case NULL:
                return this.value;
            case NUMBER:
                return this.value;
            case STRING:
                return "\"" + this.value + "\"";
            case VALUE_SEPARATOR:
                return "" + VALUE_SEPARATOR_SYMBOL;
            default:
                return "Invalid";
        }
    }
}

package dsg.json;

/**
 * Token position information.
 */
public class DSGJSONTokenPosition {
    private int line;
    private int column;

    public DSGJSONTokenPosition() {
        this.line = 1;
        this.column = 1;
    }

    public DSGJSONTokenPosition(DSGJSONTokenPosition other) {
        this.line = other.line;
        this.column = other.column;
    }

    public void newLine() {
        this.line += 1;
        this.column = 1;
    }

    public void newChar() {
        this.column += 1;
    }

    public void reset() {
        if (this.line == 1 && this.column == 1) {
            return;
        }
        if (this.column == 1) {
            this.line -= 1;
            return;
        }
        this.column -= 1;
    }

    @Override
    public String toString() {
        return "line: " + line + ", column: " + column;
    }
}

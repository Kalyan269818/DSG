package dsg.json;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import dsg.json.DSGJSONToken.Type;

public class DSGJSONArray extends DSGJSONValue implements Collection<DSGJSONValue> {
    private Collection<DSGJSONValue> items;

    public DSGJSONArray() {
        this(new ArrayList<>());
    }

    public DSGJSONArray(Collection<DSGJSONValue> items) {
        this.items = items;
    }

    public DSGJSONArray(DSGJSONValue[] items) {
        this.items = Arrays.asList(items);
    }

    public boolean add(String item) {
        if (item == null) {
            return false;
        }
        return items.add(new DSGJSONString(item));
    }

    public boolean add(DSGJSONSerializable item) {
        return items.add(item.toJSON());
    }

    @Override
    public boolean add(DSGJSONValue item) {
        return items.add(item);
    }

    @Override
    public boolean addAll(Collection<? extends DSGJSONValue> c) {
        return items.addAll(c);
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public boolean contains(Object o) {
        return items.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return items.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public Iterator<DSGJSONValue> iterator() {
        return items.iterator();
    }

    @Override
    public boolean remove(Object o) {
        return items.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return items.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return items.retainAll(c);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Object[] toArray() {
        return items.toArray();
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        return items.toArray(arg0);
    }

    @Override
    public DSGJSONType getType() {
        return DSGJSONType.Array;
    }

    @Override
    protected void serializeJSON(Writer out) throws IOException {
        int size = items.size();
        out.write(DSGJSONToken.BEGIN_ARRAY_SYMBOL);
        int i = 1;
        for (DSGJSONValue item : items) {
            item.serializeJSON(out);
            // The final attribute may not contain a trailing comma.
            if (i < size) {
                out.write(DSGJSONToken.VALUE_SEPARATOR_SYMBOL);
            }
            i++;
        }
        out.write(DSGJSONToken.END_ARRAY_SYMBOL);
    }

    @Override
    protected void deserializeJSON(DSGJSONReader reader) throws DSGJSONException, IOException {
        // array = begin-array [ value *( value-separator value ) ] end-array
        DSGJSONToken token = reader.expectTokenOf(Type.BEGIN_ARRAY);
        token = reader.peekExpectToken();
        ArrayList<DSGJSONValue> newItems = new ArrayList<>();
        while (token.getType() != Type.END_ARRAY) {
            DSGJSONValue value = reader.read();
            newItems.add(value);
            token = reader.peekExpectToken();
            switch (token.getType()) {
            case VALUE_SEPARATOR:
                reader.consumeToken();
                // The final value must not be followed by a value separator.
                token = reader.peekExpectToken();
                if (token.getType() == Type.END_ARRAY) {
                    throw new DSGJSONException(token.getPosition(),
                            "Unexpected " + token.getType() + ", expected value");
                }
                break;
            case END_ARRAY:
                break;
            default:
                throw new DSGJSONException(token, Type.END_ARRAY);
            }
        }
        // Consume the final END_ARRAY token, such that additional read calls behave as
        // expected.
        reader.consumeToken();
        this.items = newItems;
    }
}

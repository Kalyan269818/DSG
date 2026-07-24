package dsg.activitystreams;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import dsg.json.DSGJSONArray;
import dsg.json.DSGJSONException;
import dsg.json.DSGJSONObject;
import dsg.json.DSGJSONValue;

/**
 * An ordered or unordered collection of ActivityStreams objects.
 */
public class DSGActivityStreamsCollection<E extends DSGActivityStreamsEntity> extends DSGActivityStreamsObject {
    /** Type name of a collection . */
    public static final String COLLECTION_TYPE_NAME = "Collection";
    /** Type name of an ordered collection . */
    public static final String ORDERED_COLLECTION_TYPE_NAME = "OrderedCollection";

    /** Available types of collections. */
    public static enum DSGCollectionType {
        /** An unordered collection. */
        COLLECTION,
        /** An ordered collection. */
        ORDERED_COLLECTION;

        /**
         * Return the collection type with the given {@code name}.
         *
         * @param name the name of the collection type.
         * @return the collection type corresponding to {@code name}.
         * @throws IllegalArgumentException if no collection type with that name exists.
         */
        public static DSGCollectionType forName(String name) {
            switch (name) {
            case COLLECTION_TYPE_NAME:
                return COLLECTION;
            case ORDERED_COLLECTION_TYPE_NAME:
                return ORDERED_COLLECTION;
            default:
                throw new IllegalArgumentException(name + ": not a valid collection type name");
            }
        }

        @Override
        public String toString() {
            switch (this) {
            case COLLECTION:
                return COLLECTION_TYPE_NAME;
            case ORDERED_COLLECTION:
                return ORDERED_COLLECTION_TYPE_NAME;
            default:
                throw new IllegalStateException(this.name() + ": unknown collection type");
            }
        }
    }

    /** JSON object key for a collection's items. */
    private static final String ITEMS_KEY = "items";
    /** JSON object key for an ordered collection's items. */
    private static final String ORDERED_ITEMS_KEY = "orderedItems";
    /** JSON object key for the collection's total number of items. */
    private static final String TOTAL_ITEMS_KEY = "totalItems";

    /** The collection's items. */
    private List<E> items;

    /**
     * Initialize an empty collection with the default type.
     *
     * @param id the collection's id.
     */
    public DSGActivityStreamsCollection(String id) {
        super(id, DSGCollectionType.COLLECTION.toString());
        this.items = new ArrayList<>();
    }

    /**
     * Initialize an empty collection with the named {@code type}.
     *
     * @param link a link pointing to this collection.
     * @param type the type of the collection.
     */
    public DSGActivityStreamsCollection(DSGActivityStreamsLink link, DSGCollectionType type) {
        super(link.getTarget(), type.toString());
        this.items = new ArrayList<>();
    }

    /**
     * Initialize an empty collection of the named {@code type}.
     *
     * @param id   the collection's id.
     * @param type the collection's type.
     */
    public DSGActivityStreamsCollection(String id, DSGCollectionType type) {
        this(id, type, null);
    }

    /**
     * Initialize a collection of the named {@code type}.
     *
     * @param id    an URI identifying the collection.
     * @param type  the collection's type.
     * @param items the collection's items.
     */
    public DSGActivityStreamsCollection(String id, DSGCollectionType type, List<E> items) {
        super(id, type.toString());
        if (items == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = items;
        }
    }

    /**
     * Parse a collection from its JSON representation.
     *
     * @param json the JSON object to parse.
     * @param c    the class object for the components of this collection.
     * @throws DSGJSONException if {@code json} is not an AcitivtyStreams
     *                          collection.
     */
    public DSGActivityStreamsCollection(DSGJSONObject json, Class<E> c) throws DSGJSONException {
        super(json);
        DSGJSONArray jsonItems;
        try {
            switch (DSGCollectionType.forName(getType())) {
            case COLLECTION:
                jsonItems = json.getMemberAs(ITEMS_KEY);
                break;
            case ORDERED_COLLECTION:
                jsonItems = json.getMemberAs(ORDERED_ITEMS_KEY);
                break;
            default:
                throw new DSGJSONException("Collection has unknown type");
            }
        } catch (IllegalArgumentException iae) {
            throw new DSGJSONException(iae.getMessage());
        }

        this.items = new ArrayList<>();
        if (jsonItems == null) {
            return;
        }
        for (DSGJSONValue item : jsonItems) {
            try {
                Constructor<E> constructor = c.getDeclaredConstructor(item.getClass());
                this.items.add(constructor.newInstance(item));
            } catch (NoSuchMethodException e) {
                throw new DSGJSONException(
                        "Cannot deserialize " + c.getTypeName() + " from JSON " + item.getType().toString());
            } catch (InstantiationException | IllegalAccessException e) {
                // Should never happen.
                throw new IllegalStateException(e);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof DSGJSONException) {
                    throw (DSGJSONException) cause;
                }
                // Should never happen.
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Return the collection's items.
     *
     * @return the collection's items.
     */
    public List<E> getItems() {
        return items;
    }

    /**
     * Replace the collection's list of items.
     *
     * @param items the collection's new list of items.
     */
    public void setItems(List<E> items) {
        this.items = items;
    }

    /**
     * Add an item to this collection.
     *
     * @param item the item to add to the collection.
     */
    public void add(E item) {
        this.items.add(item);
    }

    /**
     * Return the total number of items present in the collection.
     *
     * @return the total number of items in the collection.
     */
    public long getTotalItems() {
        return this.items.size();
    }

    @Override
    public DSGJSONObject toJSON() {
        DSGJSONObject object = super.toJSON();
        object.setMember(TOTAL_ITEMS_KEY, items.size());
        if (items == null) {
            return object;
        }

        DSGJSONArray jsonItems = new DSGJSONArray();
        for (E item : items) {
            jsonItems.add(item);
        }
        switch (DSGCollectionType.forName(getType())) {
        case COLLECTION:
            object.setMember(ITEMS_KEY, jsonItems);
            break;
        case ORDERED_COLLECTION:
            object.setMember(ORDERED_ITEMS_KEY, jsonItems);
            break;
        default:
            // Should never happen because we are serializing a valid collection.
            throw new IllegalStateException("Unknown collection type");
        }
        return object;
    }
}

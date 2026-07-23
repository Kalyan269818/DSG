package dsg.json;

/**
 * Interface for objects that can be (de-)serialized from and to a
 * {@link DSGJSONValue}.
 */
public interface DSGJSONSerializable {
    /**
     * Transforms the object into its JSON representation.
     * 
     * @return the JSON representation of the serialized object.
     */
    public DSGJSONValue toJSON();
}

package org.evrete.guides.showcase;

/**
 * Represents a message with a type and payload.
 */
public record Message(String type, String payload) {

    /**
     * Reads the payload of a message as an object of the specified type.
     *
     * @param type the class representing the type of the object to be deserialized
     * @param <T> the type of the object to be deserialized
     * @return the deserialized object of type T
     */
    public <T> T readPayloadAs(Class<T> type) {
        return ShowcaseUtils.fromJson(this.payload, type);
    }
}

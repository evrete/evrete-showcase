package org.evrete.guides.showcase;

/**
 * The MessageWriter interface represents a component responsible for writing
 * WebSocket messages into the currently opened session.
 */
public interface MessageWriter {
    void writeMessage(String type, Object payload);

    default void writeMessage(String type) {
        writeMessage(type, "");
    }
}

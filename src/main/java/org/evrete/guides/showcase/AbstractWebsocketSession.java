package org.evrete.guides.showcase;

public abstract class AbstractWebsocketSession {
    protected final MessageWriter writer;

    protected abstract void onMessage(String body);

    public AbstractWebsocketSession(MessageWriter writer) {
        this.writer = writer;
    }

    /**
     * Closes the WebSocket session. By default, this method does nothing.
     *
     * <p>
     * This method is meant to be overridden by subclasses to perform any
     * necessary cleanup or closing operations when closing the WebSocket
     * session.
     * </p>
     */
    protected void onClose() {
        // Do nothing by default
    }

    /**
     * Called when the WebSocket session is first created.
     *
     * <p>
     * This method is called when the WebSocket session is initially created.
     * It can be overridden by subclasses to perform any necessary initialization
     * or setup operations for the WebSocket session.
     * </p>
     */
    protected void onCreate() {
        // Do nothing by default
    }
}

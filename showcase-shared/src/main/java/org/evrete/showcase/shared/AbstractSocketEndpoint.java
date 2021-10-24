package org.evrete.showcase.shared;

import javax.websocket.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public abstract class AbstractSocketEndpoint<T extends AbstractSocketSession> {
    private static final Logger LOGGER  = Logger.getLogger(AbstractSocketEndpoint.class.getSimpleName());
    private final Map<Session, T> sessionMap = new ConcurrentHashMap<>();

    protected abstract T newSession(Session session) throws Exception;

    @OnOpen
    public final void onOpen(Session session) {
        try {
            T wrapper = newSession(session);
            sessionMap.put(session, wrapper);
        } catch (Exception e) {
            closeSession(session);
        }
    }

    @OnError
    @SuppressWarnings("unused")
    public final void onError(Session session, Throwable t) {
        closeSession(session);
    }

    @OnMessage
    public final void processMessage(String message, Session session) {
        T sessionWrapper = sessionMap.get(session);
        if(sessionWrapper == null) {
            return; // Abnormal situation
        }
        try {
            WsMessage m = Utils.fromJson(message, WsMessage.class);
            if(m.type != null) {
                sessionWrapper.process(m.type, m.payload);
            } else {
                LOGGER.warning("Skipping message with unknown type: '" + message + "'");
            }
        } catch (Throwable e) {
            sessionWrapper.send(e);
        }
    }


    @OnClose
    public final void onClose(Session session) {
        closeSession(session);
    }

    private void closeSession(Session session) {
        T wrapper = sessionMap.remove(session);
        if (wrapper != null) {
            wrapper.closeSession();
        }
    }

}

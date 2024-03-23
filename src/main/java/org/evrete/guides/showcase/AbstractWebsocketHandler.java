package org.evrete.guides.showcase;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractWebsocketHandler<S extends AbstractWebsocketSession> implements WebSocketHandler {
    private final Map<String, S> sessions = new ConcurrentHashMap<>();

    protected abstract S newSession(MessageWriter writer);

    @Override
    public final void afterConnectionEstablished(@NonNull WebSocketSession session) {
        S s =  this.newSession(new MessageWriterImpl(session));
        this.sessions.put(session.getId(), s);
        s.onCreate();
    }

    @Override
    public final void handleMessage(@NonNull WebSocketSession session, @NonNull WebSocketMessage<?> message) {
        S existing = this.sessions.get(session.getId());
        if(existing != null) {
            existing.onMessage(message.getPayload().toString());
        }
    }

    @Override
    public final void handleTransportError(@NonNull WebSocketSession session, @NonNull Throwable exception) {
        // Add exception handling here if needed.
        closeSession(session);
    }

    @Override
    public final void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) {
        closeSession(session);
    }

    private void closeSession(@NonNull WebSocketSession session) {
        S existing = this.sessions.remove(session.getId());
        if(existing != null) {
            existing.onClose();
        }
    }

    @Override
    public final boolean supportsPartialMessages() {
        return false;
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class MessageWriterImpl implements MessageWriter {
        private final WebSocketSession session;

        MessageWriterImpl(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public void writeMessage(String type, @NonNull Object object) {
            try {
                final String payload;
                if(object instanceof String s) {
                    payload = s;
                } else if(object instanceof CharSequence cs) {
                    payload = String.valueOf(cs);
                } else {
                    payload = ShowcaseUtils.toJson(object);
                }

                Message message = new Message(type, payload);

                this.session.sendMessage(new TextMessage(ShowcaseUtils.toJson(message)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}

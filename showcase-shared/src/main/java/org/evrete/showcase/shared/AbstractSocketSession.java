package org.evrete.showcase.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.websocket.Session;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractSocketSession {
    private static final Logger LOGGER = Logger.getLogger(AbstractSocketSession.class.getSimpleName());
    private final Session session;
    private final GsonBuilder gsonBuilder = new GsonBuilder();

    public abstract void process(String type, String payload) throws Exception;

    public abstract void closeSession();

    public AbstractSocketSession(Session session) {
        session.setMaxTextMessageBufferSize(1024 * 1024);
        session.setMaxIdleTimeout(1000 * 60 * 30);
        this.session = session;
    }

    private void sendRaw(String rawMessage) {
        if (session.isOpen()) {
            synchronized (session) {
                try {
                    session.getBasicRemote().sendText(rawMessage);
                } catch (Throwable t) {
                    LOGGER.log(Level.WARNING, "Unable to send to remote", t);
                }
            }
        }
    }

    private Gson gson() {
        return gsonBuilder.create();
    }

    protected <T> T fromJson(String s, Class<T> cl) {
        return gson().fromJson(s, cl);
    }

    public void send(Throwable e) {
        sendError(e.getMessage());
    }

    public void sendError(String err) {
        send("ERROR", err);
    }

    private void send(WsMessage msg) {
        sendRaw(new Gson().toJson(msg));
    }

    protected void send(String type, String payload) {
        send(new WsMessage(type, payload));
    }

    protected <T> void send(String type, T obj) {
        String payload;
        if(obj instanceof String) {
            payload = (String) obj;
        } else {
            payload = gson().toJson(obj);
        }
        send(type, payload);
    }

    protected void send(String type) {
        send(type, null);
    }


}

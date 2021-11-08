package org.evrete.showcase.stock;

import org.evrete.showcase.shared.AbstractSocketEndpoint;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/api/ws")
public class StockSocketEndpoint extends AbstractSocketEndpoint<StockSocketSession> {

    @Override
    protected StockSocketSession newSession(Session session)  {
        return new StockSocketSession(session);
    }
}

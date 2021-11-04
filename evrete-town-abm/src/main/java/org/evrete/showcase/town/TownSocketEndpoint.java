package org.evrete.showcase.town;

import org.evrete.showcase.shared.AbstractSocketEndpoint;

import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws/socket")
public class TownSocketEndpoint extends AbstractSocketEndpoint<TownSocketSession> {

    @Override
    protected TownSocketSession newSession(Session session) {
        return new TownSocketSession(session);
    }

    /*
        wrapper.getMessenger().sendUnchecked(new ConfigMessage(AppContext.DEFAULT_XML));




        // DELETE BELOW
        List<XYPoint> buffer = new LinkedList<>();

        for (XYPoint home : AppContext.MAP_DATA.homes) {
            buffer.add(home);
            if (buffer.size() == 64) {
                wrapper.getMessenger().sendUnchecked(new MapMessage("residential", buffer));
                buffer.clear();
            }
        }
        if (buffer.size() > 0) {
            wrapper.getMessenger().sendUnchecked(new MapMessage("residential", buffer));
            buffer.clear();
        }

        for (XYPoint home : AppContext.MAP_DATA.businesses) {
            buffer.add(home);
            if (buffer.size() == 64) {
                wrapper.getMessenger().sendUnchecked(new MapMessage("businesses", buffer));
                buffer.clear();
            }
        }
        if (buffer.size() > 0) {
            wrapper.getMessenger().sendUnchecked(new MapMessage("businesses", buffer));
            buffer.clear();
        }
*/
}

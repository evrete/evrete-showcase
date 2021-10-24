package org.evrete.showcase.shared;

import java.util.Objects;

public class WsMessage {
    public String type;
    public String payload;

    public WsMessage(String type, String payload) {
        this.type = Objects.requireNonNull(type);
        this.payload = payload;
    }
}

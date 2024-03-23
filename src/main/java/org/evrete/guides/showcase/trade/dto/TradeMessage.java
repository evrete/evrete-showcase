package org.evrete.guides.showcase.trade.dto;

public class TradeMessage extends SlotData {
    public final String message;

    public TradeMessage(SlotData time, String message) {
        super(time);
        this.message = message;
    }
}

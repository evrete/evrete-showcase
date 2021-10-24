package org.evrete.showcase.stock.model;

public class TradeMessage extends SlotData {
    public String message;

    public TradeMessage(SlotData time, String message) {
        super(time);
        this.message = message;
    }
}

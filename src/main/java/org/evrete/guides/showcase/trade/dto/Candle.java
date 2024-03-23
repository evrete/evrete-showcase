package org.evrete.guides.showcase.trade.dto;


public class Candle extends SlotData {
    public final double open;
    public final double high;
    public final double low;
    public final double close;

    public Candle(int id, JsonCandle candle) {
        super(id);
        this.id = id;
        this.open = candle.open;
        this.high = candle.high;
        this.low = candle.low;
        this.close = candle.close;
    }
}

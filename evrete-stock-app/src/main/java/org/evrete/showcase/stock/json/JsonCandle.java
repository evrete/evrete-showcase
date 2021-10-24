package org.evrete.showcase.stock.json;

public class JsonCandle {
    public double open;
    public double high;
    public double low;
    public double close;

    public JsonCandle(double open, double high, double low, double close) {
        this();
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    public JsonCandle() {
    }

    @Override
    public String toString() {
        return "{" +
                "open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                '}';
    }
}

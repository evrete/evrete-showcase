package org.evrete.showcase.stock.json;

public class JsonCandle {
    public double open;
    public double high;
    public double low;
    public double close;

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

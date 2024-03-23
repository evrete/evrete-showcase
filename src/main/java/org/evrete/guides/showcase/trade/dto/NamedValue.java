package org.evrete.guides.showcase.trade.dto;

public class NamedValue extends SlotData {
    public final String name;
    public final double value;

    public NamedValue(SlotData time, String name, double value) {
        super(time);
        this.name = name;
        this.value = value;
    }
}

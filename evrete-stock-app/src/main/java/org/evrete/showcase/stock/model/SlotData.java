package org.evrete.showcase.stock.model;

public class SlotData {
    public int id;

    public SlotData(int id) {
        this.id = id;
    }

    public SlotData(SlotData other) {
        this.id = other.id;
    }

}

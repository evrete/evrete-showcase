package org.evrete.showcase.stock.model;

public class NamedValue extends SlotData {
    public final String name;
    public final double value;

    public NamedValue(int id, String name, double value) {
        super(id);
        this.name = name;
        this.value = value;
    }

    public NamedValue(SlotData time, String name, double value) {
        super(time);
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return  getClass().getSimpleName() + "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}

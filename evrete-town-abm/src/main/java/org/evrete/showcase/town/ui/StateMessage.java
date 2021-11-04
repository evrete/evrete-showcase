package org.evrete.showcase.town.ui;

import org.evrete.showcase.town.model.LocationType;

import java.util.*;

public class StateMessage {
    public final String time;
    public final int cellSize;
    public final boolean reset;
    private final Map<String, List<Area>> layers = new HashMap<>();
    private final Map<String, Double> total = new LinkedHashMap<>();
    private Map<String, Long> activity;

    public StateMessage(boolean reset, int cellSize, String time) {
        this.time = time;
        this.cellSize = cellSize;
        this.reset = reset;
    }

    public void add(LocationType state, Area area) {
        layers.computeIfAbsent(state.name(), k -> new LinkedList<>()).add(area);
    }

    public void setTotal(Counter summary) {
        int total = 0;
        for (LocationType state : LocationType.values()) {
            total += summary.getCount(state);
        }

        for (LocationType state : LocationType.values()) {
            this.total.put(state.name(), 1.0 * summary.getCount(state) / total);
        }
    }

    public StateMessage setActivity(Map<String, Long> activity) {
        this.activity = activity;
        return this;
    }

    @Override
    public String toString() {
        return "StateMessage{" +
                "time='" + time + '\'' +
                ", cellSize=" + cellSize +
                ", reset=" + reset +
                ", layers=" + layers +
                ", total=" + total +
                '}';
    }
}

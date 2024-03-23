package org.evrete.guides.showcase.town.dto;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

public class StateMessage {
    public final String time;
    public final int cellSize;
    public final boolean reset;
    private final IntList[] layers = new IntList[LocationType.values().length];
    private final double[] stats;
    private Map<String, Long> activity;

    public StateMessage(boolean reset, int cellSize, String time, double[] stats) {
        this.time = time;
        this.cellSize = cellSize;
        this.reset = reset;
        this.stats = stats;

        for (LocationType t : LocationType.values()) {
            int ord = t.ordinal();
            this.layers[ord] = new IntList();
        }
    }

    public void add(LocationType state, int x, int y, int opacity) {
        IntList l = layers[state.ordinal()];
        l.add(x);
        l.add(y);
        l.add(opacity);
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
                ", activity=" + activity +
                ", layers=" + Arrays.toString(layers) +
                ", distribution=" + Arrays.toString(stats) +
                '}';
    }

    static class IntList extends LinkedList<Integer> {

    }
}

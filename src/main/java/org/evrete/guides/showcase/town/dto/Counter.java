package org.evrete.guides.showcase.town.dto;


/**
 * <p>
 * Number of entities by location type
 * </p>
 */
public class Counter {
    private final int[] counts = new int[LocationType.values().length];

    void add(LocationType state, int count) {
        this.counts[state.ordinal()] += count;
    }

    public int getCount(LocationType state) {
        return counts[state.ordinal()];
    }


}

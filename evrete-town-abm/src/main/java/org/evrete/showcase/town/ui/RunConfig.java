package org.evrete.showcase.town.ui;

import org.evrete.showcase.town.model.LocationType;

import java.util.ArrayList;
import java.util.List;

public class RunConfig {
    public static int MAX_POPULATION = 20_000;
    private static final String[] LOCATION_TYPES = new String[LocationType.values().length];

    static {
        for (LocationType t : LocationType.values()) {
            LOCATION_TYPES[t.ordinal()] = t.name();
        }
    }

    public Viewport viewport = new Viewport();
    public int population = MAX_POPULATION / 2;
    public List<JsonRule> rules = new ArrayList<>();
    @SuppressWarnings("unused")
    public int maxPopulation = MAX_POPULATION;
    public int workingPercent = 70;
    public int leisureAtHomePercent = 45;
    public int commuteSpeed = 25;
    @SuppressWarnings("unused")
    public String[] locationTypes = LOCATION_TYPES;
}

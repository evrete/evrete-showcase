package org.evrete.showcase.town.ui;

import java.util.ArrayList;
import java.util.List;

public class RunConfig {
    public static int MAX_POPULATION = 20_000;
    public Viewport viewport = new Viewport();
    public int population = MAX_POPULATION;
    public List<JsonRule> rules = new ArrayList<>();
    public int maxPopulation = MAX_POPULATION;
    public int workingPercent = 70;
    public int leisureAtHomePercent = 45;
    public int commuteSpeed = 30;
}

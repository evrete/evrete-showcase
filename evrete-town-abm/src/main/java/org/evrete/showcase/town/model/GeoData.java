package org.evrete.showcase.town.model;

import java.util.ArrayList;
import java.util.List;

public class GeoData {
    public List<XYPoint> homes = new ArrayList<>();
    public List<XYPoint> businesses = new ArrayList<>();

    @Override
    public String toString() {
        return "GeoData{" +
                "homes=" + homes.size() +
                ", businesses=" + businesses.size() +
                '}';
    }
}

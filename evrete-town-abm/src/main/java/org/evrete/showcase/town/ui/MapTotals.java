package org.evrete.showcase.town.ui;

import org.evrete.showcase.town.model.Entity;
import org.evrete.showcase.town.model.LocationType;

/**
 * A utility class that holds entity totals by zoom and coordinates
 */
public class MapTotals {
    private final Counter[][][] counts = new Counter[Integer.SIZE - Integer.numberOfLeadingZeros(Viewport.MAX_SIZE)][][];

    public MapTotals() {
        for (int z = 0; z < counts.length; z++) {
            int areaSize = 1 << z;
            counts[z] = new Counter[areaSize][areaSize];
            for (int i = 0; i < areaSize; i++) {
                for (int j = 0; j < areaSize; j++) {
                    counts[z][i][j] = new Counter();
                }
            }
        }
    }

    public Counter getCellSummary(int zoom, int zoomedX, int zoomedY) {
        Counter[][] counts = this.counts[zoom];
        return counts[zoomedX][zoomedY];
    }

    //Zero zoom always contains the total count of people
    public Counter getTotal() {
        return counts[0][0][0];
    }

    private void addTo(LocationType type, int x, int y, int count) {
        if (x < 0 || y < 0) throw new IllegalArgumentException();
        for (int zoom = 0; zoom < counts.length; zoom++) {
            Counter[][] counts = this.counts[zoom];
            int div = Viewport.MAX_SIZE >> zoom;
            int zoomedX = x / div;
            int zoomedY = y / div;
            counts[zoomedX][zoomedY].add(type, count);
        }
    }

    /*
        public void addTo(LocationType state, Entity location, int count) {
            addTo(state, location.getNumber("x"), location.getNumber("y"), count);
        }
    */
    public void addTo(Entity location, int count) {
        LocationType type = LocationType.valueOf(location.type);
        addTo(type, location.getNumber("x"), location.getNumber("y"), count);
    }
}

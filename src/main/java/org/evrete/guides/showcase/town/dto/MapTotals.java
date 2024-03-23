package org.evrete.guides.showcase.town.dto;

import static org.evrete.guides.showcase.town.dto.Viewport.MAX_SIZE;

/**
 * A utility class that holds entity totals by zoom and coordinates
 */
public class MapTotals {
    private final Counter[][][] counts = new Counter[Integer.SIZE - Integer.numberOfLeadingZeros(MAX_SIZE)][][];

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
        for (int zoom = 0; zoom < counts.length; zoom++) {
            Counter[][] counts = this.counts[zoom];
            int div = MAX_SIZE >> zoom;
            int zoomedX = x / div;
            int zoomedY = y / div;
            counts[zoomedX][zoomedY].add(type, count);
        }
    }

    public void addTo(Entity location, int count) {
        LocationType type = LocationType.valueOf(location.type);
        int x = location.getNumber("x");
        int y = location.getNumber("y");
        if (x >= 0 && y >= 0 && x < MAX_SIZE && y < MAX_SIZE) {
            addTo(type, x, y, count);
        }
    }
}

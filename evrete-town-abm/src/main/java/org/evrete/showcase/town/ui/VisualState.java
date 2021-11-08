package org.evrete.showcase.town.ui;

import org.evrete.showcase.town.model.*;

import java.util.*;

public class VisualState {
    private static final double[] BRIGHTNESS;
    private final World world;
    private final Map<String, Integer> cache = new HashMap<>();
    private final List<XYPoint> scanScope = new ArrayList<>();
    private final MapTotals peopleLocations = new MapTotals();
    //private final MapTotals buildingLocations = new MapTotals();
    private int areaSize;
    private int responseZoom;
    private int roundBits;
    private boolean resetMessage = true;
    private final Map<Entity, Entity> lastPersonLocationMap = new HashMap<>();

    static {
        BRIGHTNESS = new double[LocationType.values().length];
        BRIGHTNESS[LocationType.COMMUTING.ordinal()] = 0.7;
        BRIGHTNESS[LocationType.BUSINESS.ordinal()] = 0.3;
        BRIGHTNESS[LocationType.RESIDENTIAL.ordinal()] = 0.2;
    }

    public VisualState(World world, Viewport initial) {
        this.world = world;
        setViewport(initial);
    }


    private static double sigmoidAdjusted(double x) {
        double sigmoid = 1 / (1 + Math.exp(-x));

        double sig = 2.0 * (sigmoid - 0.5);
        return Math.max(sig, 0.0);
    }

    public void setViewport(Viewport viewport) {
        this.resetMessage = true;
        this.cache.clear();

        this.responseZoom = viewport.zoom + viewport.resolution;
        this.areaSize = Viewport.MAX_SIZE >> responseZoom;

        this.scanScope.clear();
        this.roundBits = Viewport.MAX_ZOOM - responseZoom;
        int viewportX = viewport.x;
        int viewportY = viewport.y;


        int x = (viewportX >> this.roundBits) << this.roundBits;

        int viewportSize = Viewport.MAX_SIZE >> viewport.zoom;
        while (x < viewportX + viewportSize) {
            int y = (viewportY >> roundBits) << roundBits;
            while (y < viewportY + viewportSize) {
                scanScope.add(new XYPoint(x, y));
                y += areaSize;
            }
            x += areaSize;
        }
    }

    public StateMessage getState(WorldTime time) {
        for (Entity person : world.population) {
            Entity currentLocation = Objects.requireNonNull(person.getProperty("location"));
            Entity previousLocation = lastPersonLocationMap.get(person);
            if (previousLocation == null) {
                // First time we handle this person
                peopleLocations.addTo(currentLocation, 1);
                lastPersonLocationMap.put(person, currentLocation);
            } else {
                if (previousLocation != currentLocation) {
                    peopleLocations.addTo(currentLocation, 1);
                    peopleLocations.addTo(previousLocation, -1);
                    lastPersonLocationMap.put(person, currentLocation);
                }
            }
        }

        StateMessage message = new StateMessage(resetMessage, areaSize, time.toString());

        Counter total = peopleLocations.getTotal();
        message.setTotal(total);

        // Compute averages for each location type
        int[] counts = new int[LocationType.values().length];
        int[] nonZeroCells = new int[LocationType.values().length];
        for (XYPoint xy : scanScope) {
            int zoomedX = (xy.x >> roundBits);
            int zoomedY = (xy.y >> roundBits);
            Counter peopleCounter = peopleLocations.getCellSummary(responseZoom, zoomedX, zoomedY);
            for (LocationType t : LocationType.values()) {
                int count = peopleCounter.getCount(t);
                if (count > 0) {
                    int i = t.ordinal();
                    counts[i] += count;
                    nonZeroCells[i]++;
                }
            }
        }

        for (XYPoint xy : scanScope) {
            int zoomedX = (xy.x >> roundBits);
            int zoomedY = (xy.y >> roundBits);
            Counter peopleCounter = peopleLocations.getCellSummary(responseZoom, zoomedX, zoomedY);
            for (LocationType type : LocationType.values()) {
                int typeId = type.ordinal();
                int count = peopleCounter.getCount(type);
                String cellId = "c" + typeId + "_" + zoomedX + "_" + zoomedY;
                if (updateCache(cellId, count) || resetMessage) {
                    // This is a new situation, an update message should be added
                    double opacity;
                    if (count == 0) {
                        opacity = 0.0;
                    } else {
                        double average = 1.0 * counts[typeId] / nonZeroCells[typeId];
                        double globalAverage = 0.3 + 1.0 * total.getCount(type) / world.population.size();
                        opacity = BRIGHTNESS[typeId] * sigmoidAdjusted(globalAverage * count / average);
                    }

                    message.add(type, new Area(cellId, xy.x, xy.y, opacity));
                }
            }
        }

        this.resetMessage = false;
        return message;
    }

    private boolean updateCache(String id, int count) {
        Integer cached = cache.get(id);
        if (cached == null || cached != count) {
            cache.put(id, count);
            return true;
        } else {
            return false;
        }
    }
}

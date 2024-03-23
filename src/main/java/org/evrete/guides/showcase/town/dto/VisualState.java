package org.evrete.guides.showcase.town.dto;


import java.util.*;

public class VisualState {
    private static final int ARR_SIZE = LocationType.values().length;
    private static final double[] BRIGHTNESS;
    private final World world;
    private final Map<String, Integer> cache = new HashMap<>();
    private final List<XYPoint> scanScope = new ArrayList<>();
    private final MapTotals peopleLocations = new MapTotals();
    private int areaSize;
    private int responseZoom;
    private int roundBits;
    private boolean resetMessage = true;
    private final Map<Entity, Entity> lastPersonLocationMap = new HashMap<>();

    static {
        BRIGHTNESS = new double[ARR_SIZE];
        BRIGHTNESS[LocationType.COMMUTING.ordinal()] = 0.5;
        BRIGHTNESS[LocationType.BUSINESS.ordinal()] = 0.4;
        BRIGHTNESS[LocationType.RESIDENTIAL.ordinal()] = 0.4;
    }

    public VisualState(World world, Viewport initial) {
        this.world = world;
        setViewport(initial);
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

        // Compute distribution
        Counter total = peopleLocations.getTotal();
        double[] stats = new double[ARR_SIZE];
        for (LocationType t : LocationType.values()) {
            int ord = t.ordinal();
            stats[ord] = 1.0 * total.getCount(t) / world.population.size();
        }


        StateMessage message = new StateMessage(resetMessage, areaSize, time.toString(), stats);


        // Compute max people count for each location type
        int[] maxCounts = new int[LocationType.values().length];
        for (XYPoint xy : scanScope) {
            int zoomedX = (xy.x >> roundBits);
            int zoomedY = (xy.y >> roundBits);
            Counter peopleCounter = peopleLocations.getCellSummary(responseZoom, zoomedX, zoomedY);
            for (LocationType t : LocationType.values()) {
                int ord = t.ordinal();
                int count = peopleCounter.getCount(t);
                maxCounts[ord] = Math.max(maxCounts[ord], count);
            }
        }

        for (XYPoint xy : scanScope) {
            int zoomedX = (xy.x >> roundBits);
            int zoomedY = (xy.y >> roundBits);
            Counter peopleCounter = peopleLocations.getCellSummary(responseZoom, zoomedX, zoomedY);
            for (LocationType type : LocationType.values()) {
                int ord = type.ordinal();
                int count = peopleCounter.getCount(type);
                String cellId = "c" + ord + "_" + zoomedX + "_" + zoomedY;

                int opacity;
                if (count == 0) {
                    opacity = 0;
                } else {
                    //double op = emphasizeLower(1.0 * count / maxCounts[ord]);
                    double op = 1.0 * count / maxCounts[ord];
                    double globalRatio = emphasizeLower(stats[ord]);
                    opacity = (int) (100 * op * globalRatio);
                }

                if (updateCache(cellId, opacity) || resetMessage) {
                    // This is a new situation, an update message should be added
                    message.add(type, xy.x / areaSize, xy.y / areaSize, (int) (BRIGHTNESS[ord] * opacity));
                }
            }
        }

        this.resetMessage = false;
        return message;
    }

    private static double emphasizeLower(double x) {
        return Math.sqrt(2 * x - x * x);
    }

    private boolean updateCache(String id, int opacity) {
        int smoothened = opacity / 4; // Ignore 4% changes or less
        Integer cached = cache.get(id);
        if (cached == null || cached != smoothened) {
            cache.put(id, smoothened);
            return true;
        } else {
            return false;
        }
    }
}

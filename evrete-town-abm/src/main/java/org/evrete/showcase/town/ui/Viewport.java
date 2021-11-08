package org.evrete.showcase.town.ui;

public class Viewport {
    public static int MAX_SIZE = 2048;
    public static int MAX_ZOOM = 31 - Integer.numberOfLeadingZeros(MAX_SIZE);
    // Top left pixel, x-coordinate
    public int x;
    // Top left pixel, y-coordinate
    public int y;
    // Size in pixels
    public int zoom;
    // Aggregation level
    public int resolution = 5; // 2^5 = 32 (map will be split to 32x32 cells by default)

    @Override
    public String toString() {
        return "Viewport{" +
                "x=" + x +
                ", y=" + y +
                ", zoom=" + zoom +
                '}';
    }

}

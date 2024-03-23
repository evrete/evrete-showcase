package org.evrete.guides.showcase.town.dto;


public class Area extends XYPoint {
    public final int opacity;

    public Area(int x, int y, int opacity) {
        super(x, y);
        this.opacity = opacity;
    }
}

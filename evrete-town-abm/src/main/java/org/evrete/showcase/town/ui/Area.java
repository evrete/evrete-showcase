package org.evrete.showcase.town.ui;

import org.evrete.showcase.town.model.XYPoint;

public class Area extends XYPoint {
    public final int opacity;

    public Area(int x, int y, int opacity) {
        super(x, y);
        this.opacity = opacity;
    }
}

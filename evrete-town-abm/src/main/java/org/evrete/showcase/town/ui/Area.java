package org.evrete.showcase.town.ui;

import org.evrete.showcase.town.model.XYPoint;

public class Area extends XYPoint {
    public final String id;
    public final String opacity;

    public Area(String id, int x, int y, double opacity) {
        super(x, y);
        this.id = id;
        this.opacity = String.format("%.5f", opacity);
    }
}

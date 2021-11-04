package org.evrete.showcase.town.model;

public class XYPoint {
    public int x;
    public int y;

    public XYPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XYPoint point = (XYPoint) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return x + 31 * y;
    }
}

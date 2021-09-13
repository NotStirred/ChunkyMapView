package io.github.notstirred.util;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BoundingBoxf2d {
    MutVec2f minExtents;
    MutVec2f maxExtents;

    public BoundingBoxf2d(float x1, float y1, float x2, float y2) {
        this.minExtents = new MutVec2f(min(x1, x2), min(y1, y2));
        this.maxExtents = new MutVec2f(max(x1, x2), max(y1, y2));
    }

    public void moveBy(MutVec2f vec2f) {
        minExtents.add(vec2f);
        maxExtents.add(vec2f);
    }

    public void moveTo(MutVec2f vec2f) {
        minExtents.set(vec2f);
        MutVec2f newMax = maxExtents.subbed(minExtents);
        newMax.add(vec2f);
        maxExtents.set(newMax);
    }

    public MutVec2f getMinExtents() {
        return minExtents;
    }

    public MutVec2f getMaxExtents() {
        return maxExtents;
    }

    public Vec2f getImmutableMinExtents() {
        return new Vec2f(minExtents.getX(), minExtents.getY());
    }

    public Vec2f getImmutableMaxExtents() {
        return new Vec2f(maxExtents.getX(), maxExtents.getY());
    }
}

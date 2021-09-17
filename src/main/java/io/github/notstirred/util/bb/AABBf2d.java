package io.github.notstirred.util.bb;

import io.github.notstirred.util.vec.Vec2f;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Data
@RequiredArgsConstructor
public class AABBf2d {
    private final Vec2f minExtents;
    private final Vec2f maxExtents;

    public AABBf2d(float x1, float y1, float x2, float y2) {
        this.minExtents = new Vec2f(min(x1, x2), min(y1, y2));
        this.maxExtents = new Vec2f(max(x1, x2), max(y1, y2));
    }

    public boolean intersects(float x, float y) {
        return (x >= minExtents.x() && x <= maxExtents.x()) &&
                (y >= minExtents.y() && y <= maxExtents.y());
    }
}

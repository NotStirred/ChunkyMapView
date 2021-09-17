package io.github.notstirred.util.bb;

import io.github.notstirred.util.vec.Vec2i;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Data
@RequiredArgsConstructor
public class AABBi2d {
    private final Vec2i minExtents;
    private final Vec2i maxExtents;

    public AABBi2d(int x1, int y1, int x2, int y2) {
        this.minExtents = new Vec2i(min(x1, x2), min(y1, y2));
        this.maxExtents = new Vec2i(max(x1, x2), max(y1, y2));
    }

    public boolean intersects(int x, int y) {
        return (x >= minExtents.x() && x <= maxExtents.x()) &&
                (y >= minExtents.y() && y <= maxExtents.y());
    }
}
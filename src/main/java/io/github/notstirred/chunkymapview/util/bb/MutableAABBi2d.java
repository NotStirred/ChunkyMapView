package io.github.notstirred.chunkymapview.util.bb;

import io.github.notstirred.chunkymapview.util.vec.MutVec2i;
import io.github.notstirred.chunkymapview.util.vec.Vec2i;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Data
@RequiredArgsConstructor
public class MutableAABBi2d {
    private final MutVec2i minExtents;
    private final MutVec2i maxExtents;

    public MutableAABBi2d(int x1, int y1, int x2, int y2) {
        this.minExtents = new MutVec2i(min(x1, x2), min(y1, y2));
        this.maxExtents = new MutVec2i(max(x1, x2), max(y1, y2));
    }

    public void moveBy(MutVec2i Vec2i) {
        minExtents.add(Vec2i);
        maxExtents.add(Vec2i);
    }

    public void moveTo(MutVec2i Vec2i) {
        minExtents.set(Vec2i);
        MutVec2i newMax = maxExtents.subbed(minExtents);
        newMax.add(Vec2i);
        maxExtents.set(newMax);
    }

    public Vec2i getImmutableMinExtents() {
        return new Vec2i(minExtents.x(), minExtents.y());
    }

    public Vec2i getImmutableMaxExtents() {
        return new Vec2i(maxExtents.x(), maxExtents.y());
    }

    public Vec2i size() {
        return maxExtents.subbed(minExtents).toImmutable();
    }

    public AABBi2d toImmutable() {
        return new AABBi2d(this.minExtents.toImmutable(), this.maxExtents.toImmutable());
    }

    public AABBf2d toFloatBox() {
        return new AABBf2d(this.minExtents.toImmutable().toFloatVec(), this.maxExtents.toImmutable().toFloatVec());
    }
}
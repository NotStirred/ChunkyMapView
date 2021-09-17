package io.github.notstirred.util.bb;

import io.github.notstirred.util.vec.MutVec2f;
import io.github.notstirred.util.vec.Vec2f;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import static java.lang.Math.max;
import static java.lang.Math.min;

@Data
@RequiredArgsConstructor
public class MutableAABBf2d {
    private final MutVec2f minExtents;
    private final MutVec2f maxExtents;

    public MutableAABBf2d(float x1, float y1, float x2, float y2) {
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

    public Vec2f getImmutableMinExtents() {
        return new Vec2f(minExtents.x(), minExtents.y());
    }

    public Vec2f getImmutableMaxExtents() {
        return new Vec2f(maxExtents.x(), maxExtents.y());
    }

    public Vec2f size() {
        return maxExtents.subbed(minExtents).toImmutable();
    }

    public AABBf2d toImmutable() {
        return new AABBf2d(this.minExtents.toImmutable(), this.maxExtents.toImmutable());
    }

    public AABBi2d toIntBox() {
        return new AABBi2d(this.minExtents.toImmutable().toIntVec(), this.maxExtents.toImmutable().toIntVec());
    }

    public MutableAABBi2d toExpandedIntBox() {
        return new MutableAABBi2d((int) this.minExtents.x(), (int) this.minExtents.y(),
                (int) Math.ceil(this.maxExtents.x()), (int) Math.ceil(this.maxExtents.y()));
    }
}

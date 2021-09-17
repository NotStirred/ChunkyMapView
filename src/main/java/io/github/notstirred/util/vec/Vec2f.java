package io.github.notstirred.util.vec;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Vec2f {
    private final float x;
    private final float y;

    public Vec2f added(Vec2f other) {
        return new Vec2f(this.x + other.x, this.y + other.y);
    }

    public Vec2f subbed(Vec2f other) {
        return new Vec2f(this.x - other.x, this.y - other.y);
    }

    public Vec2f scaled(float scalar) {
        return new Vec2f(this.x * scalar, this.y * scalar);
    }

    public Vec2i toIntVec() {
        return new Vec2i((int) this.x, (int) this.y);
    }
}

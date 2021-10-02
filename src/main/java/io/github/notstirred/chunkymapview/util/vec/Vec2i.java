package io.github.notstirred.chunkymapview.util.vec;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Vec2i {
    private final int x;
    private final int y;

    public Vec2i added(Vec2i other) {
        return new Vec2i(this.x + other.x, this.y + other.y);
    }

    public Vec2i subbed(Vec2i other) {
        return new Vec2i(this.x - other.x, this.y - other.y);
    }

    public Vec2i scaled(int scalar) {
        return new Vec2i(this.x * scalar, this.y * scalar);
    }

    public Vec2i scaled(float scalar) {
        return new Vec2i((int)(this.x * scalar), (int)(this.y * scalar));
    }

    public Vec2f toFloatVec() {
        return new Vec2f(this.x, this.y);
    }
}

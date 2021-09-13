package io.github.notstirred.util;

public class Vec2f {
    private final float x;
    private final float y;

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public Vec2f() {
        this(0, 0);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Vec2f added(Vec2f other) {
        return new Vec2f(this.x + other.x, this.y + other.y);
    }

    public Vec2f subbed(Vec2f other) {
        return new Vec2f(this.x - other.x, this.y - other.y);
    }

    public Vec2f scaled(float scalar) {
        return new Vec2f(this.x * scalar, this.y * scalar);
    }
}

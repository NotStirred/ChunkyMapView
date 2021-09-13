package io.github.notstirred.util;

public class MutVec2f {
    private float x, y;

    public MutVec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public MutVec2f() {
        this(0, 0);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void set(MutVec2f other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void addX(float val) {
        this.x += val;
    }

    public void addY(float val) {
        this.y += val;
    }

    public void add(MutVec2f other) {
        this.x += other.x;
        this.y += other.y;
    }

    public MutVec2f added(MutVec2f other) {
        return new MutVec2f(this.x + other.x, this.y + other.y);
    }

    public void subX(float val) {
        this.x -= val;
    }

    public void subY(float val) {
        this.y -= val;
    }

    public void sub(MutVec2f other) {
        this.x -= other.x;
        this.y -= other.y;
    }

    public MutVec2f subbed(MutVec2f other) {
        return new MutVec2f(this.x - other.x, this.y - other.y);
    }

    public void scale(double xScalar, double yScalar) {
        this.x *= xScalar;
        this.y *= yScalar;
    }
}

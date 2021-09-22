package io.github.notstirred.chunkymapview.util.vec;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MutVec2f {
    private float x, y;

    public void set(MutVec2f other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void mulX(float val) {
        this.x *= val;
    }

    public void mulY(float val) {
        this.y *= val;
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

    public void scale(float xScalar, float yScalar) {
        this.x *= xScalar;
        this.y *= yScalar;
    }

    public MutVec2i toIntVec() {
        return new MutVec2i((int) this.x, (int) this.y);
    }

    public Vec2f toImmutable() {
        return new Vec2f(this.x, this.y);
    }
}

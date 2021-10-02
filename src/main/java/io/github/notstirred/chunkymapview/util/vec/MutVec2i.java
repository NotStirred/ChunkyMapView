package io.github.notstirred.chunkymapview.util.vec;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MutVec2i {
    private int x, y;

    public void set(MutVec2i other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void addX(int val) {
        this.x += val;
    }

    public void addY(int val) {
        this.y += val;
    }

    public void add(MutVec2i other) {
        this.x += other.x;
        this.y += other.y;
    }

    public MutVec2i added(MutVec2i other) {
        return new MutVec2i(this.x + other.x, this.y + other.y);
    }

    public void subX(int val) {
        this.x -= val;
    }

    public void subY(int val) {
        this.y -= val;
    }

    public void sub(MutVec2i other) {
        this.x -= other.x;
        this.y -= other.y;
    }

    public MutVec2i subbed(MutVec2i other) {
        return new MutVec2i(this.x - other.x, this.y - other.y);
    }

    public void scale(int xScalar, int yScalar) {
        this.x *= xScalar;
        this.y *= yScalar;
    }

    public MutVec2f toFloatVec() {
        return new MutVec2f(this.x, this.y);
    }

    public Vec2i toImmutable() {
        return new Vec2i(this.x, this.y);
    }
}


package io.github.notstirred.tile;

import lombok.Data;
import lombok.NonNull;

import java.util.Objects;

import static io.github.notstirred.util.Validation.check;

@Data
public class TilePos {
    private final int x;
    private final int z;
    private final int level;

    public TilePos lower() {
        return lowerTo(level + 1);
    }

    /** Return the lower detail {@link TilePos} containing this {@link TilePos} */
    public TilePos lowerTo(int targetLevel) {
        if (targetLevel == this.level) {
            return this;
        }
        check(targetLevel > this.level, "targetLevel must be greater than TilePos#level");

        int shift = targetLevel - this.level;
        return new TilePos(this.x >> shift, this.z >> shift, targetLevel);
    }

    public TilePos raise() {
        return lowerTo(level - 1);
    }

    /** Return the minimum position higher detail {@link TilePos} contained by this {@link TilePos} */
    public TilePos raiseTo(int targetLevel) {
        if (targetLevel == this.level) {
            return this;
        }
        check(targetLevel < this.level, "targetLevel must be greater than TilePos#level");

        int shift = this.level - targetLevel;
        return new TilePos(this.x << shift, this.z << shift, targetLevel);
    }

    /** Return if this {@link TilePos} contains the specified {@link TilePos} */
    public boolean contains(@NonNull TilePos pos) {
        int shift = this.level - pos.level;
        return shift > 0
            && (this.x << shift) >= pos.x && ((this.x + 1) << shift) <= pos.x
            && (this.z << shift) >= pos.z && ((this.z + 1) << shift) <= pos.z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TilePos tilePos = (TilePos) o;
        return x == tilePos.x && z == tilePos.z && level == tilePos.level;
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + Integer.hashCode(x);
        result = 31 * result + Integer.hashCode(z);
        result = 31 * result + Integer.hashCode(level);

        return result;
    }
}

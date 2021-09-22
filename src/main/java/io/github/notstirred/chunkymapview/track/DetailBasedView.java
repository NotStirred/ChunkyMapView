package io.github.notstirred.chunkymapview.track;

import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.util.bb.AABBi2d;
import io.github.notstirred.chunkymapview.util.vec.Vec2i;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
@Getter
public class DetailBasedView implements View<TilePos> {
    private final Vec2i resolution;
    private final AABBi2d extents;

    //inclusive
    private final int lowestLevel;
    //exclusive
    private final int highestLevel;

    private final int padding;

    public boolean hasLevel(int level) {
        return level >= lowestLevel && level < highestLevel;
    }

    public boolean contains(TilePos tilePos) {
        return tilePos.level() >= lowestLevel && tilePos.level() < highestLevel &&
                tilePos.x() > (extents.minExtents().x() >> tilePos.level()) - padding &&
                tilePos.z() > (extents.minExtents().y() >> tilePos.level()) - padding &&
                tilePos.x() < (extents.maxExtents().x() >> tilePos.level()) + padding &&
                tilePos.z() < (extents.maxExtents().y() >> tilePos.level()) + padding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailBasedView that = (DetailBasedView) o;
        return lowestLevel == that.lowestLevel && highestLevel == that.highestLevel && padding == that.padding &&
                Objects.equals(resolution, that.resolution) &&
                //check position didn't change based on level
                extents.minExtents().x() >> this.lowestLevel == that.extents.minExtents().x() >> this.lowestLevel &&
                extents.minExtents().y() >> this.lowestLevel == that.extents.minExtents().y() >> this.lowestLevel &&
                extents.maxExtents().x() >> this.lowestLevel == that.extents.maxExtents().x() >> this.lowestLevel &&
                extents.maxExtents().y() >> this.lowestLevel == that.extents.maxExtents().y() >> this.lowestLevel;
    }
}

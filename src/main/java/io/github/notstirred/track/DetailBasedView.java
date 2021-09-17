package io.github.notstirred.track;

import io.github.notstirred.util.bb.AABBi2d;
import io.github.notstirred.util.vec.Vec2i;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DetailBasedView {
    private final Vec2i resolution;
    private final AABBi2d extents;

    //inclusive
    private final int lowestLevel;
    //exclusive
    private final int highestLevel;

    public boolean hasLevel(int level) {
        return level >= lowestLevel && level < highestLevel;
    }
}

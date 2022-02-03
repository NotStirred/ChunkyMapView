package io.github.notstirred.chunkymapview.tile;

import io.github.notstirred.chunkymapview.util.MathUtil;
import lombok.Data;

@Data
public class MetaTilePos {
    public static final int METATILE_DIAMETER_IN_TILES = 1;
    public static final int METATILE_BITS = (int) MathUtil.log2(METATILE_DIAMETER_IN_TILES);

    private final int x;
    private final int z;
    private final int level;

    public static MetaTilePos from(TilePos pos) {
        return new MetaTilePos(pos.x() >> METATILE_BITS, pos.z() >> METATILE_BITS, pos.level());
    }
}
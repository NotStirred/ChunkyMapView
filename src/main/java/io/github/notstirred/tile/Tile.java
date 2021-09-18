package io.github.notstirred.tile;

import lombok.Data;
import lombok.Setter;

import java.nio.ByteBuffer;

@Data
public class Tile {
    private final TilePos pos;
    private final ByteBuffer data;

    @Setter
    private int texture = -1;

}

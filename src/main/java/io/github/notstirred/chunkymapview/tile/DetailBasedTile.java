package io.github.notstirred.chunkymapview.tile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class DetailBasedTile implements Tile<TilePos, ByteBuffer> {
    private final TilePos pos;
    @Getter @Setter
    private ByteBuffer data;

    public TilePos pos() {
        return pos;
    }
}

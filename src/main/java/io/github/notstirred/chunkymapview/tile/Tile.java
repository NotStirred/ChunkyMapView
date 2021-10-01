package io.github.notstirred.chunkymapview.tile;

import java.nio.ByteBuffer;

public interface Tile<POS, DATA> {
    POS pos();
    ByteBuffer data();
}

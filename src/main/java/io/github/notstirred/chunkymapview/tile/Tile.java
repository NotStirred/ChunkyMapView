package io.github.notstirred.chunkymapview.tile;

import java.nio.ByteBuffer;

public interface Tile<POS> {
    POS pos();
    ByteBuffer data();
}

package io.github.notstirred.chunkymapview.tile.gen;

import java.util.stream.Stream;

public interface TileGenerator<POS, DATA> {
    default Stream<DATA> generateBatch(Stream<POS> positions) {
        return positions.map(this::generate);
    }

    DATA generate(POS pos);
}

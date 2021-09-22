package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.tile.DetailBasedTile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.tile.gen.DetailBasedTileGenerator;

import java.nio.ByteBuffer;

public class DetailBasedMapView extends MapView<TilePos, DetailBasedTile, ByteBuffer> {
    @Override
    protected DetailBasedTileGenerator tileGenerator0() {
        return new DetailBasedTileGenerator();
    }

    @Override
    protected DetailBasedTile createTile0(TilePos pos) {
        return new DetailBasedTile(pos);
    }

    @Override
    protected void generateTile0(DetailBasedTile tile) {
        tile.data(tileGenerator.generate(tile.pos()));
    }
}

package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.tile.DetailBasedTile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.tile.gen.DetailBasedTileGenerator;
import io.github.notstirred.chunkymapview.track.DetailBasedView;
import io.github.notstirred.chunkymapview.track.DetailBasedViewTracker;
import io.github.notstirred.chunkymapview.track.ViewTracker;

import java.nio.ByteBuffer;

public class DetailBasedMapView extends MapView<TilePos, DetailBasedView, DetailBasedTile, ByteBuffer> {
    @Override
    protected ViewTracker<TilePos, DetailBasedView, DetailBasedTile> viewTracker0(MapView<TilePos, DetailBasedView, DetailBasedTile, ByteBuffer> mapView) {
        return new DetailBasedViewTracker(mapView);
    }

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

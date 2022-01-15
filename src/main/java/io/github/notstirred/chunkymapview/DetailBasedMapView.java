package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.collections.cache.Cache;
import io.github.notstirred.chunkymapview.tile.DetailBasedTile;
import io.github.notstirred.chunkymapview.collections.cache.SortedCache;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.tile.gen.DetailBasedTileGenerator;
import io.github.notstirred.chunkymapview.track.DetailBasedView;
import io.github.notstirred.chunkymapview.track.DetailBasedViewTracker;
import io.github.notstirred.chunkymapview.track.ViewTracker;
import io.github.notstirred.chunkymapview.util.bb.AABBi2d;
import io.github.notstirred.chunkymapview.util.gl.ReferenceTrackingMetaTexture2D;
import io.github.notstirred.chunkymapview.util.vec.Vec2i;

import java.nio.ByteBuffer;

import static io.github.notstirred.chunkymapview.util.MathUtil.manhattanDistance;

public class DetailBasedMapView extends MapView<TilePos, DetailBasedView, DetailBasedTile, ByteBuffer> {
    public DetailBasedMapView(int cacheSizeMiB) {
        super(cacheSizeMiB);
    }

    @Override
    protected Cache<RegionPos, ReferenceTrackingMetaTexture2D> cache0(int cacheSizeMiB) {
        int bytesPerTile = 4 * 16*16;
        int bytesPerMetaTexture = bytesPerTile * RegionPos.REGION_DIAMETER_IN_TILES*RegionPos.REGION_DIAMETER_IN_TILES;

        int cacheSizeBytes = cacheSizeMiB * 1024*1024;

        int cacheSize = cacheSizeBytes / bytesPerMetaTexture;

        return new SortedCache<>(cacheSize, (pos1, pos2) -> {
            DetailBasedView view = viewTracker.view();
            Vec2i centre = viewTracker.viewCentre();
            AABBi2d extents = view.extents();

            int texturePosShift = view.lowestLevel() + RegionPos.REGION_BITS;

            Vec2i minExtents = extents.minExtents();

            Vec2i viewSize = extents.maxExtents().subbed(minExtents);

            int viewTextureRadius = ((Math.max(viewSize.x(), viewSize.y()) >> texturePosShift) / 2) + 1;

            int pos1Y = (1 << Math.abs(view.lowestLevel() - pos1.level())) * viewTextureRadius;
            int pos2Y = (1 << Math.abs(view.lowestLevel() - pos2.level())) * viewTextureRadius;

            return Integer.compare(
                    manhattanDistance(
                            centre.x() >> RegionPos.REGION_BITS + view.lowestLevel(),
                            0,
                            centre.y() >> RegionPos.REGION_BITS + view.lowestLevel(),
                            (pos1.x() << pos1.level()) >> view.lowestLevel(),
                            pos1Y,
                            (pos1.z() << pos1.level()) >> view.lowestLevel()
                    ),
                    manhattanDistance(
                            centre.x() >> RegionPos.REGION_BITS + view.lowestLevel(),
                            0,
                            centre.y() >> RegionPos.REGION_BITS + view.lowestLevel(),
                            (pos2.x() << pos2.level()) >> view.lowestLevel(),
                            pos2Y,
                            (pos2.z() << pos2.level()) >> view.lowestLevel()
                    )
            );
        }, this.textureSupplier::release);
    }

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

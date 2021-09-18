package io.github.notstirred;

import io.github.notstirred.render.Renderer;
import io.github.notstirred.tile.Tile;
import io.github.notstirred.tile.TilePos;
import io.github.notstirred.tile.gen.DetailBasedTileGenerator;
import io.github.notstirred.tile.gen.TileGenerator;
import io.github.notstirred.track.DetailBasedView;
import io.github.notstirred.track.DetailBasedViewTracker;
import io.github.notstirred.util.MathUtil;
import io.github.notstirred.util.Validation;
import io.github.notstirred.util.bb.MutableAABBf2d;
import io.github.notstirred.util.vec.MutVec2f;
import io.github.notstirred.util.vec.Vec2i;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkyMapView {

    DetailBasedViewTracker tracker;

    MutVec2f viewResolution = new MutVec2f(1280, 720);

    private static final int MAX_LEVEL = 31;

    public ChunkyMapView() {
        Renderer renderer = new Renderer();

        TileGenerator<TilePos, ByteBuffer> tileGenerator = new DetailBasedTileGenerator();
        Set<TilePos> activePositions = new HashSet<>();
        ConcurrentHashMap<TilePos, Tile> tileCache = new ConcurrentHashMap<>();
        tracker = new DetailBasedViewTracker(pos -> {
            activePositions.add(pos);
            tileCache.computeIfAbsent(pos, p -> {
                Tile tile = new Tile(p, tileGenerator.generate(p));
                tile.texture(renderer.newTexture(tile.data()));
                return tile;
            });
        }, pos -> {
            Validation.check(activePositions.remove(pos), "Position didn't exist!");
//            Tile tile = tileCache.remove(pos);
//            glDeleteTextures(tile.texture());
        });

        MutVec2f viewPos = new MutVec2f(0, 0);
        MutVec2f viewSize = new MutVec2f(128, 72);
        MutableAABBf2d viewExtents = new MutableAABBf2d(viewPos, new MutVec2f());

        List<Tile> activeTiles = new ArrayList<>();
        do {
            activeTiles.clear();
            renderer.update(viewPos, viewSize);
            viewExtents.maxExtents().set(viewPos.added(viewSize));

            int level = Math.min(MAX_LEVEL, calculateHighestLevelForView(viewExtents.size().toIntVec(), viewResolution.toIntVec().toImmutable()));
            DetailBasedView detailBasedView = new DetailBasedView(viewResolution.toIntVec().toImmutable(), viewExtents.toExpandedIntBox().toImmutable(), level, Math.min(level+3, MAX_LEVEL));
            tracker.viewUpdated(detailBasedView);


            for (TilePos pos : activePositions) {
                activeTiles.add(tileCache.get(pos));
            }
        } while(renderer.render(activeTiles, viewExtents));
    }

    private static int calculateHighestLevelForView(Vec2i areaSize, Vec2i viewResolution) {
        double xPixelsPerTile = (viewResolution.x()) / ((double) areaSize.x());
        double zPixelsPerTile = (viewResolution.y()) / ((double) areaSize.y());

        double pixelsPerTile = Math.max(xPixelsPerTile, zPixelsPerTile);
        return (int) Math.floor(Math.max(MathUtil.log2(16*(1/pixelsPerTile)), 0));
    }

    public static void main(String[] args) {
        new ChunkyMapView();
    }
}
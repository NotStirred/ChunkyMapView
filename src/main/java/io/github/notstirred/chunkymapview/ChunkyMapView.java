package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.render.Renderer;
import io.github.notstirred.chunkymapview.tile.DetailBasedTile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.track.DetailBasedView;
import io.github.notstirred.chunkymapview.track.DetailBasedViewTracker;
import io.github.notstirred.chunkymapview.track.ViewTracker;
import io.github.notstirred.chunkymapview.util.MathUtil;
import io.github.notstirred.chunkymapview.util.bb.MutableAABBf2d;
import io.github.notstirred.chunkymapview.util.vec.MutVec2f;
import io.github.notstirred.chunkymapview.util.vec.Vec2i;

import java.nio.ByteBuffer;

public class ChunkyMapView {

    MutVec2f viewResolution = new MutVec2f(1280, 720);

    private static final int MAX_LEVEL = 31;

    private final int PADDING = 2;

    public ChunkyMapView() {

        Renderer renderer = new Renderer();

        MapView<TilePos, DetailBasedTile, ByteBuffer> mapView = new DetailBasedMapView();
        ViewTracker<TilePos, DetailBasedView, DetailBasedTile> tracker = new DetailBasedViewTracker(mapView);

        MutVec2f viewPos = new MutVec2f(0, 0);
        MutVec2f viewSize = new MutVec2f(128, 72);
        MutableAABBf2d viewExtents = new MutableAABBf2d(viewPos, new MutVec2f());

        long timePerFrame = 1000/60;
        long timeLastFrame;

        long startTime = System.currentTimeMillis();
        try {
            do {

                mapView.executeScheduledTasks();
                renderer.update(viewPos, viewSize);
                viewExtents.maxExtents().set(viewPos.added(viewSize));

                int level = Math.min(MAX_LEVEL, calculateHighestLevelForView(viewExtents.size().toIntVec(), viewResolution.toIntVec().toImmutable()));
                DetailBasedView detailBasedView = new DetailBasedView(viewResolution.toIntVec().toImmutable(), viewExtents.toExpandedIntBox().toImmutable(), level, Math.min(level + 3, MAX_LEVEL), PADDING);
                tracker.viewUpdated(detailBasedView);

                timeLastFrame = System.currentTimeMillis() - startTime;

                long delta = timePerFrame - timeLastFrame;
                if(delta > 0) {
                    Thread.sleep(delta);
                }

                startTime = System.currentTimeMillis();
            } while (renderer.render(tracker.tiles(), viewExtents));
        } catch (InterruptedException ignored) { }
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
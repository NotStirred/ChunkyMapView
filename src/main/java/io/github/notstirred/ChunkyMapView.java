package io.github.notstirred;

import io.github.notstirred.tile.TilePos;
import io.github.notstirred.track.DetailBasedView;
import io.github.notstirred.track.DetailBasedViewTracker;
import io.github.notstirred.util.MathUtil;
import io.github.notstirred.util.bb.MutableAABBf2d;
import io.github.notstirred.util.vec.MutVec2f;
import io.github.notstirred.util.vec.Vec2i;

public class ChunkyMapView {
    static {
        System.loadLibrary("chunkymapview");
    }

    DetailBasedViewTracker tracker;

    MutVec2f viewResolution = new MutVec2f(1000, 800);

    private static final int MAX_LEVEL = 31;

    int lastLevel = 0;

    public ChunkyMapView() {
        tracker = new DetailBasedViewTracker();

        setup();

        MutableAABBf2d cameraBox = new MutableAABBf2d(0, 0, 0, 0);
        do {
            float viewX = getViewX();
            float viewZ = getViewZ();
            cameraBox.minExtents().x(viewX);
            cameraBox.minExtents().y(viewZ);
            cameraBox.maxExtents().x(viewX + getViewSizeX());
            cameraBox.maxExtents().y(viewZ + getViewSizeZ());

            int level = Math.min(MAX_LEVEL, calculateHighestLevelForView(cameraBox.size().toIntVec(), viewResolution.toIntVec().toImmutable()));

            lastLevel = level;
            System.out.println(level + " " + Math.min(level+3, MAX_LEVEL));
            DetailBasedView detailBasedView = new DetailBasedView(viewResolution.toIntVec().toImmutable(), cameraBox.toExpandedIntBox().toImmutable(), level, Math.min(level+3, MAX_LEVEL));
            tracker.viewUpdated(detailBasedView);
        } while(render(cameraBox.minExtents().x(), cameraBox.minExtents().y(),
                cameraBox.maxExtents().x() - cameraBox.minExtents().x(),
                cameraBox.maxExtents().y() - cameraBox.minExtents().y(),
                tracker.getPositions().toArray(new TilePos[0]))
        );
    }

    private static int calculateHighestLevelForView(Vec2i areaSize, Vec2i viewResolution) {
        double xPixelsPerTile =  (viewResolution.x()) / ((double) areaSize.x() * 16);
        double zPixelsPerTile =  (viewResolution.y()) / ((double) areaSize.y() * 16);

        double pixelsPerTile = Math.max(xPixelsPerTile, zPixelsPerTile);
        return (int) Math.floor(Math.max(MathUtil.log2(16*(1/pixelsPerTile)), 0));
    }

    private native void setup();

    private native boolean render(float x1, float z1, float x2, float z2, TilePos[] positions);

    private native float getViewX();
    private native float getViewZ();

    private native float getViewSizeX();
    private native float getViewSizeZ();

    public static void main(String[] args) {
        new ChunkyMapView();
    }
}
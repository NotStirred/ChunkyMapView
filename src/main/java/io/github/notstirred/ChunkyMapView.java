package io.github.notstirred;

import io.github.notstirred.util.BoundingBoxf2d;
import io.github.notstirred.util.MathUtil;
import io.github.notstirred.util.MutVec2f;
import io.github.notstirred.util.Vec2f;

import java.util.ArrayList;
import java.util.List;

public class ChunkyMapView {
    static {
        System.loadLibrary("chunkymapview");
    }

    List<Plane> planes = new ArrayList<>();

    BoundingBoxf2d boundingBox = new BoundingBoxf2d(-2.5f, -1.40625f, 2.5f, 1.40625f);
    MutVec2f viewResolution = new MutVec2f(1920, 1080);

    public ChunkyMapView() {
        setup();

        do {
            boundingBox.getMaxExtents().scale(1+2.5f/1000, 1+2.5f/1000);
            boundingBox.moveBy(new MutVec2f(0.025f, 0.025f));
            planes.clear();
            loadForBox(boundingBox);
        } while(render(
                boundingBox.getMinExtents().getX(),
                boundingBox.getMinExtents().getY(),
                boundingBox.getMaxExtents().getX() - boundingBox.getMinExtents().getX(),
                boundingBox.getMaxExtents().getY() - boundingBox.getMinExtents().getY(),
                planes.toArray(new Plane[0]))
        );
    }

    private void loadForBox(BoundingBoxf2d boundingBox) {
        Vec2f maxExtents = boundingBox.getImmutableMaxExtents();
        Vec2f minExtents = boundingBox.getImmutableMinExtents();

        Vec2f areaSize = maxExtents.subbed(minExtents);

        int targetLevel = calculateTargetLodForView(areaSize, viewResolution);

        for (int i = 0; i < 5; i++) {
            loadPlanesInBoxForLevel(minExtents, maxExtents, targetLevel + i);
        }
    }

    private void loadPlanesInBoxForLevel(Vec2f minExtents, Vec2f maxExtents, int targetLod) {
        int minX = ((int) Math.floor(minExtents.getX()) >> targetLod) - 2;
        int minZ = ((int) Math.floor(minExtents.getY()) >> targetLod) - 2;

        int maxX = (((int) Math.floor(maxExtents.getX()) >> targetLod) + 1) + 2;
        int maxZ = (((int) Math.floor(maxExtents.getY()) >> targetLod) + 1) + 2;

        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                planes.add(new Plane(x << targetLod, z << targetLod, (int) Math.pow(2, targetLod)));
            }
        }
    }

    private static int calculateTargetLodForView(Vec2f areaSize, MutVec2f viewResolution) {
        float xPixelsPerTile = viewResolution.getX() / (areaSize.getX() * 16);
        float zPixelsPerTile = viewResolution.getY() / (areaSize.getY() * 16);

        float pixelsPerTile = Math.max(xPixelsPerTile, zPixelsPerTile);
        return (int) Math.max(MathUtil.log2(16*(1/pixelsPerTile)), 0);
    }

    private native void setup();

    private native boolean render(float x1, float z1, float x2, float z2, Plane[] planes);

    public static void main(String[] args) {
        new ChunkyMapView();
    }
}

class Plane {
    private final int x, z, scale;

    public Plane(final int x, final int z, final int scale) {
        this.x = x;
        this.z = z;
        this.scale = scale;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getScale() {
        return scale;
    }
}
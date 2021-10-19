package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.concurrent.SimpleTaskPool;
import io.github.notstirred.chunkymapview.tile.SortedCache;
import io.github.notstirred.chunkymapview.tile.Tile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.tile.gen.TileGenerator;
import io.github.notstirred.chunkymapview.track.View;
import io.github.notstirred.chunkymapview.track.ViewTracker;
import io.github.notstirred.chunkymapview.util.MathUtil;
import io.github.notstirred.chunkymapview.util.gl.ReferenceTrackingMetaTexture2D;
import io.github.notstirred.chunkymapview.util.vec.Vec2i;
import lombok.Data;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static io.github.notstirred.chunkymapview.util.MathUtil.manhattanDistance;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public abstract class MapView<POS extends TilePos, VIEW extends View<POS>, TILE extends Tile<POS>, DATA> {
    private static final SimpleTaskPool glThreadExecutor = new SimpleTaskPool(Thread.currentThread());

    public static Executor glThreadExecutor() {
        return MapView.glThreadExecutor;
    }

    protected final TileGenerator<POS, DATA> tileGenerator = this.tileGenerator0();

    @Getter
    protected final ViewTracker<POS, VIEW, TILE> viewTracker;

    @SuppressWarnings("unchecked")
    private final SortedCache<RegionPos, ReferenceTrackingMetaTexture2D>[] metaTextures = new SortedCache[32];

    public MapView(int cacheSizeMiB) {
        int bytesPerTile = 4 * 16*16;
        int bytesPerMetaTexture = bytesPerTile * RegionPos.REGION_DIAMETER_IN_TILES*RegionPos.REGION_DIAMETER_IN_TILES;

        int cacheSizeBytes = cacheSizeMiB * 1024*1024;

        int cacheSize = cacheSizeBytes / bytesPerMetaTexture;

        this.viewTracker = viewTracker0(this);
        for (int level = 0; level < metaTextures.length; level++) {
            int finalLevel = level;
            metaTextures[level] = new SortedCache<>(cacheSize, (pos1, pos2) -> {
                Vec2i centre = viewTracker.viewCentre();
                return Integer.compare(
                        manhattanDistance(centre.x() >> finalLevel + RegionPos.REGION_BITS, centre.y() >> finalLevel + RegionPos.REGION_BITS, pos1.x(), pos1.z()),
                        manhattanDistance(centre.x() >> finalLevel + RegionPos.REGION_BITS, centre.y() >> finalLevel + RegionPos.REGION_BITS, pos2.x(), pos2.z())
                );
            });
        }
    }

    private ReferenceTrackingMetaTexture2D create() {
        return new ReferenceTrackingMetaTexture2D(16, 16, RegionPos.REGION_DIAMETER_IN_TILES, RegionPos.REGION_DIAMETER_IN_TILES,
                GL_RGBA8, GL_UNSIGNED_BYTE, GL_RGBA,
                GL_NEAREST, GL_NEAREST,
                GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE
        );
    }

    public SortedCache<RegionPos, ReferenceTrackingMetaTexture2D>[] metaTextures() {
        return metaTextures;
    }

    @Data
    public static class RegionPos {
        public static final int REGION_DIAMETER_IN_TILES = 16;
        public static final int REGION_BITS = (int) MathUtil.log2(REGION_DIAMETER_IN_TILES);

        private final int x;
        private final int z;
        private final int level;

        private static RegionPos from(TilePos pos) {
            return new RegionPos(pos.x() >> REGION_BITS, pos.z() >> REGION_BITS, pos.level());
        }
    }

    public CompletableFuture<TILE> loadingFuture(POS pos, Executor executor) {
        {
            int xIdx = pos.x() & (RegionPos.REGION_DIAMETER_IN_TILES - 1);
            int zIdx = pos.z() & (RegionPos.REGION_DIAMETER_IN_TILES - 1);

            ReferenceTrackingMetaTexture2D texture = metaTextures[pos.level()].get(RegionPos.from(pos));
            //if texture exists, and is already tracking this tile, we just return an empty tile for this position
            if (texture != null && texture.contains(xIdx, zIdx)) {
                return CompletableFuture.completedFuture(createTile0(pos));
            }
        }

        return CompletableFuture.supplyAsync(() -> {
            TILE tile = createTile0(pos);
            generateTile0(tile);
            return tile;
        }, executor).thenComposeAsync(tile -> {
            RegionPos regionPos = RegionPos.from(tile.pos());

            ReferenceTrackingMetaTexture2D areaTexture = metaTextures[pos.level()].computeIfAbsent(regionPos, (p) -> this.create());

            int xIdx = tile.pos().x() & (RegionPos.REGION_DIAMETER_IN_TILES - 1);
            int zIdx = tile.pos().z() & (RegionPos.REGION_DIAMETER_IN_TILES - 1);
            areaTexture.ref(xIdx, zIdx);
            areaTexture.set(xIdx, zIdx, tile.data()); //tile.data is never assigned to null, so no race condition
            return CompletableFuture.completedFuture(tile);
        }, glThreadExecutor);
    }

    public void tileUnloadSync(POS pos) {
        metaTextures[pos.level()].computeIfPresent(RegionPos.from(pos), (regionPos, texture) -> {
            int xIdx = pos.x() & (RegionPos.REGION_DIAMETER_IN_TILES - 1);
            int zIdx = pos.z() & (RegionPos.REGION_DIAMETER_IN_TILES - 1);
            texture.deref(xIdx, zIdx);

            if (!texture.anyRef()) // no loaded tiles reference this texture, can be unloaded
                return null;

            //another tile is still using this texture, clear the data for this tile from it
            texture.set(xIdx, zIdx, ByteBuffer.allocateDirect(16 * 16 * 4));
            return texture;
        });
    }

    protected abstract ViewTracker<POS, VIEW, TILE> viewTracker0(MapView<POS, VIEW, TILE, DATA> mapView);

    protected abstract TileGenerator<POS, DATA> tileGenerator0();

    protected abstract TILE createTile0(POS pos);

    protected abstract void generateTile0(TILE tile);

    public void scheduleTask(Runnable runnable) {
        glThreadExecutor.execute(runnable);
    }
    public static void scheduleTaskStatic(Runnable runnable) {
        glThreadExecutor.execute(runnable);
    }

    public void executeScheduledTasks() {
        glThreadExecutor.runTasks();
    }
}

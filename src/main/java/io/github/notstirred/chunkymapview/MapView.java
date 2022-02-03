package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.collections.cache.Cache;
import io.github.notstirred.chunkymapview.concurrent.SimpleTaskPool;
import io.github.notstirred.chunkymapview.tile.MetaTilePos;
import io.github.notstirred.chunkymapview.tile.Tile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.tile.gen.TileGenerator;
import io.github.notstirred.chunkymapview.track.View;
import io.github.notstirred.chunkymapview.track.ViewTracker;
import io.github.notstirred.chunkymapview.util.MathUtil;
import io.github.notstirred.chunkymapview.util.ResettingRecyclingSupplier;
import io.github.notstirred.chunkymapview.util.gl.ReferenceTrackingMetaTexture2D;
import lombok.Data;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public abstract class MapView<POS extends TilePos, VIEW extends View<POS>, TILE extends Tile<POS>, DATA> {
    private static final SimpleTaskPool glThreadExecutor = new SimpleTaskPool(Thread.currentThread());

    public static Executor glThreadExecutor() {
        return MapView.glThreadExecutor;
    }

    protected final TileGenerator<POS, DATA> tileGenerator = this.tileGenerator0();

    @Getter
    protected final ViewTracker<POS, VIEW, TILE> viewTracker;

    protected final Cache<MetaTilePos, ReferenceTrackingMetaTexture2D> textureCache;
    protected final ResettingRecyclingSupplier<ReferenceTrackingMetaTexture2D> textureSupplier = new ResettingRecyclingSupplier<ReferenceTrackingMetaTexture2D>() {
        @Override
        protected ReferenceTrackingMetaTexture2D allocate0() {
            return create();
        }
    };

    public MapView(int cacheSizeMiB) {
        this.viewTracker = viewTracker0(this);
        this.textureCache = cache0(cacheSizeMiB);
    }

    private ReferenceTrackingMetaTexture2D create() {
        return new ReferenceTrackingMetaTexture2D(TilePos.TILE_DIAMETER, TilePos.TILE_DIAMETER, MetaTilePos.METATILE_DIAMETER_IN_TILES, MetaTilePos.METATILE_DIAMETER_IN_TILES,
                GL_RGBA8, GL_UNSIGNED_BYTE, GL_RGBA,
                GL_NEAREST, GL_NEAREST,
                GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE
        );
    }

    public Cache<MetaTilePos, ReferenceTrackingMetaTexture2D> textureCache() {
        return textureCache;
    }

    public CompletableFuture<TILE> loadingFuture(POS pos, Executor executor) {
        {
            int xIdx = pos.x() & (MetaTilePos.METATILE_DIAMETER_IN_TILES - 1);
            int zIdx = pos.z() & (MetaTilePos.METATILE_DIAMETER_IN_TILES - 1);

            ReferenceTrackingMetaTexture2D texture = textureCache.get(MetaTilePos.from(pos));
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
            MetaTilePos metaTilePos = MetaTilePos.from(tile.pos());

            ReferenceTrackingMetaTexture2D areaTexture = textureCache.computeIfAbsent(metaTilePos, (p) -> this.textureSupplier.allocate());

            int xIdx = tile.pos().x() & (MetaTilePos.METATILE_DIAMETER_IN_TILES - 1);
            int zIdx = tile.pos().z() & (MetaTilePos.METATILE_DIAMETER_IN_TILES - 1);
            areaTexture.ref(xIdx, zIdx);
            areaTexture.set(xIdx, zIdx, tile.data()); //tile.data is never assigned to null, so no race condition
            return CompletableFuture.completedFuture(tile);
        }, glThreadExecutor);
    }

    public void tileUnloadSync(POS pos) {
        textureCache.computeIfPresent(MetaTilePos.from(pos), (metaTilePos, texture) -> {
            int xIdx = pos.x() & (MetaTilePos.METATILE_DIAMETER_IN_TILES - 1);
            int zIdx = pos.z() & (MetaTilePos.METATILE_DIAMETER_IN_TILES - 1);
            texture.deref(xIdx, zIdx);

            if (!texture.anyRef()) // no loaded tiles reference this texture, can be unloaded
                return null;

            //another tile is still using this texture, clear the data for this tile from it
            texture.set(xIdx, zIdx, ByteBuffer.allocateDirect(TilePos.TILE_DIAMETER * TilePos.TILE_DIAMETER * 4));
            return texture;
        });
    }

    protected abstract Cache<MetaTilePos, ReferenceTrackingMetaTexture2D> cache0(int cacheSizeMiB);

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

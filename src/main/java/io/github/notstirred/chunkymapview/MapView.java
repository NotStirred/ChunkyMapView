package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.concurrent.SimpleTaskPool;
import io.github.notstirred.chunkymapview.tile.Tile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.tile.gen.TileGenerator;
import io.github.notstirred.chunkymapview.util.MathUtil;
import io.github.notstirred.chunkymapview.util.gl.ReferenceCountedMetaTexture2D;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

@RequiredArgsConstructor
public abstract class MapView<POS extends TilePos, TILE extends Tile<POS>, DATA> {
    private static final SimpleTaskPool glThreadExecutor = new SimpleTaskPool(Thread.currentThread());

    public static Executor glThreadExecutor() {
        return MapView.glThreadExecutor;
    }

    protected final TileGenerator<POS, DATA> tileGenerator = this.tileGenerator0();

    private final Map<RegionPos, ReferenceCountedMetaTexture2D> metaTextures = new HashMap<>();

    private ReferenceCountedMetaTexture2D create() {
        return new ReferenceCountedMetaTexture2D(16, 16, RegionPos.REGION_DIAMETER_IN_TILES, RegionPos.REGION_DIAMETER_IN_TILES,
                GL_RGBA8, GL_UNSIGNED_BYTE, GL_RGBA,
                GL_LINEAR, GL_LINEAR,
                GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE
        );
    }

    public Map<RegionPos, ReferenceCountedMetaTexture2D> metaTextures() {
        return metaTextures;
    }

    @Data
    public static class RegionPos {
        public static final int REGION_DIAMETER_IN_TILES = 64;
        public static final int REGION_BITS = (int) MathUtil.log2(REGION_DIAMETER_IN_TILES);

        private final int x;
        private final int z;
        private final int level;

        private static RegionPos from(TilePos pos) {
            return new RegionPos(pos.x() >> REGION_BITS, pos.z() >> REGION_BITS, pos.level());
        }
    }

    @NonNull
    public CompletableFuture<TILE> loadingFuture(POS pos, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            TILE tile = createTile0(pos);
            generateTile0(tile);
            return tile;
        }, executor).thenComposeAsync(tile -> {
            RegionPos regionPos = RegionPos.from(tile.pos());

            ReferenceCountedMetaTexture2D areaTexture = metaTextures.computeIfAbsent(regionPos, (p) -> this.create());
            areaTexture.ref();

            areaTexture.set(
                    (tile.pos().x() & (RegionPos.REGION_DIAMETER_IN_TILES-1))*16,
                    (tile.pos().z() & (RegionPos.REGION_DIAMETER_IN_TILES-1))*16,
                tile.data() //tile.data is never assigned to null, so no race condition
            );
            return CompletableFuture.completedFuture(tile);
        }, glThreadExecutor);
    }

    public void tileUnloadSync(POS pos) {
        metaTextures.computeIfPresent(RegionPos.from(pos), (regionPos, texture) -> {
            texture.deref();

            if (!texture.anyRef()) // no loaded tiles reference this texture, can be unloaded
                return null;

            //another tile is still using this texture, clear the data for this tile from it
            texture.set(
                    (pos.x() & (RegionPos.REGION_DIAMETER_IN_TILES-1))*16,
                    (pos.z() & (RegionPos.REGION_DIAMETER_IN_TILES-1))*16,
                    ByteBuffer.allocateDirect(16 * 16 * 4)
            );
            return texture;
        });
    }

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

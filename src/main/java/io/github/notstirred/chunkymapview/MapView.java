package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.concurrent.SimpleTaskPool;
import io.github.notstirred.chunkymapview.tile.Tile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.tile.gen.TileGenerator;
import io.github.notstirred.chunkymapview.util.RecyclingSupplier;
import io.github.notstirred.chunkymapview.util.gl.ReusableGLTexture;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

@RequiredArgsConstructor
public abstract class MapView<POS extends TilePos, TILE extends Tile<POS, DATA>, DATA> {
    private static final SimpleTaskPool glThreadExecutor = new SimpleTaskPool(Thread.currentThread());

    public static Executor glThreadExecutor() {
        return MapView.glThreadExecutor;
    }

    protected final TileGenerator<POS, DATA> tileGenerator = this.tileGenerator0();

    private final RecyclingSupplier<ReusableGLTexture> textureSupplier = new RecyclingSupplier<ReusableGLTexture>() {
        @Override
        protected ReusableGLTexture allocate0() {
            return new ReusableGLTexture(16, 16, GL_RGB8, GL_UNSIGNED_BYTE, GL_TEXTURE_2D, GL_RGB, GL_LINEAR, GL_NEAREST, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE);
        }
    };

    @NonNull
    public CompletableFuture<TILE> loadingFuture(POS pos, Executor executor) {
        CompletableFuture<TILE> tileFuture = CompletableFuture.supplyAsync(() -> {
            TILE tile = createTile0(pos);
            generateTile0(tile);
            return tile;
        }, executor);

        tileFuture.thenAcceptAsync(tile -> {
            ReusableGLTexture texture = this.newTexture();
            texture.setTexture(tile.data(), false);
            tile.texture(texture);
        }, glThreadExecutor);

        return tileFuture;
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

    public ReusableGLTexture newTexture() {
        return textureSupplier.allocate();
    }

    public void freeTexture(ReusableGLTexture texture) {
        textureSupplier.release(texture);
    }
}

package io.github.notstirred.chunkymapview;

import io.github.notstirred.chunkymapview.tile.Tile;
import io.github.notstirred.chunkymapview.tile.TilePos;
import io.github.notstirred.chunkymapview.tile.gen.TileGenerator;
import io.github.notstirred.chunkymapview.util.RecyclingSupplier;
import io.github.notstirred.chunkymapview.util.gl.ReusableGLTexture;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

@RequiredArgsConstructor
public abstract class MapView<POS extends TilePos, TILE extends Tile<POS, DATA>, DATA> {
    private final Deque<Runnable> tasks = new ConcurrentLinkedDeque<>();
    private static final Deque<Runnable> staticTasks = new ConcurrentLinkedDeque<>();

    protected final TileGenerator<POS, DATA> tileGenerator = this.tileGenerator0();

    private final RecyclingSupplier<ReusableGLTexture> textureSupplier = new RecyclingSupplier<ReusableGLTexture>() {
        @Override
        protected ReusableGLTexture allocate0() {
            return new ReusableGLTexture(16, 16, GL_RGB, GL_UNSIGNED_BYTE, GL_TEXTURE_2D, GL_LINEAR, GL_NEAREST, GL_CLAMP_TO_EDGE);
        }
    };

    @NonNull
    public CompletableFuture<TILE> futureForPos(POS pos) {
        return CompletableFuture.supplyAsync(() -> {
            TILE tile = createTile0(pos);
            generateTile0(tile);
            return tile;
        });
    }

    protected abstract TileGenerator<POS, DATA> tileGenerator0();

    protected abstract TILE createTile0(POS pos);

    protected abstract void generateTile0(TILE tile);

    public void scheduleTask(Runnable runnable) {
        tasks.addLast(runnable);
    }
    public static void scheduleTaskStatic(Runnable runnable) {
        staticTasks.addLast(runnable);
    }

    public void executeScheduledTasks() {
        tasks.removeIf((runnable) -> { runnable.run(); return true; });
        staticTasks.removeIf((runnable) -> { runnable.run(); return true; });
    }

    public ReusableGLTexture newTexture() {
        return textureSupplier.allocate();
    }

    public void freeTexture(ReusableGLTexture texture) {
        textureSupplier.release(texture);
    }
}

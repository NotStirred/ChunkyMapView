package io.github.notstirred.chunkymapview.util.gl;

import io.github.notstirred.chunkymapview.MapView;
import lombok.Getter;
import net.daporkchop.lib.unsafe.PCleaner;

public abstract class GLObject {
    private final PCleaner cleaner;
    @Getter
    protected final int id;
    public GLObject(int id) {
        this.id = id;
        Runnable delete = deleter(id);
        this.cleaner = PCleaner.cleaner(this, () -> MapView.scheduleTaskStatic(delete));
    }

    protected abstract void bind();

    protected abstract Runnable deleter(int id);
}

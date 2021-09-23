package io.github.notstirred.chunkymapview.tile;

import io.github.notstirred.chunkymapview.util.gl.ReusableGLTexture;

import java.nio.ByteBuffer;

public interface Tile<POS, DATA> {

    void texture(ReusableGLTexture texture);

    ByteBuffer data();
}

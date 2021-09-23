package io.github.notstirred.chunkymapview.tile;

import io.github.notstirred.chunkymapview.util.gl.ReusableGLTexture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public class DetailBasedTile implements Tile<TilePos, ByteBuffer> {
    @Getter
    private final TilePos pos;
    @Getter @Setter
    private ByteBuffer data;

    @Getter
    private ReusableGLTexture texture = null;

    public void texture(ReusableGLTexture texture) {
        this.texture = texture;
    }
}

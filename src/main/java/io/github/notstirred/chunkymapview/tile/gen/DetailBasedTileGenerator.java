package io.github.notstirred.chunkymapview.tile.gen;

import io.github.notstirred.chunkymapview.tile.TilePos;
import org.spongepowered.noise.Noise;
import org.spongepowered.noise.NoiseQuality;

import java.nio.ByteBuffer;

public class DetailBasedTileGenerator implements TileGenerator<TilePos, ByteBuffer> {
    @Override
    public ByteBuffer generate(TilePos tilePos) {
        ByteBuffer imageBuffer = ByteBuffer.allocateDirect(TilePos.TILE_DIAMETER * TilePos.TILE_DIAMETER * 4);

        int level = tilePos.level();
        int scaledTileX = (tilePos.x() << level) * TilePos.TILE_DIAMETER;
        int scaledTileZ = (tilePos.z() << level) * TilePos.TILE_DIAMETER;

        for (int z = 0; z < TilePos.TILE_DIAMETER; z++) {
            for (int x = 0; x < TilePos.TILE_DIAMETER; x++) {
                int x1 = scaledTileX + (x << level);
                int z1 = scaledTileZ + (z << level);

                double noise = Noise.gradientCoherentNoise3D(x1/128.0, 0, z1/128.0, 0, NoiseQuality.BEST);
                byte val = (byte)(noise * 256);
                imageBuffer.put(val);
                imageBuffer.put(val);
                imageBuffer.put(val);
                imageBuffer.put((byte)255);
            }
        }

        imageBuffer.flip();
        return imageBuffer;
    }
}
